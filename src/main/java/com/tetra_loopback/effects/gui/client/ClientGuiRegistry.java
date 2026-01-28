package com.tetra_loopback.effects.gui.client;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.getter.astralphase.AstralPhaseGetter;
import com.tetra_loopback.effects.getter.astralphase.AstralPhaseLabelGetter;
import com.tetra_loopback.effects.getter.astralphase.AstralPhaseTooltipGetter;
import com.tetra_loopback.effects.getter.earthenshield.EarthenShieldGetter;
import com.tetra_loopback.effects.getter.earthenshield.EarthenShieldLabelGetter;
import com.tetra_loopback.effects.getter.earthenshield.EarthenShieldTooltipGetter;
import com.tetra_loopback.effects.getter.lifeessence.LifeEssenceGetter;
import com.tetra_loopback.effects.getter.lifeessence.LifeEssenceLabelGetter;
import com.tetra_loopback.effects.getter.lifeessence.LifeEssenceTooltipGetter;
import com.tetra_loopback.effects.getter.lightningstrike.LightningStrikeGetter;
import com.tetra_loopback.effects.getter.lightningstrike.LightningStrikeLabelGetter;
import com.tetra_loopback.effects.getter.lightningstrike.LightningStrikeTooltipGetter;
import com.tetra_loopback.effects.getter.primordial.PrimalLeechGetter;
import com.tetra_loopback.effects.getter.primordial.PrimalLeechLabelGetter;
import com.tetra_loopback.effects.getter.primordial.PrimalLeechTooltipGetter;
import com.tetra_loopback.effects.getter.rainstorm.RainstormGetter;
import com.tetra_loopback.effects.getter.rainstorm.RainstormLabelGetter;
import com.tetra_loopback.effects.getter.rainstorm.RainstormTooltipGetter;
import com.tetra_loopback.effects.getter.resonance.ResonanceGetter;
import com.tetra_loopback.effects.getter.resonance.ResonanceLabelGetter;
import com.tetra_loopback.effects.getter.resonance.ResonanceTooltipGetter;
import com.tetra_loopback.effects.getter.supercooling.SupercoolingGetter;
import com.tetra_loopback.effects.getter.supercooling.SupercoolingLabelGetter;
import com.tetra_loopback.effects.getter.supercooling.SupercoolingTooltipGetter;
import com.tetra_loopback.effects.getter.telluric.TelluricAnchorGetter;
import com.tetra_loopback.effects.getter.telluric.TelluricAnchorLabelGetter;
import com.tetra_loopback.effects.getter.telluric.TelluricAnchorTooltipGetter;
import com.tetra_loopback.effects.getter.vision.VisionFieldGetter;
import com.tetra_loopback.effects.getter.vision.VisionFieldLabelGetter;
import com.tetra_loopback.effects.getter.vision.VisionFieldTooltipGetter;
import com.tetra_loopback.effects.getter.vitalshield.VitalShieldGetter;
import com.tetra_loopback.effects.getter.vitalshield.VitalShieldLabelGetter;
import com.tetra_loopback.effects.getter.vitalshield.VitalShieldTooltipGetter;
import com.tetra_loopback.effects.getter.windlight.WindLightGetter;
import com.tetra_loopback.effects.getter.windlight.WindLightLabelGetter;
import com.tetra_loopback.effects.getter.windlight.WindLightTooltipGetter;
import com.tetra_loopback.effects.gui.effect.astralphase.GuiStatBarAstralPhase;
import com.tetra_loopback.effects.gui.effect.earthenshield.GuiStatBarEarthenShield;
import com.tetra_loopback.effects.gui.effect.lifeessence.GuiStatBarLifeEssence;
import com.tetra_loopback.effects.gui.effect.lightningstrike.GuiStatBarLightningStrike;
import com.tetra_loopback.effects.gui.effect.primordial.GuiStatBarPrimalLeech;
import com.tetra_loopback.effects.gui.effect.rainstorm.GuiStatBarRainstorm;
import com.tetra_loopback.effects.gui.effect.supercooling.GuiStatBarSupercooling;
import com.tetra_loopback.effects.gui.effect.telluric.GuiStatBarTelluricAnchor;
import com.tetra_loopback.effects.gui.effect.vitalshield.GuiStatBarVitalShield;
import com.tetra_loopback.effects.gui.effect.windlight.GuiStatBarWindLight;
import com.tetra_loopback.effects.gui.resonance.GuiStatBarQuadSegmented;
import com.tetra_loopback.effects.gui.vision.GuiStatBarVisionField;
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
        registerGuardianBar();
        registerFlameAdmonitionBar();
        registerFrostCrownBar();
        registerEtherealGlowBar();
        registerRuneCreedBar();
        registerCrimsonScourgeBar();
        registerStrifeBar();

        registerResonanceBar();

        registerRainstormBar();
        registerWindLightBar();

        registerAttackKnockbackBar();
        registerAttackFlyingSpeedBar();
        registerFollowRangeBar();
        registerKnockbackResistanceBar();
        registerLuckBar();
        registerMaxHealthBar();
        registerMovementSpeedBar();

        registerVisionFieldBar();

        registerTemperedColdBar();
        registerSupercoolingBar();
        registerLifeEssenceBar();
        registerPrimalLeechBar();
        registerTelluricAnchorBar();
        registerAstralPhaseBar();
        registerEarthenShieldBar();
        registerLightningStrikeBar();
        registerVitalShieldBar();


    }




    private static void registerVitalShieldBar() {
        IStatGetter vitalShieldGetter = new VitalShieldGetter();
        GuiStatBarVitalShield vitalShieldBar = new GuiStatBarVitalShield(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.vital_shield.name",
                0, 5,
                false, false, false,
                vitalShieldGetter,
                new VitalShieldLabelGetter(),
                new VitalShieldTooltipGetter()
        );

        WorkbenchStatsGui.addBar(vitalShieldBar);
        HoloStatsGui.addBar(vitalShieldBar);
    }







    private static void registerLightningStrikeBar() {
        IStatGetter lightningStrikeGetter = new LightningStrikeGetter();
        GuiStatBarLightningStrike lightningStrikeBar = new GuiStatBarLightningStrike(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.lightning_strike.name",
                0, 5,  // 等级0-5
                false, false, true,
                lightningStrikeGetter,
                new LightningStrikeLabelGetter(),
                new LightningStrikeTooltipGetter()
        );

        WorkbenchStatsGui.addBar(lightningStrikeBar);
        HoloStatsGui.addBar(lightningStrikeBar);
    }

    private static void registerEarthenShieldBar() {
        IStatGetter earthenShieldGetter = new EarthenShieldGetter();
        GuiStatBarEarthenShield earthenShieldBar = new GuiStatBarEarthenShield(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.earthen_shield.name",
                0, 5,
                false, false, true,
                earthenShieldGetter,
                new EarthenShieldLabelGetter(),
                new EarthenShieldTooltipGetter()
        );

        WorkbenchStatsGui.addBar(earthenShieldBar);
        HoloStatsGui.addBar(earthenShieldBar);
    }



    private static void registerAstralPhaseBar() {
        IStatGetter astralPhaseGetter = new AstralPhaseGetter();
        GuiStatBarAstralPhase astralPhaseBar = new GuiStatBarAstralPhase(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.astral_phase.name",
                0, 1,
                false, false, true,
                astralPhaseGetter,
                new AstralPhaseLabelGetter(),
                new AstralPhaseTooltipGetter()
        );

        WorkbenchStatsGui.addBar(astralPhaseBar);
        HoloStatsGui.addBar(astralPhaseBar);
    }



    private static void registerTelluricAnchorBar() {
        IStatGetter telluricGetter = new TelluricAnchorGetter();
        GuiStatBarTelluricAnchor telluricBar = new GuiStatBarTelluricAnchor(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.telluric_anchor.name",
                0, 3,
                false, false, true,
                telluricGetter,
                new TelluricAnchorLabelGetter(),
                new TelluricAnchorTooltipGetter()
        );

        WorkbenchStatsGui.addBar(telluricBar);
        HoloStatsGui.addBar(telluricBar);
    }



    private static void registerPrimalLeechBar() {
        IStatGetter primalLeechGetter = new PrimalLeechGetter();
        GuiStatBarPrimalLeech primalLeechBar = new GuiStatBarPrimalLeech(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.primal_leech.name",
                0, 1,
                false, false, true,
                primalLeechGetter,
                new PrimalLeechLabelGetter(),
                new PrimalLeechTooltipGetter()
        );

        WorkbenchStatsGui.addBar(primalLeechBar);
        HoloStatsGui.addBar(primalLeechBar);
    }





    private static void registerLifeEssenceBar() {
        IStatGetter lifeEssenceGetter = new LifeEssenceGetter();
        GuiStatBarLifeEssence lifeEssenceBar = new GuiStatBarLifeEssence(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.life_essence.name",
                0, 1,
                false, false, true,
                lifeEssenceGetter,
                new LifeEssenceLabelGetter(),
                new LifeEssenceTooltipGetter()
        );

        WorkbenchStatsGui.addBar(lifeEssenceBar);
        HoloStatsGui.addBar(lifeEssenceBar);
    }


    private static void registerSupercoolingBar() {
        IStatGetter supercoolingGetter = new SupercoolingGetter();
        GuiStatBarSupercooling supercoolingBar = new GuiStatBarSupercooling(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.supercooling.name",
                0, 1,
                false, false, true,
                supercoolingGetter,
                new SupercoolingLabelGetter(),
                new SupercoolingTooltipGetter()
        );

        WorkbenchStatsGui.addBar(supercoolingBar);
        HoloStatsGui.addBar(supercoolingBar);
    }


    private static void registerTemperedColdBar() {
        IStatGetter temperedColdGetter = new StatGetterEffectLevel(ModEffectStats.temperedColdEffect, 1);
        GuiStatBar temperedColdBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.tempered_cold.name",
                0, 1,
                false, false, false,
                temperedColdGetter,
                new ILabelGetter() {
                    @Override
                    public String getLabel(double value, double diffValue, boolean flipped) {
                        return value > 0 ? "§9❄" : "";
                    }

                    @Override
                    public String getLabelMerged(double v, double v1) {
                        return "";
                    }
                },
                new TooltipGetterDecimal(Tetra_loopback.MODID + ".effect.tempered_cold.tooltip", temperedColdGetter)
        );
        WorkbenchStatsGui.addBar(temperedColdBar);
        HoloStatsGui.addBar(temperedColdBar);
    }


    private static void registerWindLightBar() {
        IStatGetter windLightGetter = new WindLightGetter();
        GuiStatBarWindLight windLightBar = new GuiStatBarWindLight(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.wind_light.name",
                0, 2,  // 效果等级0-2
                false, false, true,
                windLightGetter,
                new WindLightLabelGetter(),
                new WindLightTooltipGetter()
        );

        WorkbenchStatsGui.addBar(windLightBar);
        HoloStatsGui.addBar(windLightBar);
    }


    private static void registerRainstormBar() {
        IStatGetter rainstormGetter = new RainstormGetter();
        GuiStatBarRainstorm rainstormBar = new GuiStatBarRainstorm(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.rainstorm.name",
                0, 3,
                false, false, true,
                rainstormGetter,
                new RainstormLabelGetter(),
                new RainstormTooltipGetter()
        );

        WorkbenchStatsGui.addBar(rainstormBar);
        HoloStatsGui.addBar(rainstormBar);
    }

    private static void registerVisionFieldBar() {
        IStatGetter visionFieldGetter = new VisionFieldGetter();
        GuiStatBarVisionField visionFieldBar = new GuiStatBarVisionField(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.vision_field.name",
                0, 5,
                false, false, true,
                visionFieldGetter,
                new VisionFieldLabelGetter(),
                new VisionFieldTooltipGetter()
        );

        WorkbenchStatsGui.addBar(visionFieldBar);
        HoloStatsGui.addBar(visionFieldBar);
    }



    private static void registerResonanceBar() {
        IStatGetter resonanceGetter = new ResonanceGetter();
        GuiStatBarQuadSegmented resonanceBar = new GuiStatBarQuadSegmented(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.resonance.name",
                0, 20,
                false, false, true,
                resonanceGetter,
                new ResonanceLabelGetter(),
                new ResonanceTooltipGetter()
        );


        WorkbenchStatsGui.addBar(resonanceBar);
        HoloStatsGui.addBar(resonanceBar);
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