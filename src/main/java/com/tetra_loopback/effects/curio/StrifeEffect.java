package com.tetra_loopback.effects.curio;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class StrifeEffect {
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final int BUFF_DURATION = 10 * 20; // 10s
    private static final int COOLDOWN = 20 * 20;      // 20s

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker &&
                hasEffect(attacker))
        {
            LivingEntity target = event.getEntity();

            if (target instanceof Monster) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 10 * 20, 1)); // 虚弱II
            } else {
                attacker.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 10 * 20, 1));
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player &&
                hasEffect(player) &&
                !player.level().isClientSide())
        {
            long currentTime = player.level().getGameTime();
            Long lastTrigger = cooldowns.get(player.getUUID());

            if (lastTrigger == null || currentTime - lastTrigger >= COOLDOWN) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST, BUFF_DURATION, 2));
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE, BUFF_DURATION, 1));

                cooldowns.put(player.getUUID(), currentTime);
            }
        }
    }

    private static boolean hasEffect(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ModEffectStats.strifeEffect) > 0
                ))
                .map(list -> !list.isEmpty())
                .orElse(false);
    }
}