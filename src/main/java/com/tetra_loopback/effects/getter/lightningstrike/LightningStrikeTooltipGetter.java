package com.tetra_loopback.effects.getter.lightningstrike;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class LightningStrikeTooltipGetter implements ITooltipGetter {

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return I18n.get("tetra.stats.tetra_loopback:lightning_strike.tooltip.empty");
        }

        LightningStrikeGetter getter = new LightningStrikeGetter();
        double value = getter.getValue(player, itemStack);
        int level = (int) Math.round(value);

        StringBuilder tooltip = new StringBuilder();

        tooltip.append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.name"));
        tooltip.append(" §b⚡§r");

        if (level > 0) {
            switch (level) {
                case 1:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.level1"));
                    break;
                case 2:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.level2"));
                    break;
                case 3:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.level3"));
                    break;
                case 4:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.level4"));
                    break;
                case 5:
                    tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.level5"));
                    break;
            }

            tooltip.append("\n\n§7").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.condition"));
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.probability"));
        } else {
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.inactive"));
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

        extension.append("§b").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.extended_title")).append("§7:");

        extension.append("\n\n§7").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.level1"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.level2"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.level3"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.level4"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.level5"));

        extension.append("\n\n§b").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.mechanics_title")).append("§7:");
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.mechanic.proc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.mechanic.damage"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.mechanic.glow"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:lightning_strike.mechanic.slowness"));

        return extension.toString();
    }
}