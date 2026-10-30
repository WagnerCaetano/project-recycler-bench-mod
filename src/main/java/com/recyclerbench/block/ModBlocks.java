package com.recyclerbench.block;

import com.recyclerbench.RecyclerBenchMod;
import com.recyclerbench.item.RecyclerBenchItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RecyclerBenchMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RecyclerBenchMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RecyclerBenchMod.MOD_ID);

    public static final DeferredBlock<RecyclerBlock> RECYCLER_BENCH = BLOCKS.register("recycler_bench",
            () -> new RecyclerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(RecyclerBlock.LIT) ? 7 : 0)));

    public static final DeferredItem<RecyclerBenchItem> RECYCLER_BENCH_ITEM = ITEMS.register("recycler_bench",
            () -> new RecyclerBenchItem(RECYCLER_BENCH.get(), new Item.Properties()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.recyclerbench"))
                    .icon(() -> new ItemStack(RECYCLER_BENCH_ITEM.get()))
                    .displayItems((parameters, output) -> output.accept(RECYCLER_BENCH_ITEM.get()))
                    .build());

    private ModBlocks() {
    }
}
