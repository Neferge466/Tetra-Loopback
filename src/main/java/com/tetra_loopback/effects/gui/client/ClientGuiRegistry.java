package com.tetra_loopback.effects.gui.client;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.*;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;

@OnlyIn(Dist.CLIENT)
public class ClientGuiRegistry {
    public static void registerAllBars() {
        registerThunderingBar();
        registerGuardianBar();
        registerAttributeBars();
        registerAttackSpeedBar();
        registerMovementSpeedBar();
        registerFlameAdmonitionBar();
        registerFrostCrownBar();
        registerEtherealGlowBar();
        registerRuneCreedBar();
    }



    private static void registerRuneCreedBar() {
        IStatGetter runeGetter = new StatGetterEffectLevel(ModEffectStats.runeCreedEffect, 1);
        GuiStatBar runeBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.rune_creed.name",
                0, 1,
                false, false, false,
                runeGetter,
                new ILabelGetter() {
                    @Override
                    public String getLabel(double value, double diffValue, boolean flipped) {
                        return value > 0 ? "✡" : "";
                    }

                    @Override
                    public String getLabelMerged(double v, double v1) {
                        return "";
                    }
                },
                new TooltipGetterDecimal(Tetra_loopback.MODID + ".effect.rune_creed.tooltip", runeGetter)
        );
        WorkbenchStatsGui.addBar(runeBar);
        HoloStatsGui.addBar(runeBar);
    }




    private static void registerEtherealGlowBar() {
        IStatGetter glowGetter = new StatGetterEffectLevel(ModEffectStats.etherealGlowEffect, 1);
        GuiStatBar glowBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.ethereal_glow.name",
                0, 1,
                false, false, false,
                glowGetter,
                new ILabelGetter() {
                    @Override
                    public String getLabel(double value, double diffValue, boolean flipped) {
                        return value > 0 ? "✨" : "";
                    }

                    @Override
                    public String getLabelMerged(double value, double diffValue) {
                        return "";
                    }
                },
                new TooltipGetterDecimal(Tetra_loopback.MODID + ".effect.ethereal_glow.tooltip", glowGetter)
        );
        WorkbenchStatsGui.addBar(glowBar);
        HoloStatsGui.addBar(glowBar);
    }

    private static void registerFrostCrownBar() {
        IStatGetter frostGetter = new StatGetterEffectLevel(ModEffectStats.frostCrownEffect, 1);
        GuiStatBar frostBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.frost_crown.name",
                0, 1,
                false, false, false,
                frostGetter,
                new ILabelGetter() {
                    @Override
                    public String getLabel(double value, double diffValue, boolean flipped) {
                        return value > 0 ? "❄" : "";
                    }

                    @Override
                    public String getLabelMerged(double value, double diffValue) {
                        return "";
                    }
                },
                new TooltipGetterDecimal(Tetra_loopback.MODID + ".effect.frost_crown.tooltip", frostGetter)
        );
        WorkbenchStatsGui.addBar(frostBar);
        HoloStatsGui.addBar(frostBar);
    }


    private static void registerFlameAdmonitionBar() {
        IStatGetter flameGetter = new StatGetterEffectLevel(ModEffectStats.flameAdmonitionEffect, 1);
        GuiStatBar flameBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.flame_admonition.name",
                0, 1,
                false, false, false,
                flameGetter,
                new ILabelGetter() {
                    @Override
                    public String getLabel(double value, double diffValue, boolean flipped) {
                        return value > 0 ? "✧" : "";
                    }

                    @Override
                    public String getLabelMerged(double value, double diffValue) {
                        return "";
                    }
                },
                new TooltipGetterDecimal(Tetra_loopback.MODID + ".effect.flame_admonition.tooltip", flameGetter)
        );
        WorkbenchStatsGui.addBar(flameBar);
        HoloStatsGui.addBar(flameBar);
    }



    private static void registerThunderingBar() {
        IStatGetter thunderingGetter = new StatGetterEffectLevel(ModEffectStats.thunderingEffect, 1);
        ITooltipGetter thunderingTooltip = new TooltipGetterInteger(
                Tetra_loopback.MODID + ".effect.thundering.tooltip",
                thunderingGetter);

        GuiStatBar thunderingBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.thundering.name",
                0, 10,
                false, false, false,
                thunderingGetter,
                LabelGetterBasic.integerLabel,
                thunderingTooltip);

        WorkbenchStatsGui.addBar(thunderingBar);
        HoloStatsGui.addBar(thunderingBar);
    }

    private static void registerGuardianBar() {
        IStatGetter guardianGetter = new StatGetterEffectLevel(ModEffectStats.guardianEffect, 1);
        GuiStatBar guardianBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.guardian.name",
                0, 10,
                false, false, false,
                guardianGetter,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(Tetra_loopback.MODID + ".effect.guardian.tooltip", guardianGetter)
        );

        WorkbenchStatsGui.addBar(guardianBar);
        HoloStatsGui.addBar(guardianBar);
    }

    private static void registerAttributeBars() {
        registerAttributeBar(
                ModEffectStats.armorEffect,
                Tetra_loopback.MODID + ".effect.armor.name",
                Tetra_loopback.MODID + ".effect.armor.tooltip",
                new StatGetterEffectLevel(ModEffectStats.armorEffect, 1)
        );

        registerAttributeBar(
                ModEffectStats.armorToughnessEffect,
                Tetra_loopback.MODID + ".effect.armor_toughness.name",
                Tetra_loopback.MODID + ".effect.armor_toughness.tooltip",
                new StatGetterEffectLevel(ModEffectStats.armorToughnessEffect, 1)
        );

        registerAttributeBar(
                ModEffectStats.attackDamageEffect,
                Tetra_loopback.MODID + ".effect.attack_damage.name",
                Tetra_loopback.MODID + ".effect.attack_damage.tooltip",
                new StatGetterEffectLevel(ModEffectStats.attackDamageEffect, 1)
        );

        registerAttributeBar(
                ModEffectStats.attackKnockbackEffect,
                Tetra_loopback.MODID + ".effect.attack_knockback.name",
                Tetra_loopback.MODID + ".effect.attack_knockback.tooltip",
                new StatGetterEffectLevel(ModEffectStats.attackKnockbackEffect, 1)
        );

        registerAttributeBar(
                ModEffectStats.flyingSpeedEffect,
                Tetra_loopback.MODID + ".effect.flying_speed.name",
                Tetra_loopback.MODID + ".effect.flying_speed.tooltip",
                new StatGetterEffectLevel(ModEffectStats.flyingSpeedEffect, 1)
        );

        registerAttributeBar(
                ModEffectStats.followRangeEffect,
                Tetra_loopback.MODID + ".effect.follow_range.name",
                Tetra_loopback.MODID + ".effect.follow_range.tooltip",
                new StatGetterEffectLevel(ModEffectStats.followRangeEffect, 1)
        );

        registerAttributeBar(
                ModEffectStats.knockbackResistanceEffect,
                Tetra_loopback.MODID + ".effect.knockback_resistance.name",
                Tetra_loopback.MODID + ".effect.knockback_resistance.tooltip",
                new StatGetterEffectLevel(ModEffectStats.knockbackResistanceEffect, 1)
        );

        registerAttributeBar(
                ModEffectStats.luckEffect,
                Tetra_loopback.MODID + ".effect.luck.name",
                Tetra_loopback.MODID + ".effect.luck.tooltip",
                new StatGetterEffectLevel(ModEffectStats.luckEffect, 1)
        );

        registerAttributeBar(
                ModEffectStats.maxHealthEffect,
                Tetra_loopback.MODID + ".effect.max_health.name",
                Tetra_loopback.MODID + ".effect.max_health.tooltip",
                new StatGetterEffectLevel(ModEffectStats.maxHealthEffect, 1)
        );
    }

    private static void registerAttackSpeedBar() {
        IStatGetter attackSpeedGetter = new StatGetterEffectLevel(ModEffectStats.attackSpeedEffect, 1);
        GuiStatBar attackSpeedBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.attack_speed.name",
                -10, 10,
                false, false, false,
                attackSpeedGetter,
                new ILabelGetter() {
                    @Override
                    public String getLabel(double value, double diffValue, boolean flipped) {
                        return String.format("%+d", (int) value);
                    }

                    @Override
                    public String getLabelMerged(double value, double diffValue) {
                        return "";
                    }
                },
                new TooltipGetterInteger(Tetra_loopback.MODID + ".effect.attack_speed.tooltip", attackSpeedGetter)
        );
        WorkbenchStatsGui.addBar(attackSpeedBar);
        HoloStatsGui.addBar(attackSpeedBar);
    }

    private static void registerMovementSpeedBar() {
        IStatGetter movementSpeedGetter = new StatGetterEffectLevel(ModEffectStats.movementSpeedEffect, 1);
        GuiStatBar movementSpeedBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.movement_speed.name",
                -10, 10,
                false, false, false,
                movementSpeedGetter,
                new ILabelGetter() {
                    @Override
                    public String getLabel(double value, double diffValue, boolean flipped) {
                        return String.format("%+d", (int) value);
                    }

                    @Override
                    public String getLabelMerged(double value, double diffValue) {
                        return "";
                    }
                },
                new TooltipGetterInteger(Tetra_loopback.MODID + ".effect.movement_speed.tooltip", movementSpeedGetter)
        );
        WorkbenchStatsGui.addBar(movementSpeedBar);
        HoloStatsGui.addBar(movementSpeedBar);
    }

    private static void registerAttributeBar(ItemEffect effect, String nameKey, String tooltipKey, IStatGetter getter) {
        GuiStatBar bar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                nameKey,
                0, 10,
                false, false, false,
                getter,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(tooltipKey, getter)
        );
        WorkbenchStatsGui.addBar(bar);
        HoloStatsGui.addBar(bar);
    }
}
