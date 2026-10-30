package com.recyclerbench;

import java.util.List;
import java.util.regex.Pattern;

import net.minecraftforge.common.ForgeConfigSpec;

public final class RecyclerConfig {
    private static final Pattern RECIPE_LINE = Pattern.compile(
            "^[a-z0-9_.-]+:[a-z0-9_./-]+=[a-z0-9_.-]+:[a-z0-9_./-]+\\*(?:[1-9]\\d{0,3})(?:,[a-z0-9_.-]+:[a-z0-9_./-]+\\*(?:[1-9]\\d{0,3}))*$");
    private static final Pattern ITEM_ID = Pattern.compile("^[a-z0-9_.-]+:[a-z0-9_./-]+$");

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    static {
        BUILDER.comment(
                "Recycler Bench",
                "Tools and armor are recycled from their crafting or smithing recipe.",
                "A diamond chestplate costs 8 diamonds. At 0.6 that is 8 * 0.6 = 4.8, which rounds to 5.",
                "0.5 and above rounds up (4.5 -> 5, 0.6 -> 1). Below 0.5 rounds down (4.3 -> 4, 0.4 -> 0).",
                "One-ingredient costs therefore come back whole at 60%: a netherite upgrade returns the ingot, the template, and the diamond piece.",
                "Add those items to disabled_items if a world should not allow that.",
                "Tag ingredients (such as wooden planks) return the first item in the tag. Override them with extra_recipes if you want a specific item.",
                "This file is written to config/recyclerbench-common.toml."
        );
        BUILDER.push("recycling");
    }

    public static final ForgeConfigSpec.DoubleValue RETURN_RATE = BUILDER
            .comment("Fraction of each ingredient that is returned. 0.6 means 60 percent.")
            .defineInRange("return_rate", 0.6D, 0.0D, 1.0D);

    public static final ForgeConfigSpec.IntValue PROCESS_TICKS = BUILDER
            .comment("How long one item takes to recycle, in ticks. 20 ticks is one second.")
            .defineInRange("process_ticks", 40, 1, 20 * 60);

    public static final ForgeConfigSpec.BooleanValue PLAY_CHAINSAW = BUILDER
            .comment("Play a chainsaw while the bench is grinding an item.")
            .define("play_chainsaw", true);

    public static final ForgeConfigSpec.BooleanValue AUTOMATIC_EQUIPMENT = BUILDER
            .comment("Recycle vanilla and modded tools and armor by reading their crafting or smithing recipe.",
                    "Turn this off to recycle only the items listed in extra_recipes.")
            .define("automatic_equipment", true);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_RECIPES = BUILDER
            .comment(
                    "Extra recycling recipes. These override automatic lookup and can describe items that are not tools or armor.",
                    "Format: \"namespace:item=namespace:ingredient*count,namespace:ingredient*count\"",
                    "Counts are the full crafting cost, before return_rate is applied.",
                    "Example: \"coolmod:ruby_chestplate=coolmod:ruby*8\"",
                    "Example: \"coolmod:ruby_pickaxe=coolmod:ruby*3,minecraft:stick*2\""
            )
            .defineListAllowEmpty("extra_recipes", List.of(), RecyclerConfig::validateRecipeLine);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DISABLED_ITEMS = BUILDER
            .comment(
                    "Item ids that can never be recycled, even if a recipe exists.",
                    "Example: \"minecraft:netherite_chestplate\""
            )
            .defineListAllowEmpty("disabled_items", List.of(), RecyclerConfig::validateItemId);

    static {
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private RecyclerConfig() {
    }

    public static double returnRate() {
        return SPEC.isLoaded() ? RETURN_RATE.get() : 0.6D;
    }

    public static int processTicks() {
        return SPEC.isLoaded() ? PROCESS_TICKS.get() : 40;
    }

    public static boolean playChainsaw() {
        return !SPEC.isLoaded() || PLAY_CHAINSAW.get();
    }

    public static boolean automaticEquipment() {
        return !SPEC.isLoaded() || AUTOMATIC_EQUIPMENT.get();
    }

    public static List<? extends String> extraRecipes() {
        return SPEC.isLoaded() ? EXTRA_RECIPES.get() : List.of();
    }

    public static List<? extends String> disabledItems() {
        return SPEC.isLoaded() ? DISABLED_ITEMS.get() : List.of();
    }

    private static boolean validateRecipeLine(Object value) {
        return value instanceof String line && RECIPE_LINE.matcher(line).matches();
    }

    private static boolean validateItemId(Object value) {
        return value instanceof String line && ITEM_ID.matcher(line).matches();
    }
}
