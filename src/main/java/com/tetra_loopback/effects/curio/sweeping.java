package com.tetra_loopback.effects.curio;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class sweeping {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            LivingEntity target = event.getEntity();

            if (attacker.getAttackStrengthScale(0.5f) >= 0.9f) {
                getSweepingData(attacker).ifPresent(data -> {
                    float baseDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    performSweepAttack(attacker, target, data.level(), data.efficiency(), baseDamage);
                });
            }
        }
    }

    private static void performSweepAttack(Player attacker, LivingEntity mainTarget, int level, float efficiency, float baseDamage) {
        double range = 1 + efficiency;
        AABB aoe = mainTarget.getBoundingBox().inflate(range, 0.25, range);
        float sweepDamage = Math.max(baseDamage * level * 0.125f, 1f);
        boolean enhancedKnockback = level >= 4;

        attacker.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, aoe).stream()
                .filter(entity -> entity != attacker)
                .filter(entity -> entity != mainTarget)
                .filter(entity -> !attacker.isAlliedTo(entity))
                .forEach(entity -> {
                    // Apply knockback
                    float knockbackStrength = enhancedKnockback ?
                            (EnchantmentHelper.getKnockbackBonus(attacker) + 1) * 0.5f : 0.5f;

                    entity.knockback(knockbackStrength,
                            Mth.sin(attacker.getYRot() * Mth.DEG_TO_RAD),
                            -Mth.cos(attacker.getYRot() * Mth.DEG_TO_RAD));

                    // Apply damage
                    DamageSource damageSource = attacker.damageSources().playerAttack(attacker);
                    entity.hurt(damageSource, sweepDamage);
                });

        // Play effects
        attacker.getCommandSenderWorld().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
        attacker.sweepAttack();
    }

    private static Optional<SweepingData> getSweepingData(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("sweeping")) > 0
                ))
                .map(list -> {
                    int totalLevel = list.stream()
                            .mapToInt(curio ->
                                    ((ModularItem) curio.stack().getItem())
                                            .getEffectLevel(curio.stack(), ItemEffect.get("sweeping"))
                            )
                            .sum();

                    float maxEfficiency = (float) list.stream()
                            .mapToDouble(curio ->
                                    ((ModularItem) curio.stack().getItem())
                                            .getEffectEfficiency(curio.stack(), ItemEffect.get("sweeping"))
                            )
                            .max()
                            .orElse(0.0);

                    return new SweepingData(totalLevel, maxEfficiency);
                })
                .filter(data -> data.level() > 0);
    }

    private record SweepingData(int level, float efficiency) {}
}
