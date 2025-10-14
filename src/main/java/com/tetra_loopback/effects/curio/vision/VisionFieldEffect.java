package com.tetra_loopback.effects.curio.vision;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class VisionFieldEffect {
    private static final Map<UUID, Integer> playerEffectLevels = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        UUID playerId = player.getUUID();

        //检查效果等级
        int effectLevel = getEffectLevel(player);

        if (effectLevel > 0) {
            playerEffectLevels.put(playerId, effectLevel);
        } else {
            playerEffectLevels.remove(playerId);
        }
    }

    private static int getEffectLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ModEffectStats.visionFieldEffect) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(slotResult -> {
                            ItemStack stack = slotResult.stack();
                            return ((ModularItem) stack.getItem()).getEffectLevel(stack, ModEffectStats.visionFieldEffect);
                        })
                        .max()
                        .orElse(0))
                .orElse(0);
    }

    //获取效果等级
    public static int getPlayerEffectLevel(Player player) {
        return playerEffectLevels.getOrDefault(player.getUUID(), 0);
    }

    //清理数据
    public static void cleanupPlayer(UUID playerId) {
        playerEffectLevels.remove(playerId);
    }
}