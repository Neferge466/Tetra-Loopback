package com.tetra_loopback.effects;

import com.tetra_loopback.Tetra_loopback;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TLbEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,
                    Tetra_loopback.MODID);


    public static final RegistryObject<MobEffect> THUNDER =
            MOB_EFFECTS.register("thunder",ThunderEffect::new);


    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }
}