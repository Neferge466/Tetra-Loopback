package com.tetra_loopback.effects.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;
import com.tetra_loopback.effects.gui.GuiBarQuadSegmented;

public class ResonanceTooltipGetter implements ITooltipGetter {
    @Override
    public String getTooltip(Player player, ItemStack itemStack) {
        return ITooltipGetter.super.getTooltip(player, itemStack);
    }

    @Override
    public String getTooltipExtended(Player player, ItemStack itemStack) {
        return ITooltipGetter.super.getTooltipExtended(player, itemStack);
    }

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        ResonanceGetter getter = new ResonanceGetter();
        double value = getter.getValue(player, itemStack);
        int intValue = (int) Math.round(value);

        //获取阶段颜色
        String colorCode = getColorCode(GuiBarQuadSegmented.getStageColor(intValue));

        return colorCode + intValue + " - " + I18n.get(GuiBarQuadSegmented.getStageName(intValue));
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        ResonanceGetter getter = new ResonanceGetter();
        double value = getter.getValue(player, itemStack);
        int intValue = (int) Math.round(value);

        //阶段显示提示
        if (intValue >= 16) {
            return I18n.get("tetra.stats.tetra_loopback:resonance.stage4.tooltip");
        } else if (intValue >= 11) {
            return I18n.get("tetra.stats.tetra_loopback:resonance.stage3.tooltip");
        } else if (intValue >= 6) {
            return I18n.get("tetra.stats.tetra_loopback:resonance.stage2.tooltip");
        } else if (intValue >= 1) {
            return I18n.get("tetra.stats.tetra_loopback:resonance.stage1.tooltip");
        } else {
            return I18n.get("tetra.stats.tetra_loopback:resonance.stage0.tooltip");
        }
    }

    //颜色值转换
    private String getColorCode(int color) {
        if (color == 0xFF0000) return "§c"; // 红色
        if (color == 0xFF55FF) return "§5"; // 紫色
        if (color == 0x5555FF) return "§9"; // 蓝色
        if (color == 0x55FF55) return "§a"; // 绿色
        return "§7"; // 灰色
    }
}