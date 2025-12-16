package com.tetra_loopback.effects.getter.rainstorm;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class RainstormTooltipGetter implements ITooltipGetter {

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return I18n.get("tetra.stats.tetra_loopback:rainstorm.tooltip.empty");
        }

        RainstormGetter getter = new RainstormGetter();
        double value = getter.getValue(player, itemStack);
        int rainstormLevel = (int) Math.round(value);

        StringBuilder tooltip = new StringBuilder();

        //暴雨等级
        tooltip.append("§b").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.name"));

        if (rainstormLevel > 0) {
            tooltip.append(" ").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.level" + rainstormLevel));

            //基础效果
            tooltip.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.tooltip.base_effects")).append("§7:");

            // 根据等级显示效果
            if (rainstormLevel >= 1) {
                tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.effect.speed"));
            }
            if (rainstormLevel >= 2) {
                tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.effect.jump"));
            }
            if (rainstormLevel >= 3) {
                tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.effect.resistance"));
            }

            // 激活条件
            tooltip.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.tooltip.conditions")).append("§7:");
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.condition.rain"));

            // 显示效果持续时间
            tooltip.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.effect.duration"));

        } else {
            tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.inactive"));
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

        // 共鸣系统说明
        extension.append("§6").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.tooltip.resonance_system")).append("§7:")
                .append("\n").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.tooltip.resonance_desc"));

        // 共鸣阶段效果
        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.tooltip.resonance_stages")).append("§7:");

        // 阶段1-4的详细效果
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.resonance.stage1_desc"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.resonance.stage2_desc"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.resonance.stage3_desc"));
        extension.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.resonance.stage4_desc"));

        // 阶段4额外说明
        extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.tooltip.stage4_note")).append("§7:")
                .append("\n").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.stage4.detail"));

        // 如果玩家存在，可以显示当前状态
        if (player != null) {
            extension.append("\n\n§6").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.tooltip.current_status")).append("§7:");
            boolean isRaining = player.level().isRaining();
            String weatherStatus = isRaining ?
                    "§a" + I18n.get("tetra.stats.tetra_loopback:rainstorm.status.raining") :
                    "§7" + I18n.get("tetra.stats.tetra_loopback:rainstorm.status.not_raining");
            extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:rainstorm.status.weather", weatherStatus));
        }

        return extension.toString();
    }
}