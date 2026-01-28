package com.tetra_loopback.effects.gui.effect.lightningstrike;

import net.minecraft.client.gui.GuiGraphics;
import se.mickelus.tetra.gui.stats.bar.GuiBar;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiBarLightningStrike extends GuiBar {
    private final int barLength;

    public GuiBarLightningStrike(int x, int y, int barLength, double min, double max, boolean invertedDiff) {
        super(x, y, barLength, min, max, invertedDiff);
        this.barLength = barLength;
    }

    @Override
    protected void calculateBarLengths() {
        super.calculateBarLengths();
        diffColor = 0x4addf6;
    }

    @Override
    public void draw(GuiGraphics graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        int progressLength = (int) ((value / (max - min)) * barLength);

        drawRect(graphics,
                refX + x,
                refY + y + 6,
                refX + x + barLength,
                refY + y + 6 + height,
                0x0066cc, opacity * 0.5f);

        if (progressLength > 0) {
            for (int i = 0; i < progressLength; i++) {
                float ratio = (float) i / progressLength;
                int color = interpolateColor(0x0066cc, 0x4addf6, ratio);

                drawRect(graphics,
                        refX + x + i,
                        refY + y + 6,
                        refX + x + i + 1,
                        refY + y + 6 + height,
                        color, opacity);
            }

            if (progressLength > 2) {
                for (int i = 0; i < progressLength; i += 6) {
                    int flashPos = Math.min(i, progressLength - 2);
                    drawRect(graphics,
                            refX + x + flashPos,
                            refY + y + 6,
                            refX + x + flashPos + 2,
                            refY + y + 6 + height,
                            0xaaddff, opacity * 0.6f);
                }
            }
        }
    }

    private int interpolateColor(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int)(r1 + (r2 - r1) * ratio);
        int g = (int)(g1 + (g2 - g1) * ratio);
        int b = (int)(b1 + (b2 - b1) * ratio);

        return (r << 16) | (g << 8) | b;
    }
}