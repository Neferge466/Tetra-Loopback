package com.tetra_loopback.effects.getter.earthenshield;

import se.mickelus.tetra.gui.stats.getter.ILabelGetter;

public class EarthenShieldLabelGetter implements ILabelGetter {
    @Override
    public String getLabel(double value, double diffValue, boolean flipped) {
        int intValue = (int) Math.round(value);
        int intDiffValue = (int) Math.round(diffValue);

        if (intValue <= 0) {
            return "§7-";
        }

        String current = "§6⛰§r " + intValue;

        if (intDiffValue <= 0 || intValue == intDiffValue) {
            return current;
        }

        String next = "§6⛰§r " + intDiffValue;
        return current + " §7→ " + next;
    }

    @Override
    public String getLabelMerged(double value, double diffValue) {
        return getLabel(value, diffValue, false);
    }
}