package com.tetra_loopback.effects.curio;


import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class FrostCrownEffect {
    private static final String FREEZE_DAMAGE = "freeze";

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && hasEffect(player)) {
             if (event.getSource().is(DamageTypeTags.IS_FREEZING)) {
               event.setCanceled(true);
             }
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && hasEffect(player)) {
            LivingEntity attacker = null;
            if (event.getSource().getEntity() instanceof LivingEntity) {
                attacker = (LivingEntity) event.getSource().getEntity();
            }

            if (attacker != null) {
                attacker.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        100,  // 5秒
                        1,    // II
                        false, false, true));
            }
        }
    }

    private static boolean hasEffect(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(itemStack ->
                        itemStack.getItem() instanceof ModularItem &&
                                ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack, ModEffectStats.frostCrownEffect) > 0
                ))
                .map(list -> !list.isEmpty())
                .orElse(false);
    }
}
