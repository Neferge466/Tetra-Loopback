package com.tetra_loopback.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;

import java.util.UUID;

public class ThunderEffect extends MobEffect {
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
    private static final String COOLDOWN_TAG = "ThunderCooldown";
    private static final String ATTACK_BONUS_TAG = "ThunderAttackBonus";
    private static final String LAST_COMBAT_TAG = "LastCombatTime";

    public ThunderEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00BFFF);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            long currentTime = entity.level().getGameTime();
            long lastCombatTime = entity.getPersistentData().getLong(LAST_COMBAT_TAG);

            if (currentTime - lastCombatTime > 300) {
                resetAttackBonus(entity);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private void resetAttackBonus(LivingEntity entity) {
        if (entity.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(ATTACK_DAMAGE_UUID) != null) {
            entity.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_UUID);
        }
        entity.getPersistentData().remove(ATTACK_BONUS_TAG);
    }

    public static void tryTriggerThunder(LivingEntity attacker, LivingEntity target) {
        if (attacker.level().isClientSide) return;

        var persistentData = attacker.getPersistentData();
        long gameTime = attacker.level().getGameTime();

        if (gameTime < persistentData.getLong(COOLDOWN_TAG)) return;

        if (attacker.getRandom().nextFloat() < 0.2f) {

            LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, attacker.level());
            lightning.moveTo(Vec3.atBottomCenterOf(target.blockPosition()));
            attacker.level().addFreshEntity(lightning);

            persistentData.putLong(COOLDOWN_TAG, gameTime + 200);

            int currentBonus = persistentData.getInt(ATTACK_BONUS_TAG) + 2;
            persistentData.putInt(ATTACK_BONUS_TAG, currentBonus);

            if (attacker.getAttribute(Attributes.ATTACK_DAMAGE) != null) {

                attacker.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_UUID);

                AttributeModifier modifier = new AttributeModifier(
                        ATTACK_DAMAGE_UUID,
                        "Thunder Attack Bonus",
                        currentBonus,
                        AttributeModifier.Operation.ADDITION
                );
                attacker.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(modifier);
            }

            persistentData.putLong(LAST_COMBAT_TAG, gameTime);
        }
    }
}