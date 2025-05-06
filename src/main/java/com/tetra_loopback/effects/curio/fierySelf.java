package com.tetra_loopback.effects.curio;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class fierySelf {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker
                && !attacker.level().isClientSide()) {
            Optional.ofNullable(CuriosApi.getCuriosInventory(attacker))
                    .ifPresent(inv -> processFierySelfEffect(attacker, 1.0));
        }
    }

    private static void processFierySelfEffect(Player player, double multiplier) {
        if (player.level().isClientSide()) return;

        List<SlotResult> curios = CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                EffectHelper.getEffectLevel(stack, ItemEffect.get("fierySelf")) > 0))
                .orElse(List.of());

        if (!curios.isEmpty()) {
            double totalEfficiency = curios.stream()
                    .mapToDouble(curio -> EffectHelper.getEffectEfficiency(curio.stack(), ItemEffect.get("fierySelf")))
                    .sum();

            int totalLevel = curios.stream()
                    .mapToInt(curio -> EffectHelper.getEffectLevel(curio.stack(), ItemEffect.get("fierySelf")))
                    .sum();

            BlockPos pos = player.blockPosition();
            float temperature = getBiomeTemperature(player, pos);
            double chance = totalEfficiency * temperature * multiplier;

            if (player.getRandom().nextDouble() < chance) {
                player.setSecondsOnFire(totalLevel);
            }
        }
    }

    private static float getBiomeTemperature(Player player, BlockPos pos) {
        return player.level().getBiome(pos).value().getBaseTemperature();
    }
}
