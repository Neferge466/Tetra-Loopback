package com.tetra_loopback.effects.gui.effect.earthenshield;

import net.minecraft.client.gui.GuiGraphics;
import se.mickelus.tetra.gui.stats.bar.GuiBar;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiBarEarthenShield extends GuiBar {
    private final int barLength;

    public GuiBarEarthenShield(int x, int y, int barLength, double min, double max, boolean invertedDiff) {
        super(x, y, barLength, min, max, invertedDiff);
        this.barLength = barLength;
    }

    @Override
    protected void calculateBarLengths() {
        super.calculateBarLengths();
        diffColor = 0xCC9966; // 土黄色
    }

    @Override
    public void draw(GuiGraphics graphics, int refX, int refY, int screenWidth, int screenHeight,
                     int mouseX, int mouseY, float opacity) {
        int progressLength = (int) ((value / (max - min)) * barLength);

        drawRect(graphics,
                refX + x,
                refY + y + 6,
                refX + x + barLength,
                refY + y + 6 + height,
                0x333333, opacity * 0.5f);

        if (progressLength > 0) {
            drawRect(graphics,
                    refX + x,
                    refY + y + 6,
                    refX + x + progressLength,
                    refY + y + 6 + height,
                    diffColor, opacity);

            if (progressLength > 3) {
                for (int i = 0; i < progressLength; i += 4) {
                    if (i + 2 <= progressLength) {
                        int spotColor = 0x664422;
                        drawRect(graphics,
                                refX + x + i,
                                refY + y + 6 + 1,
                                refX + x + i + 2,
                                refY + y + 6 + height - 1,
                                spotColor, opacity * 0.7f);
                    }
                }

                if (progressLength > 2) {
                    drawRect(graphics,
                            refX + x + progressLength - 2,
                            refY + y + 6,
                            refX + x + progressLength,
                            refY + y + 6 + height,
                            0xFFEE99, opacity * 0.6f);
                }
            }
        }
    }
}