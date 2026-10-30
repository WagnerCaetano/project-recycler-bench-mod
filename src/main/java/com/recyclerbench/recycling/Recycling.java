package com.recyclerbench.recycling;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.recyclerbench.RecyclerBenchMod;
import com.recyclerbench.RecyclerConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;

/**
 * Turns one crafted item into the rounded share of its ingredients.
 * Eight diamonds at 0.6 become {@code round(4.8) = 5}.
 */
public final class Recycling {
    private static final TagKey<Item> C_TOOLS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools"));
    private static final TagKey<Item> C_ARMORS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "armors"));
    private static final TagKey<Item> C_NETHERITE_INGOTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/netherite"));
    private static final TagKey<Item> FORGE_NETHERITE_INGOTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "ingots/netherite"));

    private static final Object CACHE_LOCK = new Object();
    private static RecipeManager cachedManager;
    private static int cachedFingerprint = Integer.MIN_VALUE;
    private static Map<Item, Map<Item, Double>> cachedRecipes = Map.of();

    private Recycling() {
    }

    public static boolean canRecycle(Level level, ItemStack stack) {
        return !ingredientsFor(level, stack).isEmpty();
    }

    public static List<ItemStack> resultsFor(Level level, ItemStack stack) {
        Map<Item, Double> used = ingredientsFor(level, stack);
        if (used.isEmpty() || belowDurabilityCutoff(stack)) {
            return List.of();
        }
        return scale(netheriteIngotOnly(used), RecyclerConfig.returnRate());
    }

    private static Map<Item, Double> ingredientsFor(Level level, ItemStack stack) {
        if (level == null || stack.isEmpty() || isDisabled(stack.getItem())) {
            return Map.of();
        }

        Map<Item, Double> used = customRecipe(stack.getItem());
        if (used == null) {
            if (!RecyclerConfig.automaticEquipment() || !isEquipment(stack)) {
                return Map.of();
            }
            used = recipeFor(level, stack.getItem());
        }
        return used == null ? Map.of() : used;
    }

    /**
     * Remaining durability strictly below the configured fraction yields nothing.
     * An undamaged item is 1.0, so 0.5 still returns materials and anything under it does not.
     */
    private static boolean belowDurabilityCutoff(ItemStack stack) {
        double minimum = RecyclerConfig.minDurability();
        if (minimum <= 0.0D || !stack.isDamageableItem()) {
            return false;
        }
        int max = stack.getMaxDamage();
        if (max <= 0) {
            return false;
        }
        double remaining = (max - stack.getDamageValue()) / (double) max;
        return remaining < minimum;
    }

    private static Map<Item, Double> netheriteIngotOnly(Map<Item, Double> used) {
        if (!RecyclerConfig.netheriteIngotOnly()) {
            return used;
        }
        Map<Item, Double> ingots = new LinkedHashMap<>();
        for (Map.Entry<Item, Double> entry : used.entrySet()) {
            if (isNetheriteIngot(entry.getKey())) {
                ingots.put(entry.getKey(), entry.getValue());
            }
        }
        return ingots.isEmpty() ? used : ingots;
    }

    private static boolean isNetheriteIngot(Item item) {
        if ("netherite_ingot".equals(BuiltInRegistries.ITEM.getKey(item).getPath())) {
            return true;
        }
        ItemStack stack = new ItemStack(item);
        return stack.is(C_NETHERITE_INGOTS) || stack.is(FORGE_NETHERITE_INGOTS);
    }

    public static boolean isEquipment(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ArmorItem
                || item instanceof AnimalArmorItem
                || item instanceof DiggerItem
                || item instanceof SwordItem
                || item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof TridentItem
                || item instanceof MaceItem
                || item instanceof FishingRodItem
                || item instanceof ShearsItem
                || item instanceof ShieldItem
                || item instanceof ElytraItem
                || item instanceof FlintAndSteelItem
                || item instanceof BrushItem) {
            return true;
        }
        return stack.is(ItemTags.HEAD_ARMOR)
                || stack.is(ItemTags.CHEST_ARMOR)
                || stack.is(ItemTags.LEG_ARMOR)
                || stack.is(ItemTags.FOOT_ARMOR)
                || stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES)
                || stack.is(C_TOOLS)
                || stack.is(C_ARMORS);
    }

    /**
     * @return the configured ingredient counts, an empty map when the line exists but its items are missing,
     * or null when this item has no custom line
     */
    private static Map<Item, Double> customRecipe(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        Map<Item, Double> found = null;
        for (String line : RecyclerConfig.extraRecipes()) {
            int split = line.indexOf('=');
            if (split <= 0) {
                continue;
            }
            ResourceLocation configured = parseId(line.substring(0, split));
            if (configured == null || !configured.equals(itemId)) {
                continue;
            }
            Map<Item, Double> counts = new LinkedHashMap<>();
            for (String part : line.substring(split + 1).split(",")) {
                int star = part.lastIndexOf('*');
                ResourceLocation ingredientId = parseId(part.substring(0, star));
                if (ingredientId == null) {
                    continue;
                }
                Item ingredient = BuiltInRegistries.ITEM.getOptional(ingredientId).orElse(null);
                if (ingredient == null || ingredient == Items.AIR) {
                    RecyclerBenchMod.LOGGER.warn("Recycler recipe for {} refers to unknown item {}", itemId, ingredientId);
                    continue;
                }
                int count = Integer.parseInt(part.substring(star + 1));
                counts.merge(ingredient, (double) count, Double::sum);
            }
            found = counts;
            break;
        }
        return found;
    }

    private static boolean isDisabled(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        for (String disabled : RecyclerConfig.disabledItems()) {
            if (disabled.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static Map<Item, Double> recipeFor(Level level, Item item) {
        synchronized (CACHE_LOCK) {
            RecipeManager manager = level.getRecipeManager();
            int fingerprint = fingerprint(manager);
            if (cachedManager != manager || cachedFingerprint != fingerprint) {
                cachedRecipes = indexRecipes(manager, level);
                cachedManager = manager;
                cachedFingerprint = fingerprint;
            }
            return cachedRecipes.get(item);
        }
    }

    private static int fingerprint(RecipeManager manager) {
        List<RecipeHolder<CraftingRecipe>> crafting = manager.getAllRecipesFor(RecipeType.CRAFTING);
        List<RecipeHolder<SmithingRecipe>> smithing = manager.getAllRecipesFor(RecipeType.SMITHING);
        return crafting.size() * 31 + smithing.size() * 17 + System.identityHashCode(crafting);
    }

    private static Map<Item, Map<Item, Double>> indexRecipes(RecipeManager manager, Level level) {
        Map<Item, Map<Item, Double>> indexed = new LinkedHashMap<>();
        Map<Item, Integer> scores = new LinkedHashMap<>();

        for (RecipeHolder<CraftingRecipe> holder : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = holder.value();
            if (recipe.isSpecial()) {
                continue;
            }
            ItemStack result = recipe.getResultItem(level.registryAccess());
            if (result.isEmpty() || result.getCount() <= 0) {
                continue;
            }
            Map<Item, Double> ingredients = ingredientsOf(recipe.getIngredients(), result.getCount(), result.getItem());
            if (ingredients == null) {
                continue;
            }
            int score = 800 + ingredients.size();
            if ("minecraft".equals(holder.id().getNamespace())) {
                score += 1200;
            }
            if (recipe instanceof ShapedRecipe) {
                score += 100;
            }
            if (result.getCount() == 1) {
                score += 50;
            }
            consider(indexed, scores, result.getItem(), ingredients, score);
        }

        for (RecipeHolder<SmithingRecipe> holder : manager.getAllRecipesFor(RecipeType.SMITHING)) {
            if (!(holder.value() instanceof SmithingTransformRecipe smithing)) {
                continue;
            }
            ItemStack result = smithing.getResultItem(level.registryAccess());
            if (result.isEmpty()) {
                continue;
            }
            List<Ingredient> ingredients = List.of(smithing.template, smithing.base, smithing.addition);
            Map<Item, Double> counts = ingredientsOf(ingredients, Math.max(1, result.getCount()), result.getItem());
            if (counts == null) {
                continue;
            }
            int score = 200;
            if ("minecraft".equals(holder.id().getNamespace())) {
                score += 300;
            }
            consider(indexed, scores, result.getItem(), counts, score);
        }
        return indexed;
    }

    private static void consider(Map<Item, Map<Item, Double>> indexed, Map<Item, Integer> scores, Item result,
                                 Map<Item, Double> ingredients, int score) {
        Integer previous = scores.get(result);
        if (previous == null || score > previous) {
            scores.put(result, score);
            indexed.put(result, ingredients);
        }
    }

    /**
     * @return ingredient totals for a single output item, or null when the recipe consumes its own output
     */
    private static Map<Item, Double> ingredientsOf(List<Ingredient> ingredients, int resultCount, Item resultItem) {
        if (resultCount <= 0) {
            return null;
        }
        Map<Item, Double> totals = new LinkedHashMap<>();
        boolean any = false;
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty() || ingredient.hasNoItems()) {
                continue;
            }
            ItemStack[] choices = ingredient.getItems();
            if (choices.length == 0 || choices[0].isEmpty()) {
                continue;
            }
            Item chosen = choices[0].getItem();
            if (chosen == Items.AIR) {
                continue;
            }
            if (chosen == resultItem) {
                return null;
            }
            totals.merge(chosen, 1.0D / resultCount, Double::sum);
            any = true;
        }
        return any ? totals : null;
    }

    static List<ItemStack> scale(Map<Item, Double> used, double rate) {
        List<ItemStack> results = new ArrayList<>();
        if (rate <= 0.0D) {
            return results;
        }
        for (Map.Entry<Item, Double> entry : used.entrySet()) {
            int count = (int) Math.round(entry.getValue() * rate + 1.0E-6D);
            Item item = entry.getKey();
            int max = Math.max(1, item.getDefaultMaxStackSize());
            while (count > 0) {
                int take = Math.min(count, max);
                results.add(new ItemStack(item, take));
                count -= take;
            }
        }
        return results;
    }

    private static ResourceLocation parseId(String text) {
        try {
            return ResourceLocation.parse(text);
        } catch (IllegalArgumentException exception) {
            RecyclerBenchMod.LOGGER.warn("Ignoring invalid recycler item id '{}'", text);
            return null;
        }
    }
}
