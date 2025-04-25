package com.tetra_loopback.item.creative;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.TLbRegistry;
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
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(TLbRegistry.LOOPBACK_ITEM.get()))
                    .title(Component.translatable("creativetab.tetra_loopback"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(TLbRegistry.CURIOS_EMBLEM.get());
                        pOutput.accept(TLbRegistry.INK_FROZEN.get());
                        pOutput.accept(TLbRegistry.INK_RUNE.get());
                        pOutput.accept(TLbRegistry.INK_BURN.get());
                        pOutput.accept(TLbRegistry.INK_TWINKLE.get());
                        pOutput.accept(TLbRegistry.BLOODY_STAR.get());
                        pOutput.accept(TLbRegistry.MISLEAD_STAR.get());

                        pOutput.accept(TLbRegistry.LIGHTNING_INGOT.get());
                        pOutput.accept(TLbRegistry.LIGHTNING_INGOT_GRIT.get());
                        pOutput.accept(TLbRegistry.FUSE_STEEL_INGOT.get());
                        pOutput.accept(TLbRegistry.ROUND_RUBY.get());
                        pOutput.accept(TLbRegistry.LIGHTNING_LOGO.get());




                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}