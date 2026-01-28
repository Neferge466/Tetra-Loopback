package com.tetra_loopback.effects.getter.vitalshield;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class VitalShieldTooltipGetter implements ITooltipGetter {

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return I18n.get("tetra.stats.tetra_loopback:vital_shield.tooltip.empty");
        }

        VitalShieldGetter getter = new VitalShieldGetter();
        double value = getter.getValue(player, itemStack);
        int level = (int) Math.round(value);

        StringBuilder tooltip = new StringBuilder();

        tooltip.append(I18n.get("tetra.stats.tetra_loopback:vital_shield.name"));
        tooltip.append(" §a❤§r");

        if (level > 0) {
            switch (level) {
                case 1:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.level1"));
                    break;
                case 2:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.level2"));
                    break;
                case 3:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.level3"));
                    break;
                case 4:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.level4"));
                    break;
                case 5:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.level5"));
                    break;
            }

            tooltip.append("\n\n§7").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.condition"));
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.refresh"));

            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.cooldown"));
        } else {
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.inactive"));
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

        extension.append("§6").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.extended_title")).append("§7:");

        extension.append("\n\n§7").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.level1"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.level2"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.level3"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.level4"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.level5"));

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.cooldown_title")).append("§7:");
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.cooldown_level1"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.cooldown_level2"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.cooldown_level3"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.cooldown_level4"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.cooldown_level5"));

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.mechanics_title")).append("§7:");
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.mechanic.trigger"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.mechanic.refresh"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.mechanic.upgrade"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:vital_shield.mechanic.cooldown"));

        return extension.toString();
    }
}