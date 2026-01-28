package com.tetra_loopback.effects.getter.supercooling;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class SupercoolingTooltipGetter implements ITooltipGetter {

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return I18n.get("tetra.stats.tetra_loopback:supercooling.tooltip.empty");
        }

        SupercoolingGetter getter = new SupercoolingGetter();
        double value = getter.getValue(player, itemStack);

        StringBuilder tooltip = new StringBuilder();

        tooltip.append("§3").append(I18n.get("tetra.stats.tetra_loopback:supercooling.name"));
        tooltip.append(" ").append("§3❄§r");

        if (value > 0) {
            tooltip.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:supercooling.tooltip.core")).append("§7:");
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:supercooling.effect.cooldown_overfill"));
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:supercooling.effect.conditional_damage"));

            tooltip.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:supercooling.tooltip.activation")).append("§7:");
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:supercooling.condition.cooldown_full"));
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:supercooling.condition.wait"));
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:supercooling.condition.attack"));
        } else {
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:supercooling.inactive"));
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

        extension.append("§6").append(I18n.get("tetra.stats.tetra_loopback:supercooling.tooltip.resonance_stages")).append("§7:");
        extension.append("\n").append(I18n.get("tetra.stats.tetra_loopback:supercooling.resonance.note"));

        //阶段1
        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:supercooling.resonance.stage1_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:supercooling.stage1.damage"));

        //阶段2
        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:supercooling.resonance.stage2_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:supercooling.stage2.interrupt"));

        //阶段3
        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:supercooling.resonance.stage3_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:supercooling.stage3.armor_conversion"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:supercooling.stage3.weakness"));

        //阶段4
        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:supercooling.resonance.stage4_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:supercooling.stage4.knockback"));

        return extension.toString();
    }
}