package com.recyclerbench.block;

import com.recyclerbench.RecyclerBenchMod;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RecyclerBenchMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<RecyclerBlockEntity>> RECYCLER_BENCH =
            BLOCK_ENTITIES.register("recycler_bench", () -> BlockEntityType.Builder
                    .of(RecyclerBlockEntity::new, ModBlocks.RECYCLER_BENCH.get())
                    .build(null));

    private ModBlockEntities() {
    }
}
