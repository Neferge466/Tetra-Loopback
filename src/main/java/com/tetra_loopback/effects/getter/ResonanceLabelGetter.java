package com.tetra_loopback.effects.getter;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;
import com.tetra_loopback.effects.gui.GuiBarQuadSegmented;

public class ResonanceLabelGetter implements ILabelGetter {
    @Override
    public String getLabel(double value, double diffValue, boolean flipped) {
        int intValue = (int) Math.round(value);
        int intDiffValue = (int) Math.round(diffValue);

        //获取阶段颜色
        String colorCode = getColorCode(GuiBarQuadSegmented.getStageColor(intValue));

        if (value == diffValue) {
            //显示当前值和阶段名称
            return colorCode + intValue + " (" + I18n.get(GuiBarQuadSegmented.getStageName(intValue)) + ")";
        }

        //显示变化
        String oldColorCode = getColorCode(GuiBarQuadSegmented.getStageColor(intValue));
        String newColorCode = getColorCode(GuiBarQuadSegmented.getStageColor(intDiffValue));

        return oldColorCode + intValue + " §7→ " + newColorCode + intDiffValue +
                " §7(" + oldColorCode + I18n.get(GuiBarQuadSegmented.getStageName(intValue)) +
                " §7→ " + newColorCode + I18n.get(GuiBarQuadSegmented.getStageName(intDiffValue)) + "§7)";
    }

    //颜色值转换
    private String getColorCode(int color) {
        if (color == 0xFF0000) return "§c";//红色
        if (color == 0xFF55FF) return "§5";//紫色
        if (color == 0x5555FF) return "§9";//蓝色
        if (color == 0x55FF55) return "§a";//绿色
        return "§7";//灰色
    }

    @Override
    public String getLabelMerged(double value, double diffValue) {
        return "diffvalue";
    }
}