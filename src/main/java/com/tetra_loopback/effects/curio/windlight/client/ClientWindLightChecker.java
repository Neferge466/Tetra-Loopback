package com.tetra_loopback.effects.curio.windlight.client;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class ClientWindLightChecker {

    //客户端检查是否有效果
    public static boolean hasWindLightEffect(Player player) {
        if (player == null) return false;
        if (player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            return true;
        }

        return false;
    }
}