package com.tetra_loopback.effects.gui.effect.supercooling;

import net.minecraft.client.gui.GuiGraphics;
import se.mickelus.tetra.gui.stats.bar.GuiBar;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiBarSupercooling extends GuiBar {
    private final int barLength;

    public GuiBarSupercooling(int x, int y, int barLength, double min, double max, boolean invertedDiff) {
        super(x, y, barLength, min, max, invertedDiff);
        this.barLength = barLength;
    }

    @Override
    protected void calculateBarLengths() {
        super.calculateBarLengths();
        diffColor = 0x3366CC; //冰蓝色
    }

    @Override
    public void draw(GuiGraphics graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        int progressLength = (int) ((value / (max - min)) * barLength);

        //绘制背景
        drawRect(graphics,
                refX + x,
                refY + y + 6,
                refX + x + barLength,
                refY + y + 6 + height,
                0x333333, opacity * 0.5f);

        if (progressLength > 0) {
            //绘制进度条
            drawRect(graphics,
                    refX + x,
                    refY + y + 6,
                    refX + x + progressLength,
                    refY + y + 6 + height,
                    diffColor, opacity);

            //绘制高光效果
            if (progressLength > 2) {
                drawRect(graphics,
                        refX + x + progressLength - 2,
                        refY + y + 6,
                        refX + x + progressLength,
                        refY + y + 6 + height,
                        0x88CCFF, opacity * 0.8f);
            }
        }
    }
}