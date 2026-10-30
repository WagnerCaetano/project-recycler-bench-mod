package com.recyclerbench.sound;

import com.recyclerbench.RecyclerBenchMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RecyclerBenchMod.MOD_ID);

    public static final RegistryObject<SoundEvent> CHAINSAW = SOUND_EVENTS.register("chainsaw",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(RecyclerBenchMod.MOD_ID, "chainsaw")));

    private ModSounds() {
    }
}
