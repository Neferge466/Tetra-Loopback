package com.tetra_loopback.effects.curio.tetra;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
public class QuickStrike {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            int totalLevel = getCombinedQuickStrikeLevel(attacker);
            if (totalLevel > 0) {
                float baseDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float minMultiplier = 0.2f + totalLevel * 0.05f;
                float minDamage = baseDamage * minMultiplier;

                if (event.getAmount() < minDamage) {
                    event.setAmount(minDamage);
                }
            }
        }
    }

    private static int getCombinedQuickStrikeLevel(Player player) {
        int curioLevel = getCurioQuickStrikeLevel(player);
        int weaponLevel = getWeaponQuickStrikeLevel(player);
        return curioLevel + weaponLevel;
    }

    private static int getCurioQuickStrikeLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.quickStrike) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio ->
                                ((ModularItem) curio.stack().getItem())
                                        .getEffectLevel(curio.stack(), ItemEffect.quickStrike)
                        )
                        .sum()
                )
                .orElse(0);
    }

    private static int getWeaponQuickStrikeLevel(Player player) {
        return Optional.of(player.getMainHandItem())
                .filter(stack -> stack.getItem() instanceof ModularItem)
                .map(stack -> ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.quickStrike))
                .orElse(0);
    }
}
