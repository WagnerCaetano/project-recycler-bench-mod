package com.recyclerbench.block;

import com.recyclerbench.RecyclerBenchMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RecyclerBenchMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RecyclerBlockEntity>> RECYCLER_BENCH =
            BLOCK_ENTITIES.register("recycler_bench", () -> BlockEntityType.Builder
                    .of(RecyclerBlockEntity::new, ModBlocks.RECYCLER_BENCH.get())
                    .build(null));

    private ModBlockEntities() {
    }
}
