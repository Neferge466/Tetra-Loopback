package com.tetra_loopback.effects.curio.supercooling.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = "tetra_loopback", value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class SupercoolingOverlayRenderer {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        //检查准星渲染
        if (event.getOverlay().equals(VanillaGuiOverlay.CROSSHAIR.type())) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;

            ClientSupercoolingData.PlayerSupercoolState state = ClientSupercoolingData.getLocalPlayerState();

            if (state == null) {
                return;
            }
            if (!state.isActive) {
                return;
            }

            float attackStrength = player.getAttackStrengthScale(0.0F);
            if (attackStrength < 0.9f) return;

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            int hungerBarWidth = 81;
            int rightAreaX = screenWidth / 2 + 91;
            int hungerBarY = screenHeight - 39;

            int x = rightAreaX - hungerBarWidth;
            int y = hungerBarY - 12;

            float supercoolProgress = state.progress;

            renderSupercoolingBar(event.getGuiGraphics(), x, y, supercoolProgress, state.resonanceStage, hungerBarWidth);
        }
    }

    private static void renderSupercoolingBar(GuiGraphics guiGraphics, int x, int y, float supercoolProgress, int resonanceStage, int barWidth) {
        if (supercoolProgress <= 0) return;

        //启用混合
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int color = getColorForStage(resonanceStage);

        //计算进度条长度
        int progressLength = (int) (barWidth * supercoolProgress);

        //背景矩形
        drawColoredRect(guiGraphics, x, y, barWidth, 8, 0x66000000);

        //边框
        drawColoredRect(guiGraphics, x, y, barWidth, 1, 0x99FFFFFF); //上边框
        drawColoredRect(guiGraphics, x, y + 7, barWidth, 1, 0x99FFFFFF); //下边框
        drawColoredRect(guiGraphics, x, y, 1, 8, 0x99FFFFFF); //左边框
        drawColoredRect(guiGraphics, x + barWidth - 1, y, 1, 8, 0x99FFFFFF); //右边框

        //进度条（从左向右）
        if (progressLength > 0) {
            //主进度
            drawColoredRect(guiGraphics, x + 1, y + 1, progressLength, 6, color);

            //内部高光
            int highlightWidth = Math.max(0, progressLength - 2);
            int highlightColor = color & 0x00FFFFFF | 0x66000000; //添加高光
            drawColoredRect(guiGraphics, x + 2, y + 2, highlightWidth, 2, highlightColor);
        }

        //闪烁
        if (supercoolProgress >= 1.0f) {
            float alpha = (float) (0.5f + 0.5f * Math.sin(System.currentTimeMillis() / 200.0));
            int flashColor = color & 0x00FFFFFF | ((int)(alpha * 255) << 24);

            //绘制闪烁边框
            drawColoredRect(guiGraphics, x, y, barWidth, 1, flashColor); //上边框
            drawColoredRect(guiGraphics, x, y + 7, barWidth, 1, flashColor); //下边框
            drawColoredRect(guiGraphics, x, y, 1, 8, flashColor); //左边框
            drawColoredRect(guiGraphics, x + barWidth - 1, y, 1, 8, flashColor); //右边框

            //绘制闪烁内部
            int innerFlashColor = flashColor & 0x00FFFFFF | 0x44000000;
            drawColoredRect(guiGraphics, x + 1, y + 1, barWidth - 2, 6, innerFlashColor);
        }

        RenderSystem.disableBlend();
    }

    private static int getColorForStage(int stage) {
        return switch (stage) {
            case 1 -> 0xDD66CCFF; //浅蓝
            case 2 -> 0xDD3399FF; //蓝
            case 3 -> 0xDD0066CC; //深蓝
            case 4 -> 0xDD003399; //蓝紫
            default -> 0xDD66CCFF; //默认,浅蓝
        };
    }

    private static void drawColoredRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) return;

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();

        bufferbuilder.vertex(matrix4f, x, y + height, 0.0F).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix4f, x + width, y, 0.0F).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix4f, x, y, 0.0F).color(r, g, b, a).endVertex();

        tesselator.end();
        RenderSystem.disableBlend();
    }
}