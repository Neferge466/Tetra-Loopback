package com.tetra_loopback.effects.curio;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class sweeping {
    private static final double BASE_RANGE = 1.0;
    private static final double RANGE_MULTIPLIER = 0.6;
    private static final float DAMAGE_MULTIPLIER = 0.125f;
    private static final float MAX_DAMAGE_FACTOR = 2.0f;
    private static final int SOUND_COOLDOWN = 500; //ms

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            if (event.getEntity().getTags().contains("tetra_sweep_processed")) return;

            try {
                event.getEntity().addTag("tetra_sweep_processed");

                getSweepingLevel(attacker).ifPresent(level -> {
                    LivingEntity target = event.getEntity();
                    float baseDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    double efficiency = getSweepingEfficiency(attacker);

                    double totalRange = BASE_RANGE + efficiency * RANGE_MULTIPLIER;
                    float damageFactor = Math.min(level * DAMAGE_MULTIPLIER, MAX_DAMAGE_FACTOR);
                    float sweepDamage = baseDamage * damageFactor;

                    Vec3 lookVec = attacker.getLookAngle().normalize();
                    AABB aoeBox = calculateAttackAABB(attacker, totalRange, lookVec);

                    attacker.level().getEntitiesOfClass(LivingEntity.class, aoeBox).stream()
                            .filter(entity -> isValidTarget(entity, attacker, target))
                            .forEach(entity -> handleSweepAttack(attacker, entity, sweepDamage, lookVec));
                });
            } finally {
                event.getEntity().removeTag("tetra_sweep_processed");
            }
        }
    }

    private static AABB calculateAttackAABB(Player attacker, double range, Vec3 direction) {
        Vec3 startPos = attacker.position()
                .add(0, attacker.getEyeHeight() * 0.5, 0)
                .add(direction.scale(1.5));

        return new AABB(startPos, startPos)
                .inflate(range, 0.4, range)
                .expandTowards(direction.scale(range * 0.8));
    }

    private static boolean isValidTarget(LivingEntity entity, Player attacker, LivingEntity mainTarget) {
        return entity != attacker
                && (entity == mainTarget || !attacker.isAlliedTo(entity))
                && attacker.hasLineOfSight(entity)
                && attacker.distanceToSqr(entity) < 36.0; // 6blocks
    }

    private static void handleSweepAttack(Player attacker, LivingEntity target, float damage, Vec3 direction) {
        applyKnockback(target, attacker.getYRot(), 0.5f);

        DamageSource damageSource = attacker.damageSources().playerAttack(attacker);
        target.hurt(damageSource, damage);

        if (!attacker.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) attacker.level();
            playSweepEffects(serverLevel, target, attacker);
        }
    }

    private static void applyKnockback(LivingEntity target, float yRot, float strength) {
        float radians = yRot * (float) (Math.PI / 180.0);
        target.knockback(strength,
                Mth.sin(radians),
                -Mth.cos(radians)
        );
    }

    private static void playSweepEffects(ServerLevel level, LivingEntity target, Player attacker) {
        long lastSound = attacker.getPersistentData().getLong("LastSweepSound");
        long now = System.currentTimeMillis();

        if (now - lastSound > SOUND_COOLDOWN) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS,
                    0.8F,
                    0.9F + attacker.getRandom().nextFloat() * 0.2F
            );
            attacker.getPersistentData().putLong("LastSweepSound", now);
        }

        Vec3 pos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        level.sendParticles(
                new DustParticleOptions(new Vector3f(0.9f, 0.8f, 0.3f), 1.0f),
                pos.x, pos.y, pos.z,
                5,
                0.3, 0.2, 0.3,
                0.02
        );
    }

    private static Optional<Integer> getSweepingLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("sweeping")) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio ->
                                ((ModularItem) curio.stack().getItem())
                                        .getEffectLevel(curio.stack(), ItemEffect.get("sweeping"))
                        )
                        .sum()
                );
    }

    private static double getSweepingEfficiency(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("sweeping")) > 0
                ))
                .map(list -> list.stream()
                        .mapToDouble(curio ->
                                ((ModularItem) curio.stack().getItem())
                                        .getEffectEfficiency(curio.stack(), ItemEffect.get("sweeping"))
                        )
                        .sum()
                )
                .orElse(0.0);
    }
}
