package com.recyclerbench;

import com.mojang.logging.LogUtils;
import com.recyclerbench.block.ModBlockEntities;
import com.recyclerbench.block.ModBlocks;
import com.recyclerbench.menu.ModMenus;
import com.recyclerbench.sound.ModSounds;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import org.slf4j.Logger;

@Mod(RecyclerBenchMod.MOD_ID)
public class RecyclerBenchMod {
    public static final String MOD_ID = "recyclerbench";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RecyclerBenchMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlocks.CREATIVE_TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, RecyclerConfig.SPEC);
    }
}
