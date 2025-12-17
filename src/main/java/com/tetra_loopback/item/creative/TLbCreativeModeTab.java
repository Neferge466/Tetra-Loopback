package com.tetra_loopback.item.creative;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.TLbRegistry;
import com.tetra_loopback.item.display.ScrollDisplayDefinitions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class TLbCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Tetra_loopback.MODID);

    public static final RegistryObject<CreativeModeTab> TLb_TAB = CREATIVE_MODE_TABS.register("tetra_loopback",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(TLbRegistry.LOOPBACK_CRYSTAL.get()))
                    .title(Component.translatable("creativetab.tetra_loopback"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(TLbRegistry.ANCIENT_FORGE.get());
                        pOutput.accept(TLbRegistry.CURIOS_EMBLEM.get());
                        pOutput.accept(TLbRegistry.CURIOS_GOGGLES.get());
                        pOutput.accept(TLbRegistry.EMPTY_CRYSTAL.get());
                        pOutput.accept(TLbRegistry.LOOPBACK_CRYSTAL.get());
                        pOutput.accept(TLbRegistry.VITALITY_CRYSTAL.get());
                        pOutput.accept(TLbRegistry.EARTH_VEIN_CRYSTAL.get());

                        pOutput.accept(TLbRegistry.BLOODY_STAR.get());
                        pOutput.accept(TLbRegistry.MISLEAD_STAR.get());

                        pOutput.accept(TLbRegistry.FUSE_STEEL_INGOT.get());
                        pOutput.accept(TLbRegistry.EARTH_VEIN_INGOT.get());
                        pOutput.accept(TLbRegistry.COLD_STEEL_INGOT.get());
                        pOutput.accept(TLbRegistry.VITALITY_INGOT.get());
                        pOutput.accept(TLbRegistry.COLD_WIND_INGOT.get());

                        pOutput.accept(TLbRegistry.RAINSTORM_PEARL.get());
                        pOutput.accept(TLbRegistry.LAVA_PEARL.get());
                        pOutput.accept(TLbRegistry.LIGHTNING_PEARL.get());
                        pOutput.accept(TLbRegistry.SNOW_PEARL.get());
                        pOutput.accept(TLbRegistry.FUSE_PEARL.get());



                        ScrollDisplayDefinitions.getAllDisplayScrolls().forEach(pOutput::accept);
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}