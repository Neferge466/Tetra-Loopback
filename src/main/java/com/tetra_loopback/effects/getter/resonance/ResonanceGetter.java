package com.tetra_loopback.effects.getter.resonance;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;
import com.tetra_loopback.effects.gui.ModEffectStats;

public class ResonanceGetter implements IStatGetter {
    private final IStatGetter effectLevelGetter;

    public ResonanceGetter() {
        this.effectLevelGetter = new StatGetterEffectLevel(ModEffectStats.resonanceEffect);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return hasResonance(player, currentStack) || hasResonance(player, previewStack);
    }

    private boolean hasResonance(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        if (itemStack.hasTag() && itemStack.getTag().contains("Resonance")) {
            return true;
        }
        double effectLevel = effectLevelGetter.getValue(player, itemStack);
        if (effectLevel > 0) {
            return true;
        }
        return false;
    }



    public double getResonanceValue(Player player, ItemStack itemStack) {

        double nbtValue = getNbtResonanceValue(itemStack);

        double effectLevel = effectLevelGetter.getValue(player, itemStack);

        double combinedValue = nbtValue + effectLevel;

        return Mth.clamp(combinedValue, 0, 20);
    }

    private double getNbtResonanceValue(ItemStack itemStack) {
        if (itemStack.hasTag() && itemStack.getTag().contains("Resonance")) {
            var data = itemStack.getTag().getCompound("Resonance");
            if (!data.contains("value") || itemStack.isDamaged()) {
                return 0;
            }
            double ResonanceValue = data.getDouble("value");
            return ResonanceValue;
        }
        return 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return getResonanceValue(player, itemStack);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String s) {
        return getResonanceValue(player, itemStack);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String s, String s1) {
        return getResonanceValue(player, itemStack);
    }
}