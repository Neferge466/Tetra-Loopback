package com.tetra_loopback.effects.curio;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.potion.SeveredPotionEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class severing {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            getSeveringLevel(attacker).ifPresent(level -> {
                LivingEntity target = event.getEntity();
                RandomSource rand = attacker.getRandom();

                if (rand.nextFloat() < level / 100f) {
                    int stackCap = getSeveringEfficiency(attacker) - 1;
                    int currentAmplifier = Optional.ofNullable(target.getEffect(SeveredPotionEffect.instance))
                            .map(MobEffectInstance::getAmplifier)
                            .orElse(-1);

                    target.addEffect(new MobEffectInstance(
                            SeveredPotionEffect.instance,
                            1200,
                            Math.min(currentAmplifier + 1, stackCap),
                            false, false));

                    if (!target.level().isClientSide) {
                        ServerLevel serverLevel = (ServerLevel) target.level();
                        serverLevel.playSound(null,
                                target.getX(), target.getY(), target.getZ(),
                                SoundEvents.PLAYER_ATTACK_STRONG,
                                SoundSource.PLAYERS, 0.8f, 0.9f);

                        serverLevel.sendParticles(
                                new DustParticleOptions(new Vector3f(0.5f, 0, 0), 0.5f),
                                target.getX() + target.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                                target.getY() + target.getBbHeight() * (0.2 + rand.nextGaussian() * 0.4),
                                target.getZ() + target.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                                20, 0, 0, 0, 0f);
                    }
                }
            });
        }
    }

    private static Optional<Integer> getSeveringLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("severing")) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio ->
                                ((ModularItem) curio.stack().getItem())
                                        .getEffectLevel(curio.stack(), ItemEffect.get("severing"))
                        )
                        .sum()
                );
    }

    private static int getSeveringEfficiency(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("severing")) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio ->
                                (int) ((ModularItem) curio.stack().getItem())
                                        .getEffectEfficiency(curio.stack(), ItemEffect.get("severing"))
                        )
                        .sum()
                )
                .orElse(0);
    }
}
