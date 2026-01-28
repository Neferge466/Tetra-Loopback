package com.tetra_loopback.effects.getter.primordial;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class PrimalLeechTooltipGetter implements ITooltipGetter {

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return I18n.get("tetra.stats.tetra_loopback:primal_leech.tooltip.empty");
        }

        PrimalLeechGetter getter = new PrimalLeechGetter();
        double value = getter.getValue(player, itemStack);

        StringBuilder tooltip = new StringBuilder();

        tooltip.append("§c").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.name"));

        if (value > 0) {
            tooltip.append(" §c✴§r ");
        }

        tooltip.append("\n§7").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.desc"));

        return tooltip.toString();
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        StringBuilder extension = new StringBuilder();

        extension.append("§6").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.tooltip.core_effects")).append("§7:");

        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.effect.leech"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.effect.low_hp"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.effect.kill"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.effect.reflect"));

        extension.append("\n\n§a").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.tooltip.resonance_effects")).append("§7:");

        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.resonance.stage1")).append(": ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.effect.stage1"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.resonance.stage2")).append(": ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.effect.stage2.1"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.resonance.stage3")).append(": ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.effect.stage3.1"));
        extension.append("\n§7• ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.resonance.stage4")).append(": ").append(I18n.get("tetra.stats.tetra_loopback:primal_leech.effect.stage4.1"));

        return extension.toString();
    }
}