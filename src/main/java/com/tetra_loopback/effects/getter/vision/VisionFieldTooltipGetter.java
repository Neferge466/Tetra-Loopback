package com.tetra_loopback.effects.getter.vision;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class VisionFieldTooltipGetter implements ITooltipGetter {
    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        VisionFieldGetter getter = new VisionFieldGetter();
        double value = getter.getValue(player, itemStack);
        int intValue = (int) Math.round(value);

        return "§6" + intValue + " §7/ 5 - " + I18n.get("tetra.stats.tetra_loopback:vision_field.level");
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        VisionFieldGetter getter = new VisionFieldGetter();
        double value = getter.getValue(player, itemStack);
        int intValue = (int) Math.round(value);

        switch (intValue) {
            case 5:
                return I18n.get("tetra.stats.tetra_loopback:vision_field.tooltip.level5");
            case 4:
                return I18n.get("tetra.stats.tetra_loopback:vision_field.tooltip.level4");
            case 3:
                return I18n.get("tetra.stats.tetra_loopback:vision_field.tooltip.level3");
            case 2:
                return I18n.get("tetra.stats.tetra_loopback:vision_field.tooltip.level2");
            case 1:
                return I18n.get("tetra.stats.tetra_loopback:vision_field.tooltip.level1");
            default:
                return I18n.get("tetra.stats.tetra_loopback:vision_field.tooltip.level0");
        }
    }
}