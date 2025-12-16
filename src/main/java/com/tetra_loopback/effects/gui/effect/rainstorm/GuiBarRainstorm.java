package com.tetra_loopback.effects.gui.effect.rainstorm;

import net.minecraft.client.gui.GuiGraphics;
import se.mickelus.tetra.gui.stats.bar.GuiBar;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiBarRainstorm extends GuiBar {
    private final int barLength;
    private final int fixedColor = 0x5555FF;

    public GuiBarRainstorm(int x, int y, int barLength, double min, double max, boolean invertedDiff) {
        super(x, y, barLength, min, max, invertedDiff);
        this.barLength = barLength;
    }

    @Override
    protected void calculateBarLengths() {
        super.calculateBarLengths();
        diffColor = fixedColor;
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
                    diffColor, opacity);
        }
    }
}