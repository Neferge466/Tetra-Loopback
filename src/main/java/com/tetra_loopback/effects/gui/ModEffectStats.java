package com.tetra_loopback.effects.gui;



import com.tetra_loopback.Tetra_loopback;
import net.minecraft.client.resources.language.I18n;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;

public class ModEffectStats {
    // Thundering
    public static final ItemEffect thunderingEffect =
            ItemEffect.get(Tetra_loopback.MODID + ":thundering");
    public static final String thunderingName =
            Tetra_loopback.MODID + ".effect.thundering.name";
    public static final String thunderingTooltip =
            Tetra_loopback.MODID + ".effect.thundering.tooltip";

    // Guardian
    public static final ItemEffect guardianEffect =
            ItemEffect.get(Tetra_loopback.MODID + ":guardian");
    public static final String guardianName =
            Tetra_loopback.MODID + ".effect.guardian.name";
    public static final String guardianTooltip =
            Tetra_loopback.MODID + ".effect.guardian.tooltip";

    //attribute
    public static final ItemEffect armorEffect = ItemEffect.get(Tetra_loopback.MODID + ":armor");
    public static final ItemEffect armorToughnessEffect = ItemEffect.get(Tetra_loopback.MODID + ":armor_toughness");
    public static final ItemEffect attackDamageEffect = ItemEffect.get(Tetra_loopback.MODID + ":attack_damage");
    public static final ItemEffect attackKnockbackEffect = ItemEffect.get(Tetra_loopback.MODID + ":attack_knockback");
    public static final ItemEffect attackSpeedEffect = ItemEffect.get(Tetra_loopback.MODID + ":attack_speed");
    public static final ItemEffect flyingSpeedEffect = ItemEffect.get(Tetra_loopback.MODID + ":flying_speed");
    public static final ItemEffect followRangeEffect = ItemEffect.get(Tetra_loopback.MODID + ":follow_range");
    public static final ItemEffect knockbackResistanceEffect = ItemEffect.get(Tetra_loopback.MODID + ":knockback_resistance");
    public static final ItemEffect luckEffect = ItemEffect.get(Tetra_loopback.MODID + ":luck");
    public static final ItemEffect maxHealthEffect = ItemEffect.get(Tetra_loopback.MODID + ":max_health");
    public static final ItemEffect movementSpeedEffect = ItemEffect.get(Tetra_loopback.MODID + ":movement_speed");


    public static final String armorName = Tetra_loopback.MODID + ".effect.armor.name";
    public static final String armorToughnessName = Tetra_loopback.MODID + ".effect.armor_toughness.name";
    public static final String attackDamageName = Tetra_loopback.MODID + ".effect.attack_damage.name";
    public static final String attackKnockbackName = Tetra_loopback.MODID + ".effect.attack_knockback.name";
    public static final String attackSpeedName = Tetra_loopback.MODID + ".effect.attack_speed.name";
    public static final String flyingSpeedName = Tetra_loopback.MODID + ".effect.flying_speed.name";
    public static final String followRangeName = Tetra_loopback.MODID + ".effect.follow_range.name";
    public static final String knockbackResistanceName = Tetra_loopback.MODID + ".effect.knockback_resistance.name";
    public static final String luckName = Tetra_loopback.MODID + ".effect.luck.name";
    public static final String maxHealthName = Tetra_loopback.MODID + ".effect.max_health.name";
    public static final String movementSpeedName = Tetra_loopback.MODID + ".effect.movement_speed.name";

    public static final String armorTooltip = Tetra_loopback.MODID + ".effect.armor.tooltip";
    public static final String armorToughnessTooltip = Tetra_loopback.MODID + ".effect.armor_toughness.tooltip";
    public static final String attackDamageTooltip = Tetra_loopback.MODID + ".effect.attack_damage.tooltip";
    public static final String attackKnockbackTooltip = Tetra_loopback.MODID + ".effect.attack_knockback.tooltip";
    public static final String attackSpeedTooltip = Tetra_loopback.MODID + ".effect.attack_speed.tooltip";
    public static final String flyingSpeedTooltip = Tetra_loopback.MODID + ".effect.flying_speed.tooltip";
    public static final String followRangeTooltip = Tetra_loopback.MODID + ".effect.follow_range.tooltip";
    public static final String knockbackResistanceTooltip = Tetra_loopback.MODID + ".effect.knockback_resistance.tooltip";
    public static final String luckTooltip = Tetra_loopback.MODID + ".effect.luck.tooltip";
    public static final String maxHealthTooltip = Tetra_loopback.MODID + ".effect.max_health.tooltip";
    public static final String movementSpeedTooltip = Tetra_loopback.MODID + ".effect.movement_speed.tooltip";



    public static void registerBars() {






        //Thundering

        IStatGetter thunderingGetter = new StatGetterEffectLevel(thunderingEffect, 1);

        ITooltipGetter thunderingTooltip = new TooltipGetterInteger(
                Tetra_loopback.MODID + ".effect.thundering.tooltip",
                thunderingGetter
        );

        GuiStatBar thunderingBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                Tetra_loopback.MODID + ".effect.thundering.name",
                0, 10, //
                false, false, false,
                thunderingGetter,
                LabelGetterBasic.integerLabel,
                thunderingTooltip
        );

        WorkbenchStatsGui.addBar(thunderingBar);
        HoloStatsGui.addBar(thunderingBar);

        IStatGetter guardianGetter = new StatGetterEffectLevel(guardianEffect, 1);
        GuiStatBar guardianBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                ModEffectStats.guardianName, 0, 10,
                false, false, false,
                guardianGetter,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(ModEffectStats.guardianTooltip, guardianGetter)
        );

        WorkbenchStatsGui.addBar(guardianBar);
        HoloStatsGui.addBar(guardianBar);





        registerAttributeBar(
                armorEffect,
                armorName,
                armorTooltip,
                new StatGetterEffectLevel(armorEffect, 1)
        );

        registerAttributeBar(
                armorToughnessEffect,
                armorToughnessName,
                armorToughnessTooltip,
                new StatGetterEffectLevel(armorToughnessEffect, 1)
        );

        registerAttributeBar(
                attackDamageEffect,
                attackDamageName,
                attackDamageTooltip,
                new StatGetterEffectLevel(attackDamageEffect, 1)
        );

        registerAttributeBar(
                attackKnockbackEffect,
                attackKnockbackName,
                attackKnockbackTooltip,
                new StatGetterEffectLevel(attackKnockbackEffect, 1)
        );

        IStatGetter attackSpeedGetter = new StatGetterEffectLevel(attackSpeedEffect, 1);
        GuiStatBar attackSpeedBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                attackSpeedName,
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
                new TooltipGetterInteger(attackSpeedTooltip, attackSpeedGetter)
        );
        WorkbenchStatsGui.addBar(attackSpeedBar);
        HoloStatsGui.addBar(attackSpeedBar);

        registerAttributeBar(
                flyingSpeedEffect,
                flyingSpeedName,
                flyingSpeedTooltip,
                new StatGetterEffectLevel(flyingSpeedEffect, 1)
        );

        registerAttributeBar(
                followRangeEffect,
                followRangeName,
                followRangeTooltip,
                new StatGetterEffectLevel(followRangeEffect, 1)
        );

        registerAttributeBar(
                knockbackResistanceEffect,
                knockbackResistanceName,
                knockbackResistanceTooltip,
                new StatGetterEffectLevel(knockbackResistanceEffect, 1)
        );

        registerAttributeBar(
                luckEffect,
                luckName,
                luckTooltip,
                new StatGetterEffectLevel(luckEffect, 1)
        );

        registerAttributeBar(
                maxHealthEffect,
                maxHealthName,
                maxHealthTooltip,
                new StatGetterEffectLevel(maxHealthEffect, 1)
        );

        IStatGetter movementSpeedGetter = new StatGetterEffectLevel(movementSpeedEffect, 1);
        GuiStatBar movementSpeedBar = new GuiStatBar(
                0, 0, StatsHelper.barLength,
                movementSpeedName,
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
                new TooltipGetterInteger(movementSpeedTooltip, movementSpeedGetter)
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

