package com.tetra_loopback.effects.getter.telluric;

import se.mickelus.tetra.gui.stats.getter.ILabelGetter;

public class TelluricAnchorLabelGetter implements ILabelGetter {
    @Override
    public String getLabel(double value, double diffValue, boolean flipped) {
        int intValue = (int) Math.round(value);

        if (intValue <= 0) {
            return "§7-";
        }

        return "§6" + intValue + "§r";
    }

    @Override
    public String getLabelMerged(double value, double diffValue) {
        return getLabel(value, diffValue, false);
    }
}