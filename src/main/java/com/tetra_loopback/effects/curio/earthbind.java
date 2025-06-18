package com.tetra_loopback.effects.curio;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.potion.EarthboundPotionEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class earthbind {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            int totalLevel = getCombinedEarthbindLevel(attacker);
            LivingEntity target = event.getEntity();
            Level world = target.getCommandSenderWorld();

            if (totalLevel > 0 && attacker.getRandom().nextFloat() < Math.max(0.1, 0.5 * (1 - target.getY() / 128))) {
                target.addEffect(new MobEffectInstance(EarthboundPotionEffect.instance, totalLevel * 20, 0, false, true));

                if (world instanceof ServerLevel serverLevel) {
                    BlockState blockState = world.getBlockState(target.blockPosition().below());
                    serverLevel.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                            target.getX(),
                            target.getY() + 0.1,
                            target.getZ(),
                            16,
                            0,
                            world.getRandom().nextGaussian() * 0.2,
                            0,
                            0.1
                    );
                }
            }
        }
    }
    private static int getCombinedEarthbindLevel(Player player) {
        return getCurioEarthbindLevel(player) + getWeaponEarthbindLevel(player);
    }

    private static int getCurioEarthbindLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.earthbind) > 0))
                .map(list -> list.stream()
                        .mapToInt(curio -> ((ModularItem) curio.stack().getItem())
                                .getEffectLevel(curio.stack(), ItemEffect.earthbind))
                        .sum())
                .orElse(0);
    }

    private static int getWeaponEarthbindLevel(Player player) {
        return Optional.of(player.getMainHandItem())
                .filter(stack -> stack.getItem() instanceof ModularItem)
                .map(stack -> ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.earthbind))
                .orElse(0);
    }
}
