package com.recyclerbench.menu;

import com.recyclerbench.RecyclerBenchMod;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RecyclerBenchMod.MOD_ID);

    public static final RegistryObject<MenuType<RecyclerMenu>> RECYCLER_BENCH = MENUS.register("recycler_bench",
            () -> IForgeMenuType.create(RecyclerMenu::new));

    private ModMenus() {
    }
}
