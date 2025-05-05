package com.tetra_loopback.effects.curio;

import net.minecraft.world.entity.player.Player;
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
public class crushing {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            getCrushingLevel(attacker).ifPresent(level -> {
                if (event.getAmount() < level) {
                    float attackStrength = attacker.getAttackStrengthScale(0.5f);
                    if (attackStrength > 0.9f) {
                        event.setAmount(level);
                    }
                }
            });
        }
    }

    private static Optional<Integer> getCrushingLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("crushing")) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio ->
                                ((ModularItem) curio.stack().getItem())
                                        .getEffectLevel(curio.stack(), ItemEffect.get("crushing"))
                        )
                        .sum()
                );
    }
}
