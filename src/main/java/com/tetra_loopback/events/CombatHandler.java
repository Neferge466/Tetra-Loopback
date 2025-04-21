package com.tetra_loopback.events;

import com.tetra_loopback.effects.curio.ThunderingCurioEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "tetra_loopback")
public class CombatHandler {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        //check if the attacker is a player
        if (event.getSource().getEntity() instanceof Player player) {
            ThunderingCurioEffect.tryTriggerThunder(player, event.getEntity());
        }
    }
}