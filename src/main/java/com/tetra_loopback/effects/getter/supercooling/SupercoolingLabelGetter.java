package com.tetra_loopback.effects.getter.supercooling;

import se.mickelus.tetra.gui.stats.getter.ILabelGetter;

public class SupercoolingLabelGetter implements ILabelGetter {
    @Override
    public String getLabel(double value, double diffValue, boolean flipped) {
        int intValue = (int) Math.round(value);
        int intDiffValue = (int) Math.round(diffValue);

        if (intValue <= 0) {
            return "§7-";
        }

        String current = "§3❄§r";

        if (intDiffValue <= 0 || intValue == intDiffValue) {
            return current;
        }

        String next = "§3❄§r";
        return current + " §7→ " + next;
    }

    @Override
    public String getLabelMerged(double value, double diffValue) {
        return getLabel(value, diffValue, false);
    }
}