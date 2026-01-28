package com.tetra_loopback.effects.getter.windlight;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class WindLightTooltipGetter implements ITooltipGetter {

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return I18n.get("tetra.stats.tetra_loopback:wind_light.tooltip.empty");
        }

        WindLightGetter getter = new WindLightGetter();
        double value = getter.getValue(player, itemStack);
        int windLightLevel = (int) Math.round(value);

        StringBuilder tooltip = new StringBuilder();

        tooltip.append("§b").append(I18n.get("tetra.stats.tetra_loopback:wind_light.name"));

        if (windLightLevel > 0) {
            tooltip.append(" ").append(I18n.get("tetra.stats.tetra_loopback:wind_light.level" + Math.min(windLightLevel, 2)));

            tooltip.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:wind_light.tooltip.base_effects")).append("§7:");

            if (windLightLevel >= 1) {
                tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:wind_light.effect.speed"));
            }
            if (windLightLevel >= 2) {
                tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:wind_light.effect.jump"));
            }

            tooltip.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:wind_light.tooltip.conditions")).append("§7:");
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:wind_light.condition.always"));
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:wind_light.effect.duration"));

        } else {
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:wind_light.inactive"));
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

        extension.append("§6").append(I18n.get("tetra.stats.tetra_loopback:wind_light.tooltip.resonance_system")).append("§7:")
                .append("\n").append(I18n.get("tetra.stats.tetra_loopback:wind_light.tooltip.resonance_desc"));

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:wind_light.tooltip.resonance_stages")).append("§7:");

        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:wind_light.resonance.stage1_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:wind_light.resonance.stage2_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:wind_light.resonance.stage3_desc"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:wind_light.resonance.stage4_desc"));

        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:wind_light.tooltip.stage4_note")).append("§7:")
                .append("\n").append(I18n.get("tetra.stats.tetra_loopback:wind_light.stage4.detail"));

        return extension.toString();
    }
}