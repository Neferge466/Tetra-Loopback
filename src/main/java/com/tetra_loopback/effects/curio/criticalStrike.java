package com.tetra_loopback.effects.curio;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Random;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class criticalStrike {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = (Player) event.getEntity();

        if (event.isCanceled()) return;

        getCriticalStrikeLevel(player).ifPresent(totalLevel -> {
            if (player.getRandom().nextFloat() < totalLevel * 0.01f) {
                ItemStack mainHand = player.getMainHandItem();
                float critMultiplier = 2.0f;

                if (mainHand.getItem() instanceof ModularItem modularItem) {
                    critMultiplier = modularItem.getEffectEfficiency(mainHand, ItemEffect.criticalStrike);
                }

                event.setDamageModifier(Math.max(critMultiplier, event.getDamageModifier()));
                event.setResult(CriticalHitEvent.Result.ALLOW);

                if (!player.level().isClientSide()) {
                    ServerLevel serverLevel = (ServerLevel) player.level();
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            event.getTarget().getX(),
                            event.getTarget().getY() + event.getTarget().getBbHeight() / 2,
                            event.getTarget().getZ(),
                            15,
                            0.5, 0.5, 0.5,
                            0.3);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        LevelAccessor levelAccess = event.getLevel();

        if (levelAccess.isClientSide()) return;

        getCriticalStrikeLevel(player).ifPresent(totalLevel -> {
            if (player.getRandom().nextFloat() < totalLevel * 0.01f) {
                ServerLevel serverLevel = (ServerLevel) levelAccess;
                BlockPos pos = event.getPos();

                serverLevel.destroyBlock(pos, true, player);
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        20,
                        (serverLevel.random.nextDouble() * 2.0D - 1.0D) * 0.5D,
                        0.5D + serverLevel.random.nextDouble() * 0.5D,
                        (serverLevel.random.nextDouble() * 2.0D - 1.0D) * 0.5D,
                        0.5);

                event.setCanceled(true);
            }
        });
    }

    private static Optional<Integer> getCriticalStrikeLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack -> {
                    if (stack.getItem() instanceof ModularItem) {
                        ModularItem item = (ModularItem) stack.getItem();
                        return item.getEffectLevel(stack, ItemEffect.get("criticalStrike")) > 0;
                    }
                    return false;
                }))
                .map(list -> list.stream()
                        .mapToInt(curio -> {
                            ModularItem item = (ModularItem) curio.stack().getItem();
                            return item.getEffectLevel(curio.stack(), ItemEffect.get("criticalStrike"));
                        })
                        .sum()
                );
    }
}
