package com.tetra_loopback.effects.gui.effect.windlight;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.*;

public class GuiStatBarWindLight extends GuiStatBar {
    public GuiStatBarWindLight(int x, int y, int barLength, String label, double min, double max,
                               boolean invertedDiff, boolean segmented, boolean split,
                               IStatGetter statGetter, ILabelGetter labelGetter, ITooltipGetter tooltipGetter) {
        super(x, y, barLength, label, min, max, invertedDiff, segmented, split, statGetter, labelGetter, tooltipGetter);

        this.bar = new GuiBarWindLight(0, 0, barLength, min, max, invertedDiff);
        this.bar.setAlignment(alignment);
        addChild(this.bar);
    }

    @Override
    public void update(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        super.update(player, currentStack, previewStack, slot, improvement);
        boolean shouldShow = statGetter.shouldShow(player, currentStack, previewStack);
        setVisible(shouldShow);
        bar.setVisible(shouldShow);
    }
}