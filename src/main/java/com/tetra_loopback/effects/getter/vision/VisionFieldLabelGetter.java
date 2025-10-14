package com.tetra_loopback.effects.getter.vision;

import se.mickelus.tetra.gui.stats.getter.ILabelGetter;

public class VisionFieldLabelGetter implements ILabelGetter {
    @Override
    public String getLabel(double value, double diffValue, boolean flipped) {
        int intValue = (int) Math.round(value);
        int intDiffValue = (int) Math.round(diffValue);


        String colorCode = "§6"; //金色

        if (value == diffValue) {
            return colorCode + intValue + " §7/ 5";
        }

        return colorCode + intValue + " §7→ " + colorCode + intDiffValue + " §7/ 5";
    }

    @Override
    public String getLabelMerged(double value, double diffValue) {
        return getLabel(value, diffValue, false);
    }
}