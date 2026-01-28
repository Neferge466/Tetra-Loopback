package com.tetra_loopback.block.ancientforge.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.block.ancientforge.inventory.AncientForgeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;

import java.util.ArrayList;
import java.util.List;

public class AncientForgeScreen extends AbstractContainerScreen<AncientForgeMenu> {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Tetra_loopback.MODID, "textures/gui/ancient_forge.png");
    private static final ResourceLocation PLAYER_INVENTORY_TEXTURE =
            new ResourceLocation("tetra", "textures/gui/player-inventory.png");

    //燃烧条常量
    private static final int BURN_X = 91;
    private static final int BURN_Y = 55;
    private static final int BURN_WIDTH = 10;
    private static final int BURN_HEIGHT = 18;

    //燃烧条帧的纹理坐标
    private static final int[][] BURN_FRAMES = {
            {176, 16},
            {192, 16},
            {208, 16},
            {224, 16},
            {240, 16},
            {176, 38}
    };

    //进度条常量
    private static final int CRAFT_X = 84;
    private static final int CRAFT_Y = 40;
    private static final int CRAFT_WIDTH = 23;
    private static final int CRAFT_HEIGHT = 8;

    //进度条帧的纹理坐标
    private static final int[][] CRAFT_FRAMES = {
            {230, 80},
            {203, 80},
            {176, 80},
            {230, 69},
            {203, 69},
            {176, 69}
    };

    //动画相关
    private final List<GuiElement> animatedElements = new ArrayList<>();
    private GuiElement backgroundElement;
    private GuiElement playerInventoryElement;

    private int lastBurnTime = -1;
    private int lastBurnTimeTotal = -1;
    private int lastCraftTime = -1;
    private int lastCraftTimeTotal = -1;
    private boolean wasBurning = false;

    private boolean animationsInitialized = false;

    public AncientForgeScreen(AncientForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelY = 2;
        this.inventoryLabelY = this.imageHeight - 100;
    }

    @Override
    protected void init() {
        super.init();
        initAnimationElements();
    }

    private void initAnimationElements() {
        if (animationsInitialized) return;

        backgroundElement = new GuiElement(0, 0, imageWidth, imageHeight - 60) {
            @Override
            public void draw(GuiGraphics gui, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
            }
        };
        animatedElements.add(backgroundElement);

        playerInventoryElement = new GuiElement(0, 116, 172, 98) {
            @Override
            public void draw(GuiGraphics gui, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
            }
        };
        animatedElements.add(playerInventoryElement);

        startAnimations();
        animationsInitialized = true;
    }

    private void startAnimations() {
        for (GuiElement element : animatedElements) {
            element.setOpacity(0);
        }

        new KeyframeAnimation(300, backgroundElement)
                .withDelay(0)
                .applyTo(new Applier.Opacity(0, 1))
                .start();

        new KeyframeAnimation(300, playerInventoryElement)
                .withDelay(100)
                .applyTo(new Applier.Opacity(0, 1))
                .start();
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;

        //更新所有动画元素
        for (GuiElement element : animatedElements) {
            element.updateAnimations();
        }

        //绘制主背景
        float bgOpacity = backgroundElement != null ? backgroundElement.getOpacity() : 1.0f;
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, bgOpacity);
        gui.blit(TEXTURE, guiLeft, guiTop, 0, 0, imageWidth, imageHeight - 60, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        //绘制物品栏背景
        float piOpacity = playerInventoryElement != null ? playerInventoryElement.getOpacity() : 1.0f;
        RenderSystem.setShaderTexture(0, PLAYER_INVENTORY_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, piOpacity);
        gui.blit(PLAYER_INVENTORY_TEXTURE, guiLeft, guiTop + 116, 0, 0, 172, 98, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        //获取当前燃烧和合成数据
        int burnTime = menu.getBurnTime();
        int burnTimeTotal = menu.getBurnTimeTotal();
        int craftTime = menu.getCraftTime();
        int craftTimeTotal = menu.getCraftTimeTotal();

        if (burnTime > 0 && burnTimeTotal > 0) {
            float progress = Math.min(1.0f, (float) burnTime / burnTimeTotal);
            int frameIndex = calculateBurnFrameIndex(progress);

            if (frameIndex >= 0 && frameIndex < BURN_FRAMES.length) {
                int[] frameCoords = BURN_FRAMES[frameIndex];
                int u = frameCoords[0];
                int v = frameCoords[1];

                gui.blit(
                        TEXTURE,
                        guiLeft + BURN_X, guiTop + BURN_Y,
                        u, v,
                        BURN_WIDTH, BURN_HEIGHT,
                        256, 256
                );
            }
        }

        if (craftTime > 0 && craftTimeTotal > 0) {
            float progress = Math.min(1.0f, (float) craftTime / craftTimeTotal);
            int frameIndex = calculateCraftFrameIndex(progress);

            if (frameIndex >= 0 && frameIndex < CRAFT_FRAMES.length) {
                int[] frameCoords = CRAFT_FRAMES[frameIndex];
                int u = frameCoords[0];
                int v = frameCoords[1];

                gui.blit(
                        TEXTURE,
                        guiLeft + CRAFT_X, guiTop + CRAFT_Y,
                        u, v,
                        CRAFT_WIDTH, CRAFT_HEIGHT,
                        256, 256
                );
            }
        }

        //保存当前状态
        lastBurnTime = burnTime;
        lastBurnTimeTotal = burnTimeTotal;
        lastCraftTime = craftTime;
        lastCraftTimeTotal = craftTimeTotal;
    }

    private int calculateBurnFrameIndex(float progress) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));

        float inverseProgress = 1.0f - progress;
        int frameIndex = (int) (inverseProgress * 6);

        if (frameIndex >= 6) frameIndex = 5;
        if (frameIndex < 0) frameIndex = 0;

        return frameIndex;
    }

    private int calculateCraftFrameIndex(float progress) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));

        int frameIndex = (int) (progress * 6);

        if (frameIndex >= 6) frameIndex = 5;
        if (frameIndex < 0) frameIndex = 0;

        return frameIndex;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);

        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;

        //燃烧条提示
        if (isHovering(BURN_X, BURN_Y, BURN_WIDTH, BURN_HEIGHT, mouseX, mouseY)) {
            int burnTime = menu.getBurnTime();
            int burnTimeTotal = menu.getBurnTimeTotal();
            if (burnTime > 0 && burnTimeTotal > 0) {
                gui.renderTooltip(font,
                        Component.translatable("gui.tetra_loopback.ancient_forge.burn_time",
                                burnTime / 20, burnTimeTotal / 20),
                        mouseX, mouseY);
            }
        }

        //合成进度提示
        if (isHovering(CRAFT_X, CRAFT_Y, CRAFT_WIDTH, CRAFT_HEIGHT, mouseX, mouseY)) {
            int craftTime = menu.getCraftTime();
            int craftTimeTotal = menu.getCraftTimeTotal();
            if (craftTime > 0 && craftTimeTotal > 0) {
                int progress = (int) ((float) craftTime / craftTimeTotal * 100);
                gui.renderTooltip(font,
                        Component.translatable("gui.tetra_loopback.ancient_forge.craft_progress", progress),
                        mouseX, mouseY);
            }
        }

        renderSlotTooltips(gui, mouseX, mouseY, guiLeft, guiTop);
    }

    private void renderSlotTooltips(GuiGraphics gui, int mouseX, int mouseY, int guiLeft, int guiTop) {
        //原料槽 (3x3)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = guiLeft + 12 + col * 17;
                int slotY = guiTop + 15 + row * 17;
                if (isHovering(slotX - guiLeft, slotY - guiTop, 16, 16, mouseX, mouseY)) {
                    int slotIndex = row * 3 + col;
                    Slot slot = this.menu.getSlot(slotIndex);
                    if (slot.getItem().isEmpty()) {
                        gui.renderTooltip(this.font,
                                Component.translatable("tooltip.tetra_loopback.material_slot"),
                                mouseX, mouseY);
                        return;
                    }
                }
            }
        }

        //催化剂槽
        if (isHovering(76, 15, 16, 16, mouseX, mouseY)) {
            Slot slot = this.menu.getSlot(9);
            if (slot.getItem().isEmpty()) {
                gui.renderTooltip(this.font,
                        Component.translatable("tooltip.tetra_loopback.catalyst_slot"),
                        mouseX, mouseY);
                return;
            }
        }

        //能量源槽
        if (isHovering(88, 80, 16, 16, mouseX, mouseY)) {
            Slot slot = this.menu.getSlot(10);
            if (slot.getItem().isEmpty()) {
                gui.renderTooltip(this.font,
                        Component.translatable("tooltip.tetra_loopback.energy_slot"),
                        mouseX, mouseY);
                return;
            }
        }

        //主产物槽
        int slotX = guiLeft + 136;
        int slotY = guiTop + 15;
        if (isHovering(slotX - guiLeft, slotY - guiTop, 16, 16, mouseX, mouseY)) {
            Slot slot = this.menu.getSlot(11);
            if (slot.getItem().isEmpty()) {
                gui.renderTooltip(this.font,
                        Component.translatable("tooltip.tetra_loopback.main_output_slot"),
                        mouseX, mouseY);
                return;
            }
        }

        //副产物槽
        slotX = guiLeft + 136;
        slotY = guiTop + 60;
        if (isHovering(slotX - guiLeft, slotY - guiTop, 16, 16, mouseX, mouseY)) {
            Slot slot = this.menu.getSlot(12);
            if (slot.getItem().isEmpty()) {
                gui.renderTooltip(this.font,
                        Component.translatable("tooltip.tetra_loopback.side_output_slot"),
                        mouseX, mouseY);
                return;
            }
        }

        //返还原料槽
        slotX = guiLeft + 12;
        slotY = guiTop + 72;
        if (isHovering(slotX - guiLeft, slotY - guiTop, 16, 16, mouseX, mouseY)) {
            Slot slot = this.menu.getSlot(13);
            if (slot.getItem().isEmpty()) {
                gui.renderTooltip(this.font,
                        Component.translatable("tooltip.tetra_loopback.return_slot"),
                        mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);
        int textX = this.inventoryLabelX;
        int textY = 117 - 10;
        gui.drawString(this.font, this.playerInventoryTitle, textX, textY, 0xFFFFFF, false);
    }
}