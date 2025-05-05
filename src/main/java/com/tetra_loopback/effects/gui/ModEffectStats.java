package com.tetra_loopback.effects.gui;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.client.ClientGuiRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import se.mickelus.tetra.effect.ItemEffect;

public class ModEffectStats {
    public static final ItemEffect thunderingEffect = ItemEffect.get(Tetra_loopback.MODID + ":thundering");
    public static final ItemEffect guardianEffect = ItemEffect.get(Tetra_loopback.MODID + ":guardian");
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

    public static final ItemEffect flameAdmonitionEffect = ItemEffect.get(Tetra_loopback.MODID + ":flame_admonition");
    public static final ItemEffect frostCrownEffect = ItemEffect.get(Tetra_loopback.MODID + ":frost_crown");
    public static final ItemEffect etherealGlowEffect = ItemEffect.get(Tetra_loopback.MODID + ":ethereal_glow");
    public static final ItemEffect runeCreedEffect = ItemEffect.get(Tetra_loopback.MODID + ":rune_creed");
    public static final ItemEffect crimsonScourgeEffect = ItemEffect.get(Tetra_loopback.MODID + ":crimson_scourge");
    public static final ItemEffect strifeEffect = ItemEffect.get(Tetra_loopback.MODID + ":strife");
    public static void safeInit() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientGuiRegistry::registerAllBars);
    }
}
