package com.tetra_loopback.effects.getter.rainstorm;

import se.mickelus.tetra.gui.stats.getter.ILabelGetter;

public class RainstormLabelGetter implements ILabelGetter {
    @Override
    public String getLabel(double value, double diffValue, boolean flipped) {
        int intValue = (int) Math.round(value);
        int intDiffValue = (int) Math.round(diffValue);

        if (intValue <= 0) {
            return "§7-";
        }

        //只显示等级
        String current = "§b" + intValue;

        if (intDiffValue <= 0 || intValue == intDiffValue) {
            return current;
        }

        //显示变化
        String next = "§b" + intDiffValue;
        return current + " §7→ " + next;
    }

    @Override
    public String getLabelMerged(double value, double diffValue) {
        return getLabel(value, diffValue, false);
    }
}