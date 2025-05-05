package com.tetra_loopback.effects.curio;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class CrimsonScourgeEffect {
    private static final Map<UUID, ScourgeData> targetData = new ConcurrentHashMap<>();

    private static class ScourgeData {
        int stacks = 0;
        float totalBleed = 0;
        long lastBleedTick = 0;
        boolean canTrigger = true;
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker &&
                hasEffect(attacker))
        {
            LivingEntity target = event.getEntity();
            applyBleeding(target, attacker);
        }
    }

    private static void applyBleeding(LivingEntity target, Player attacker) {
        ScourgeData data = targetData.computeIfAbsent(target.getUUID(), k -> new ScourgeData());

        if (data.stacks < 10) {
            data.stacks++;
        }

        if (data.lastBleedTick == 0) {
            MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
                if (event.phase == TickEvent.Phase.END && target.isAlive()) {
                    handleBleeding(target, data);
                }
            });
        }
        data.lastBleedTick = target.level().getGameTime();
    }

    private static void handleBleeding(LivingEntity target, ScourgeData data) {
        long currentTime = target.level().getGameTime();
        if (currentTime - data.lastBleedTick >= 20) {
            float maxHealth = target.getMaxHealth();
            float bleedDamage = maxHealth * (0.01f + data.stacks * 0.001f);

            if (target.getHealth() / maxHealth > 0.5f) {
                target.hurt(target.damageSources().magic(), bleedDamage);
                data.totalBleed += bleedDamage;

                if (data.stacks >= 7 &&
                        data.totalBleed >= maxHealth * 0.15f &&
                        data.canTrigger)
                {
                    target.hurt(target.damageSources().magic(), maxHealth * 0.4f);
                    data.canTrigger = false;
                }
            }

            if (currentTime - data.lastBleedTick >= 140) {
                targetData.remove(target.getUUID());
            }
        }
    }

    private static boolean hasEffect(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ModEffectStats.crimsonScourgeEffect) > 0
                ))
                .map(list -> !list.isEmpty())
                .orElse(false);
    }
}