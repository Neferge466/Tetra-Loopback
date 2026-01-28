package com.tetra_loopback.effects.getter.lifeessence;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class LifeEssenceTooltipGetter implements ITooltipGetter {

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return I18n.get("tetra.stats.tetra_loopback:life_essence.tooltip.empty");
        }

        LifeEssenceGetter getter = new LifeEssenceGetter();
        double value = getter.getValue(player, itemStack);

        StringBuilder tooltip = new StringBuilder();

        tooltip.append("§a").append(I18n.get("tetra.stats.tetra_loopback:life_essence.name"));
        tooltip.append(" ").append("§a❤§r");

        if (value > 0) {
            tooltip.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:life_essence.tooltip.core")).append("§7:");
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:life_essence.effect.natural_healing"));
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:life_essence.effect.debuff_reduction"));
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:life_essence.effect.overflow_compensation"));
        } else {
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:life_essence.inactive"));
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

        extension.append("§6").append(I18n.get("tetra.stats.tetra_loopback:life_essence.tooltip.resonance_stages")).append("§7:");
        extension.append("\n").append(I18n.get("tetra.stats.tetra_loopback:life_essence.resonance.note"));

        //阶段1
        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:life_essence.resonance.stage1_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:life_essence.stage1.healing"));

        //阶段2
        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:life_essence.resonance.stage2_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:life_essence.stage2.natural_blocks"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:life_essence.stage2.overflow_start"));

        //阶段3
        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:life_essence.resonance.stage3_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:life_essence.stage3.enhanced_healing"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:life_essence.stage3.enhanced_overflow"));

        //阶段4
        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:life_essence.resonance.stage4_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:life_essence.stage4.duration"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:life_essence.stage4.food_restoration"));

        return extension.toString();
    }
}