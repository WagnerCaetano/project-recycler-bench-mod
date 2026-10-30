package com.recyclerbench.client;

import com.recyclerbench.RecyclerBenchMod;
import com.recyclerbench.menu.ModMenus;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = RecyclerBenchMod.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = RecyclerBenchMod.MOD_ID, value = Dist.CLIENT)
public class RecyclerBenchClient {
    public RecyclerBenchClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.RECYCLER_BENCH.get(), RecyclerScreen::new);
    }
}
