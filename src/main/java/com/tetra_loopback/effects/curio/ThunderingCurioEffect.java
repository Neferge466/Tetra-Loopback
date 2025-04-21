package com.tetra_loopback.effects.curio;

import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ThunderingCurioEffect {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 10_000;

    public static void tryTriggerThunder(Player player, Entity target) {
        if (player.level().isClientSide || !(target instanceof LivingEntity)) return;

        long currentTime = System.currentTimeMillis();
        UUID playerId = player.getUUID();

        if (cooldowns.containsKey(playerId)) {
            long lastTrigger = cooldowns.get(playerId);
            if (currentTime - lastTrigger < COOLDOWN_MS) return;
        }

        // check thundering
        boolean hasThundering = CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(itemStack -> {
                    if (itemStack.getItem() instanceof ModularItem) {
                        ModularItem item = (ModularItem) itemStack.getItem();
                        return item.getEffectLevel(itemStack, ModEffectStats.thunderingEffect) > 0;
                    }
                    return false;
                }))
                .map(list -> !list.isEmpty())
                .orElse(false);

        if (hasThundering) {
            ServerLevel world = (ServerLevel) player.level();
            LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
            lightning.moveTo(target.getX(), target.getY(), target.getZ());
            world.addFreshEntity(lightning);

            cooldowns.put(playerId, currentTime);
        }
    }
}