package com.tetra_loopback.events;



import com.tetra_loopback.effects.TLbEffects;
import com.tetra_loopback.effects.ThunderEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "tetra_loopback")
public class ThunderPotionHanlder {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (event.getAmount() > 0.0f &&
                    !event.isCanceled() &&
                    attacker.hasEffect(TLbEffects.THUNDER.get())) {

                ThunderEffect.tryTriggerThunder(attacker, event.getEntity());
            }
        }
    }
}
