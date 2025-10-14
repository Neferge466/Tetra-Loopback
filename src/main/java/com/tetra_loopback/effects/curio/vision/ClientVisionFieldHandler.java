package com.tetra_loopback.effects.curio.vision;

import com.tetra_loopback.effects.gui.vision.VisionFieldOverlay;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientVisionFieldHandler {
    private static VisionFieldOverlay overlay = new VisionFieldOverlay();
    private static int effectLevel = 0;

    public static void updateEffectLevel(int newLevel, Player player) {
        effectLevel = newLevel;
        overlay.updateEffectLevel(newLevel);
        overlay.updatePlayerData(player);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        //在客户端tick中更新覆盖层数据
        if (event.phase == TickEvent.Phase.END) {
            //更新覆盖层的数据
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        //渲染覆盖层
        if (effectLevel > 0) {
            overlay.render(null, event.getGuiGraphics(), event.getPartialTick(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
        }
    }
}