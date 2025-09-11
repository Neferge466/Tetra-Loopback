package com.tetra_loopback.effects.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class GuiStatBarQuadSegmented extends GuiStatBar {
    public GuiStatBarQuadSegmented(int x, int y, int barLength, String label, double min, double max,
                                   boolean invertedDiff, boolean segmented, boolean split,
                                   IStatGetter statGetter, ILabelGetter labelGetter, ITooltipGetter tooltipGetter) {
        super(x, y, barLength, label, min, max, invertedDiff, segmented, split, statGetter, labelGetter, tooltipGetter);

        //替换默认的条形为自定义条形
        this.bar = new GuiBarQuadSegmented(0, 0, barLength, min, max, invertedDiff);
        this.bar.setAlignment(alignment);
        addChild(this.bar);
    }

    @Override
    public void update(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        super.update(player, currentStack, previewStack, slot, improvement);

        //确保条形可见性正确
        boolean shouldShow = statGetter.shouldShow(player, currentStack, previewStack);
        setVisible(shouldShow);
        bar.setVisible(shouldShow);
    }
}