package com.tetra_loopback.effects.gui.effect.vitalshield;

import net.minecraft.client.gui.GuiGraphics;
import se.mickelus.tetra.gui.stats.bar.GuiBar;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiBarVitalShield extends GuiBar {
    private final int barLength;

    public GuiBarVitalShield(int x, int y, int barLength, double min, double max, boolean invertedDiff) {
        super(x, y, barLength, min, max, invertedDiff);
        this.barLength = barLength;
    }

    @Override
    protected void calculateBarLengths() {
        super.calculateBarLengths();
        diffColor = 0x4FA976;
    }

    @Override
    public void draw(GuiGraphics graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
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
                    0x4FA976, opacity);

            if (progressLength > 3) {
                drawRect(graphics,
                        refX + x + progressLength - 3,
                        refY + y + 6,
                        refX + x + progressLength,
                        refY + y + 6 + height,
                        0x7FDC9C, opacity * 0.8f);
            }
        }
    }
}