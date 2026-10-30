package com.recyclerbench;

import com.mojang.logging.LogUtils;
import com.recyclerbench.block.ModBlockEntities;
import com.recyclerbench.block.ModBlocks;
import com.recyclerbench.menu.ModMenus;
import com.recyclerbench.sound.ModSounds;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

@Mod(RecyclerBenchMod.MOD_ID)
public class RecyclerBenchMod {
    public static final String MOD_ID = "recyclerbench";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RecyclerBenchMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlocks.CREATIVE_TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        context.registerConfig(ModConfig.Type.COMMON, RecyclerConfig.SPEC);
    }
}
