package com.tetra_loopback.effects.getter.rainstorm;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;
import com.tetra_loopback.effects.gui.ModEffectStats;

public class RainstormGetter implements IStatGetter {
    private final IStatGetter effectLevelGetter;

    public RainstormGetter() {
        this.effectLevelGetter = new StatGetterEffectLevel(ModEffectStats.rainstormEffect);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return hasRainstorm(currentStack) || hasRainstorm(previewStack);
    }

    private boolean hasRainstorm(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
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