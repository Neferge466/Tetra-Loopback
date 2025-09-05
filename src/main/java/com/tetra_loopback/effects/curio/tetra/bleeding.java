package com.tetra_loopback.effects.curio.tetra;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.potion.BleedingPotionEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class bleeding {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            int totalLevel = getCombinedBleedingLevel(attacker);
            LivingEntity target = event.getEntity();

            if (totalLevel > 0
                    && !MobType.UNDEAD.equals(target.getMobType())
                    && attacker.getRandom().nextFloat() < 0.3f) {
                target.addEffect(new MobEffectInstance(
                        BleedingPotionEffect.instance,
                        40,
                        totalLevel,
                        false,
                        true));
            }
        }
    }

    private static int getCombinedBleedingLevel(Player player) {
        return getCurioBleedingLevel(player) + getWeaponBleedingLevel(player);
    }

    private static int getCurioBleedingLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.bleeding) > 0))
                .map(list -> list.stream()
                        .mapToInt(curio -> ((ModularItem) curio.stack().getItem())
                                .getEffectLevel(curio.stack(), ItemEffect.bleeding))
                        .sum())
                .orElse(0);
    }

    private static int getWeaponBleedingLevel(Player player) {
        return Optional.of(player.getMainHandItem())
                .filter(stack -> stack.getItem() instanceof ModularItem)
                .map(stack -> ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.bleeding))
                .orElse(0);
    }
}
