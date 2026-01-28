package com.tetra_loopback.effects.getter.telluric;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class TelluricAnchorTooltipGetter implements ITooltipGetter {
    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return I18n.get("tetra.stats.tetra_loopback:telluric_anchor.tooltip.empty");
        }

        TelluricAnchorGetter getter = new TelluricAnchorGetter();
        double value = getter.getValue(player, itemStack);

        StringBuilder tooltip = new StringBuilder();

        tooltip.append("§6").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.name"));

        tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.tooltip.core_desc"));

        if (value > 0) {
            tooltip.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.tooltip.effects")).append("§7:");

            //根据等级显示
            int level = (int) value;
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.effect.gravity_anchor"));
        } else {
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.inactive"));
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

        extension.append("§6").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.tooltip.resonance_stages")).append("§7:");

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.resonance.stage1_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.stage1.none"));

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.resonance.stage2_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.stage2.gravity_anchor"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.stage2.ground_only"));

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.resonance.stage3_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.stage3.kinetic_dissipation"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.stage3.knockback_aoe"));

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.resonance.stage4_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.stage4.earth_empowerment"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.stage4.hunger_regen"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:telluric_anchor.stage4.fall_reduction"));

        return extension.toString();
    }
}