package com.tetra_loopback.effects.getter.primordial;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;
import com.tetra_loopback.effects.gui.ModEffectStats;

public class PrimalLeechGetter implements IStatGetter {
    private final IStatGetter effectLevelGetter;

    public PrimalLeechGetter() {
        this.effectLevelGetter = new StatGetterEffectLevel(ModEffectStats.primalLeechEffect, 1);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return hasPrimalLeech(currentStack) || hasPrimalLeech(previewStack);
    }

    private boolean hasPrimalLeech(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;
        double effectLevel = effectLevelGetter.getValue(null, itemStack);
        return effectLevel > 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return effectLevelGetter.getValue(player, itemStack);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return effectLevelGetter.getValue(player, itemStack, slot);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return effectLevelGetter.getValue(player, itemStack, slot, improvement);
    }
}