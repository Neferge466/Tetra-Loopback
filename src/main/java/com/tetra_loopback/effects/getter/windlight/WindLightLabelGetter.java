package com.tetra_loopback.effects.getter.windlight;

import se.mickelus.tetra.gui.stats.getter.ILabelGetter;

public class WindLightLabelGetter implements ILabelGetter {
    @Override
    public String getLabel(double value, double diffValue, boolean flipped) {
        int intValue = (int) Math.round(value);
        int intDiffValue = (int) Math.round(diffValue);

        if (intValue <= 0) {
            return "§7-";
        }

        int displayValue = Math.min(intValue, 2);
        String current = "§b" + displayValue;

        if (intDiffValue <= 0 || intValue == intDiffValue) {
            return current;
        }

        int displayDiff = Math.min(intDiffValue, 2);
        String next = "§b" + displayDiff;
        return current + " §7→ " + next;
    }

    @Override
    public String getLabelMerged(double value, double diffValue) {
        return getLabel(value, diffValue, false);
    }
}