package com.tetra_loopback.effects.getter.vitalshield;

import net.minecraft.client.resources.language.I18n;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;

public class VitalShieldLabelGetter implements ILabelGetter {
    @Override
    public String getLabel(double value, double diffValue, boolean flipped) {
        int intValue = (int) Math.round(value);

        if (intValue <= 0) {
            return "§7-";
        }

        String current = "§a❤§r";

        int intDiffValue = (int) Math.round(diffValue);
        if (intDiffValue <= 0 || intValue == intDiffValue) {
            return current;
        }

        String next = "§a❤§r";
        return current + " §7→ " + next;
    }

    @Override
    public String getLabelMerged(double value, double diffValue) {
        return getLabel(value, diffValue, false);
    }
}