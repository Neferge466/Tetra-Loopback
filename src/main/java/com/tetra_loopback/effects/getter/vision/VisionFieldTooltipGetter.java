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
        int currentLevel = (int) Math.round(value);
        StringBuilder extension = new StringBuilder();
        //显示所有等级的效果
        for (int level = 5; level >= 1; level--) {
            String levelKey = "tetra.stats.tetra_loopback:vision_field.tooltip.level" + level;

            if (level == currentLevel) {
                //当前高亮
                extension.append("§b").append(I18n.get("tetra.stats.tetra_loopback:vision_field.level" + level))
                        .append(": ").append(I18n.get(levelKey)).append(" §b\n\n");
            } else {
                extension.append("§7").append(I18n.get("tetra.stats.tetra_loopback:vision_field.level" + level))
                        .append(": ").append(I18n.get(levelKey)).append("\n\n");
            }
        }

        if (extension.length() > 0) {
            extension.setLength(extension.length() - 2);
        }

        return extension.toString();
    }
}