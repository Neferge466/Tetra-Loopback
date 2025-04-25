package com.tetra_loopback.effects.curio;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class FlameAdmonitionHandler {
    private static final UUID FIRE_RESIST_UUID = UUID.fromString("d9aae8a1-1234-5678-9abc-def012345679");

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getSource().is(DamageTypeTags.IS_FIRE) && hasEffect(player)) {
                event.setAmount(event.getAmount() * 0.3f);
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (hasEffect(player)) {
            event.getTarget().setSecondsOnFire(3);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level().isClientSide) {
            boolean shouldHaveEffect = hasEffect(player) && player.isOnFire();

            MobEffectInstance currentEffect = player.getEffect(MobEffects.DAMAGE_BOOST);
            if (shouldHaveEffect) {
                if (currentEffect == null || currentEffect.getAmplifier() < 1) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST,
                            120,  //40 2
                            1,   //1 II
                            false, false, true));
                }
            } else if (currentEffect != null && currentEffect.getAmplifier() == 1) {
                player.removeEffect(MobEffects.DAMAGE_BOOST);
            }
        }
    }

    private static boolean hasEffect(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(itemStack ->
                        itemStack.getItem() instanceof ModularItem &&
                                ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack, ModEffectStats.flameAdmonitionEffect) > 0
                ))
                .map(list -> !list.isEmpty())
                .orElse(false);
    }
}
