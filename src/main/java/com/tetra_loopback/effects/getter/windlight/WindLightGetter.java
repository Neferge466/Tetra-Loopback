package com.tetra_loopback.effects.getter.windlight;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;
import com.tetra_loopback.effects.gui.ModEffectStats;

public class WindLightGetter implements IStatGetter {
    private final IStatGetter effectLevelGetter;

    public WindLightGetter() {
        this.effectLevelGetter = new StatGetterEffectLevel(ModEffectStats.windLightEffect);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return hasWindLight(currentStack) || hasWindLight(previewStack);
    }

    private boolean hasWindLight(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        double effectLevel = effectLevelGetter.getValue(null, itemStack);
        return effectLevel > 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        double value = effectLevelGetter.getValue(player, itemStack);
        return Math.min(value, 2);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        double value = effectLevelGetter.getValue(player, itemStack, slot);
        return Math.min(value, 2);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        double value = effectLevelGetter.getValue(player, itemStack, slot, improvement);
        return Math.min(value, 2);
    }
}