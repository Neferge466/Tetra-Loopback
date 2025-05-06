package com.tetra_loopback.effects.curio;

import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class reaching {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            getReachingLevel(attacker).ifPresent(level -> {
                Vec3 attackerPos = attacker.position();
                Vec3 targetPos = event.getEntity().position();
                double distance = attackerPos.distanceToSqr(targetPos);

                float multiplier = event.getSource().is(DamageTypeTags.IS_PROJECTILE) ?
                        attacker.getAttackStrengthScale(0.5f) : 1.0f;

                if (distance > 1.0) {
                    float damageMultiplier = getMultiplier(level, distance, multiplier);
                    event.setAmount(event.getAmount() * damageMultiplier);
                }
            });
        }
    }

    private static Optional<Integer> getReachingLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("reaching")) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio ->
                                ((ModularItem) curio.stack().getItem())
                                        .getEffectLevel(curio.stack(), ItemEffect.get("reaching"))
                        )
                        .sum()
                );
    }

    private static float getMultiplier(int level, double squareDistance, float offsetMultiplier) {
        return level > 0 && squareDistance > 0 ?
                1.0f + getOffset(level, squareDistance) * offsetMultiplier :
                1.0f;
    }

    private static float getOffset(int level, double squareDistance) {
        return (float) (level / 100.0f * Math.log(squareDistance * squareDistance));
    }
}
