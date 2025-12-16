package com.tetra_loopback.sound;

import com.tetra_loopback.Tetra_loopback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TLbSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Tetra_loopback.MODID);

    // 护目镜声音
    public static final RegistryObject<SoundEvent> GOGGLES_EQUIP =
            registerSoundEvent("equip_goggles");
    public static final RegistryObject<SoundEvent> GOGGLES_UNEQUIP =
            registerSoundEvent("unequip_goggles");

//    // 徽章声音
//    public static final RegistryObject<SoundEvent> EMBLEM_EQUIP =
//            registerSoundEvent("equip_emblem");
//    public static final RegistryObject<SoundEvent> EMBLEM_UNEQUIP =
//            registerSoundEvent("unequip_emblem");
//
    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation location = new ResourceLocation(Tetra_loopback.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(location));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}