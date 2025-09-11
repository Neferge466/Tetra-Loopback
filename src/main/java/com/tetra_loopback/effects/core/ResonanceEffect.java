package com.tetra_loopback.effects.core;

import net.minecraft.client.resources.language.I18n;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import com.tetra_loopback.effects.getter.ResonanceGetter;
import com.tetra_loopback.effects.getter.ResonanceLabelGetter;
import com.tetra_loopback.effects.getter.ResonanceTooltipGetter;

public class ResonanceEffect {
    public static final ItemEffect RESONANCE = ItemEffect.get("tetra_loopback:resonance");
    public static final IStatGetter resonanceGetter = new ResonanceGetter();

    public static GuiStatBar getBar() {
        return new GuiStatBar(
                0, 0, 59, "tetra.stats.tetra_loopback:resonance",
                0, 20, false, true, true, // 设置为true以启用分段显示
                resonanceGetter, new ResonanceLabelGetter(),
                new ResonanceTooltipGetter()
        );
    }

    public static int getResonanceStage(double progress) {
        return (int) progress / 5; // 每5点为一个阶段，共4个阶段(0-3)
    }

    public static String getResonanceDisplayName(double progress) {
        var stage = getResonanceStage(progress);
        return I18n.get("tetra.stats.tetra_loopback:resonance.stage" + stage);
    }

    public static String getResonanceDisplayTooltip(double progress) {
        var stage = getResonanceStage(progress);
        return I18n.get("tetra.stats.tetra_loopback:resonance.stage" + stage + ".tooltip");
    }

    public static int getResonanceColor(double progress, double diffValue) {
        if (progress == diffValue) {
            return 0xFFFFFF; // 白色 - 无变化
        }

        int oldStage = getResonanceStage(progress);
        int newStage = getResonanceStage(diffValue);

        if (newStage > oldStage) {
            return 0x00FF00; // 绿色 - 提升
        } else if (newStage < oldStage) {
            return 0xFF0000; // 红色 - 降低
        } else {
            return 0xFFD700; // 金色 - 阶段内变化
        }
    }
}