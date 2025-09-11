package com.tetra_loopback.effects.getter;

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
        return this.getValue(player, currentStack) != 0 || this.getValue(player, previewStack) != 0;
    }

    public double getResonanceValue(Player player, ItemStack itemStack) {
        //检查NBT中是否有固定的共鸣值
        double nbtValue = getNbtResonanceValue(itemStack);

        //然后获取正常的effectlevel值
        double effectLevel = effectLevelGetter.getValue(player, itemStack);

        //将两个值结合（根据需要调整逻辑）
        double combinedValue = nbtValue + effectLevel;

        //将结果限制在0-20范围内（用于Resonance效果显示）
        return Mth.clamp(combinedValue, 0, 20);
    }

    private double getNbtResonanceValue(ItemStack itemStack) {
        //检查NBT中是否有Resonance数据
        if (itemStack.hasTag() && itemStack.getTag().contains("Resonance")) {
            var data = itemStack.getTag().getCompound("Resonance");
            if (!data.contains("value") || itemStack.isDamaged()) {
                return 0;
            }
            //
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