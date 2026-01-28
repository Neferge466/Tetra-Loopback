package com.tetra_loopback.effects.getter.lifeessence;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;
import com.tetra_loopback.effects.gui.ModEffectStats;

public class LifeEssenceGetter implements IStatGetter {
    private final IStatGetter effectLevelGetter;

    public LifeEssenceGetter() {
        this.effectLevelGetter = new StatGetterEffectLevel(ModEffectStats.lifeEssenceEffect);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return hasLifeEssence(currentStack) || hasLifeEssence(previewStack);
    }

    private boolean hasLifeEssence(ItemStack itemStack) {
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