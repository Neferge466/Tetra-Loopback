package com.tetra_loopback.effects.gui.vision;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.curio.vision.VisionFieldClientEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class VisionFieldOverlayRegister {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("vision_field", VisionFieldClientEvents.overlay);
    }
}