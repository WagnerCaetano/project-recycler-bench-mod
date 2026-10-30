package com.recyclerbench.sound;

import com.recyclerbench.RecyclerBenchMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, RecyclerBenchMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> CHAINSAW = SOUND_EVENTS.register("chainsaw",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(RecyclerBenchMod.MOD_ID, "chainsaw")));

    private ModSounds() {
    }
}
