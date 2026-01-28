package com.tetra_loopback.effects.getter.earthenshield;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class EarthenShieldTooltipGetter implements ITooltipGetter {

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return I18n.get("tetra.stats.tetra_loopback:earthen_shield.tooltip.empty");
        }

        EarthenShieldGetter getter = new EarthenShieldGetter();
        double value = getter.getValue(player, itemStack);
        int level = (int) Math.round(value);

        StringBuilder tooltip = new StringBuilder();

        tooltip.append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.name"));
        tooltip.append(" §6⛰§r");

        if (level > 0) {
            switch (level) {
                case 1:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.level1"));
                    break;
                case 2:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.level2"));
                    break;
                case 3:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.level3"));
                    break;
                case 4:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.level4"));
                    break;
                case 5:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.level5"));
                    break;
            }

            tooltip.append("\n\n§7").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.condition"));
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.buffer"));
        } else {
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.inactive"));
        }

        return tooltip.toString();
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        StringBuilder extension = new StringBuilder();

        extension.append("§6").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.extended_title")).append("§7:");

        extension.append("\n\n§7").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.level1"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.level2"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.level3"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.level4"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.level5"));

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.mechanics_title")).append("§7:");
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.mechanic.active"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.mechanic.buffer_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.mechanic.aura"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:earthen_shield.mechanic.stack"));

        return extension.toString();
    }
}