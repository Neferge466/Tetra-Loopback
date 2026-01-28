package com.tetra_loopback.effects.getter.astralphase;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class AstralPhaseTooltipGetter implements ITooltipGetter {

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return I18n.get("tetra.stats.tetra_loopback:astral_phase.tooltip.empty");
        }

        AstralPhaseGetter getter = new AstralPhaseGetter();
        double value = getter.getValue(player, itemStack);

        StringBuilder tooltip = new StringBuilder();

        tooltip.append("§5").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.name"));
        tooltip.append(" ").append("§5★§r");

        if (value > 0) {
            tooltip.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.tooltip.core")).append("§7:");
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.effect.phase_dodge"));
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.effect.starlight_agility"));

            tooltip.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.tooltip.details")).append("§7:");
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.detail.base_chance"));
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.detail.speed_bonus"));
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.detail.attack_cancel"));
        } else {
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.inactive"));
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

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.tooltip.mechanics")).append("§7:");
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.mechanic.cooldown"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.mechanic.push_physics"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.mechanic.fall_reset"));

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.tooltip.note")).append("§7:");
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.note.bypass_damage"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:astral_phase.note.status_effects"));

        return extension.toString();
    }
}