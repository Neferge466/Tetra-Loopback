package com.tetra_loopback.effects.getter.lightningstrike;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;
import com.tetra_loopback.effects.gui.ModEffectStats;

public class LightningStrikeGetter implements IStatGetter {
    private final IStatGetter effectLevelGetter;

    public LightningStrikeGetter() {
        this.effectLevelGetter = new StatGetterEffectLevel(ModEffectStats.lightningStrikeEffect);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return hasLightningStrike(currentStack) || hasLightningStrike(previewStack);
    }

    private boolean hasLightningStrike(ItemStack itemStack) {
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