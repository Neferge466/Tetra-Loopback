package com.tetra_loopback.effects.gui.client;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.*;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;



@OnlyIn(Dist.CLIENT)
public class ClientGuiRegistry {
    public static void registerAllBars() {
        registerThunderingBar();
        registerGuardianBar();
        registerFlameAdmonitionBar();
        registerFrostCrownBar();
        registerEtherealGlowBar();
        registerRuneCreedBar();
        registerCrimsonScourgeBar();
        registerStrifeBar();

        registerAttackKnockbackBar();
        registerAttackFlyingSpeedBar();
        registerFollowRangeBar();
        registerKnockbackResistanceBar();
        registerLuckBar();
        registerMaxHealthBar();
        registerMovementSpeedBar();
    }


    private static void registerCrimsonScourgeBar() {
        IStatGetter scourgeGetter = new StatGetterEffectLevel(ModEffectStats.crimsonScourgeEffect, 1);
        GuiStatBar scourgeBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.crimson_scourge.name",
                0, 1,
                false, false, false,
                scourgeGetter,
                new ILabelGetter() {
                    @Override
                    public String getLabel(double value, double diffValue, boolean flipped) {
                        return value > 0 ? "ǝ" : "";
                    }

                    @Override
                    public String getLabelMerged(double v, double v1) {
                        return "";
                    }
                },
                new TooltipGetterDecimal(Tetra_loopback.MODID + ".effect.crimson_scourge.tooltip", scourgeGetter)
        );


        WorkbenchStatsGui.addBar(scourgeBar);
        HoloStatsGui.addBar(scourgeBar);
    }


    private static void registerStrifeBar() {
        IStatGetter strifeGetter = new StatGetterEffectLevel(ModEffectStats.strifeEffect, 1);
        GuiStatBar strifeBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.strife.name",
                0, 1,
                false, false, false,
                strifeGetter,
                new ILabelGetter() {
                    @Override
                    public String getLabel(double value, double diffValue, boolean flipped) {
                        return value > 0 ? "Ƶ" : "";
                    }

                    @Override
                    public String getLabelMerged(double v, double v1) {
                        return "";
                    }
                },
                new TooltipGetterDecimal(Tetra_loopback.MODID + ".effect.strife.tooltip", strifeGetter)
        );
        WorkbenchStatsGui.addBar(strifeBar);
        HoloStatsGui.addBar(strifeBar);
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

    private static void registerAttackKnockbackBar() {
        ResourceLocation res = new ResourceLocation("minecraft:generic.attack_knockback");
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(res);
        if (attribute == null) {
            return;
        }
        IStatGetter getter = new StatGetterAttribute(attribute, false);
        GuiStatBar bar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".attribute.attack_knockback.name",
                0, 10,
                false, false, false,
                getter,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(Tetra_loopback.MODID + ".attribute.attack_knockback.tooltip", getter)
        );
        WorkbenchStatsGui.addBar(bar);
        HoloStatsGui.addBar(bar);
    }

    private static void registerAttackFlyingSpeedBar() {
        ResourceLocation res = new ResourceLocation("minecraft:generic.flying_speed");
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(res);
        if (attribute == null) {
            return;
        }
        IStatGetter getter = new StatGetterAttribute(attribute, false);
        GuiStatBar bar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".attribute.flying_speed.name",
                0, 10,
                false, false, false,
                getter,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(Tetra_loopback.MODID + ".attribute.flying_speed.tooltip", getter)
        );
        WorkbenchStatsGui.addBar(bar);
        HoloStatsGui.addBar(bar);
    }

    private static void registerFollowRangeBar() {
        ResourceLocation res = new ResourceLocation("minecraft:generic.follow_range");
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(res);
        if (attribute == null) {
            return;
        }
        IStatGetter getter = new StatGetterAttribute(attribute, false);
        GuiStatBar bar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".attribute.follow_range.name",
                0, 10,
                false, false, false,
                getter,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(Tetra_loopback.MODID + ".attribute.follow_range.tooltip", getter)
        );
        WorkbenchStatsGui.addBar(bar);
        HoloStatsGui.addBar(bar);
    }

    private static void registerKnockbackResistanceBar() {
        ResourceLocation res = new ResourceLocation("minecraft:generic.knockback_resistance");
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(res);
        if (attribute == null) {
            return;
        }
        IStatGetter getter = new StatGetterAttribute(attribute, false);
        GuiStatBar bar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".attribute.knockback_resistance.name",
                0, 10,
                false, false, false,
                getter,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(Tetra_loopback.MODID + ".attribute.knockback_resistance.tooltip", getter)
        );
        WorkbenchStatsGui.addBar(bar);
        HoloStatsGui.addBar(bar);
    }

    private static void registerLuckBar() {
        ResourceLocation res = new ResourceLocation("minecraft:generic.luck");
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(res);
        if (attribute == null) {
            return;
        }
        IStatGetter getter = new StatGetterAttribute(attribute, false);
        GuiStatBar bar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".attribute.luck.name",
                0, 10,
                false, false, false,
                getter,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(Tetra_loopback.MODID + ".attribute.luck.tooltip", getter)
        );
        WorkbenchStatsGui.addBar(bar);
        HoloStatsGui.addBar(bar);
    }

    private static void registerMaxHealthBar() {
        ResourceLocation res = new ResourceLocation("minecraft:generic.max_health");
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(res);
        if (attribute == null) {
            return;
        }
        IStatGetter getter = new StatGetterAttribute(attribute, false);
        GuiStatBar bar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".attribute.max_health.name",
                0, 10,
                false, false, false,
                getter,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(Tetra_loopback.MODID + ".attribute.max_health.tooltip", getter)
        );
        WorkbenchStatsGui.addBar(bar);
        HoloStatsGui.addBar(bar);
    }

    private static void registerMovementSpeedBar() {
        ResourceLocation res = new ResourceLocation("minecraft:generic.movement_speed");
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(res);
        if (attribute == null) {
            return;
        }
        IStatGetter getter = new StatGetterAttribute(attribute, false);
        GuiStatBar bar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".attribute.movement_speed.name",
                0, 10,
                false, false, false,
                getter,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(Tetra_loopback.MODID + ".attribute.movement_speed.tooltip", getter)
        );
        WorkbenchStatsGui.addBar(bar);
        HoloStatsGui.addBar(bar);
    }

}