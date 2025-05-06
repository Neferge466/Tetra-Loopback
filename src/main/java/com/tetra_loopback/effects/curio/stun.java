package com.tetra_loopback.effects.curio;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class stun {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            getStunLevel(attacker).ifPresent(level -> {
                if (attacker.getRandom().nextFloat() < level / 100f) {
                    LivingEntity target = event.getEntity();
                    int duration = getStunEfficiency(attacker) * 20;

                    target.addEffect(new MobEffectInstance(
                            StunPotionEffect.instance,
                            duration,
                            0,
                            false,
                            false));

                    if (!target.level().isClientSide) {
                        ServerLevel serverLevel = (ServerLevel) target.level();
                        serverLevel.playSound(null,
                                target.getX(), target.getY(), target.getZ(),
                                SoundEvents.PLAYER_ATTACK_STRONG,
                                SoundSource.PLAYERS, 0.8f, 0.9f);

                        serverLevel.sendParticles(ParticleTypes.ENTITY_EFFECT,
                                target.getX(),
                                target.getEyeY(),
                                target.getZ(),
                                5, 0, 0, 0, 0);
                    }
                }
            });
        }
    }

    private static Optional<Integer> getStunLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("stun")) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio ->
                                ((ModularItem) curio.stack().getItem())
                                        .getEffectLevel(curio.stack(), ItemEffect.get("stun"))
                        )
                        .sum()
                );
    }

    private static int getStunEfficiency(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("stun")) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio ->
                                (int) ((ModularItem) curio.stack().getItem())
                                        .getEffectEfficiency(curio.stack(), ItemEffect.get("stun"))
                        )
                        .sum()
                )
                .orElse(0);
    }
}
