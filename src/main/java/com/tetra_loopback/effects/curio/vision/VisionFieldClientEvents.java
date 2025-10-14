package com.tetra_loopback.effects.curio.vision;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.curio.vision.VisionFieldEffect;
import com.tetra_loopback.effects.gui.vision.VisionFieldKeyBindings;
import com.tetra_loopback.effects.gui.vision.VisionFieldOverlay;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class VisionFieldClientEvents {
    public static VisionFieldOverlay overlay = new VisionFieldOverlay();

    @SubscribeEvent
    public static void onClientTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (player.level().isClientSide) {
            int effectLevel = VisionFieldEffect.getPlayerEffectLevel(player);

            if (effectLevel > 0) {
                overlay.updateEffectLevel(effectLevel);
                overlay.updatePlayerData(player);
            } else {
                overlay.updateEffectLevel(0);
            }
        }
    }

    //处理按键输入
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        //检查是否按下了切换覆盖层的按键
        if (VisionFieldKeyBindings.TOGGLE_VISION_OVERLAY.consumeClick()) {
            overlay.toggleVisibility();
        }
    }
}