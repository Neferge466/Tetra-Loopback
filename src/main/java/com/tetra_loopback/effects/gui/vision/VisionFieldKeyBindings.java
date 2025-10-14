package com.tetra_loopback.effects.gui.vision;

import com.mojang.blaze3d.platform.InputConstants;
import com.tetra_loopback.Tetra_loopback;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class VisionFieldKeyBindings {


    //Type和keyCode的构造函数
     public static final KeyMapping TOGGLE_VISION_OVERLAY = new KeyMapping(
             "key.tetra_loopback.toggle_vision_overlay",
             InputConstants.Type.KEYSYM, //按键类型
             GLFW.GLFW_KEY_RIGHT_ALT, //按键码
             "key.categories.tetra_loopback"
     );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_VISION_OVERLAY);
    }
}