package com.tetra_loopback.effects.getter.supercooling;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;
import com.tetra_loopback.effects.gui.ModEffectStats;

public class SupercoolingGetter implements IStatGetter {
    private final IStatGetter effectLevelGetter;

    public SupercoolingGetter() {
        this.effectLevelGetter = new StatGetterEffectLevel(ModEffectStats.supercoolingEffect);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return hasSupercooling(currentStack) || hasSupercooling(previewStack);
    }

    private boolean hasSupercooling(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        double effectLevel = effectLevelGetter.getValue(null, itemStack);
        return effectLevel > 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return 1.0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return 1.0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return 1.0;
    }
}