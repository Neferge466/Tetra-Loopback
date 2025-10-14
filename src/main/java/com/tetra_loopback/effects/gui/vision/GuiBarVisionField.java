package com.tetra_loopback.effects.gui.vision;

import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiBarVisionField extends se.mickelus.tetra.gui.stats.bar.GuiBar {
    private final int maxValue = 5;
    private int currentValue;
    private int barLength;

    public GuiBarVisionField(int x, int y, int barLength, double min, double max, boolean invertedDiff) {
        super(x, y, barLength, min, max, invertedDiff);
        this.barLength = barLength;
    }

    @Override
    protected void calculateBarLengths() {
        currentValue = (int) Math.round(value);

        //浅金色到深金色渐变
        if (currentValue >= 5) {
            diffColor = 0xFFD700; //亮金色
        } else if (currentValue >= 4) {
            diffColor = 0xFFC125; //金色
        } else if (currentValue >= 3) {
            diffColor = 0xFFB90F; //中金色
        } else if (currentValue >= 2) {
            diffColor = 0xFFA500; //橙金色
        } else if (currentValue >= 1) {
            diffColor = 0xFF8C00; //深金色
        } else {
            diffColor = 0x555555; //灰色
        }
    }

    @Override
    public void draw(GuiGraphics graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        int progressLength = (int) ((currentValue / 5.0) * barLength);
        //绘制背景
        drawRect(graphics,
                refX + x,
                refY + y + 6,
                refX + x + barLength,
                refY + y + 6 + height,
                0x333333, opacity * 0.5f);
        //绘制进度条
        if (progressLength > 0) {
            int colorWithAlpha = (diffColor & 0x00FFFFFF) | ((int)(opacity * 255) << 24);
            drawRect(graphics,
                    refX + x,
                    refY + y + 6,
                    refX + x + progressLength,
                    refY + y + 6 + height,
                    diffColor, opacity);
        }
    }

    //文本
    public static int getLevelColor(int value) {
        if (value >= 5) return 0xFFD700;
        else if (value >= 4) return 0xFFC125;
        else if (value >= 3) return 0xFFB90F;
        else if (value >= 2) return 0xFFA500;
        else if (value >= 1) return 0xFF8C00;
        else return 0x555555;
    }
}