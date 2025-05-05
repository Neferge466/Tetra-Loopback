package com.tetra_loopback.effects.curio;

import net.minecraft.world.entity.LivingEntity;
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
public class skewering {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            LivingEntity target = event.getEntity();
            int targetArmor = target.getArmorValue();

            getSkeweringBonus(attacker, targetArmor).ifPresent(bonus ->
                    event.setAmount(event.getAmount() + bonus)
            );
        }
    }

    private static Optional<Integer> getSkeweringBonus(Player player, int targetArmor) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("skewering")) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio -> {
                            ModularItem item = (ModularItem) curio.stack().getItem();
                            int level = item.getEffectLevel(curio.stack(), ItemEffect.get("skewering"));
                            float efficiency = item.getEffectEfficiency(curio.stack(), ItemEffect.get("skewering"));
                            return efficiency >= targetArmor ? level : 0;
                        })
                        .sum()
                )
                .filter(bonus -> bonus > 0);
    }
}

