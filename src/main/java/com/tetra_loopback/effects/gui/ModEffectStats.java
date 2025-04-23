package com.tetra_loopback.effects.gui;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.client.ClientGuiRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import se.mickelus.tetra.effect.ItemEffect;

public class ModEffectStats {
    // 所有效果定义（服务端和客户端都需要）
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

    // 客户端安全初始化入口
    public static void safeInit() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientGuiRegistry::registerAllBars);
    }
}
