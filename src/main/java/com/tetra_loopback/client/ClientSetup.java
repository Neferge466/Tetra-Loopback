package com.tetra_loopback.client;

import com.tetra_loopback.TLbRegistry;
import com.tetra_loopback.client.renderer.ModularEmblemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@OnlyIn(Dist.CLIENT)
public class ClientSetup {

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::onClientSetup);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            //register renderer
            CuriosRendererRegistry.register(TLbRegistry.MODULAR_EMBLEM.get(), ModularEmblemRenderer::new);
        });
    }
}
