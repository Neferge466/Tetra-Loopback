package com.tetra_loopback.effects.curio.astralphase.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientAstralPhaseData {
    private static final Map<Integer, ClientAstralState> clientPlayerStates = new HashMap<>();

    public static class ClientAstralState {
        public boolean isPhasing = false;
        public int phaseTicks = 0;
        public float velocityFactor = 0.0f;
        public float transparency = 1.0f;
    }


    public static void updatePlayerState(int playerId, boolean isPhasing, int phaseTicks, float velocityFactor) {
        ClientAstralState state = clientPlayerStates.computeIfAbsent(playerId, k -> new ClientAstralState());
        state.isPhasing = isPhasing;
        state.phaseTicks = phaseTicks;
        state.velocityFactor = velocityFactor;
        state.transparency = isPhasing ? 0.5f : 1.0f;//相位50%透明
    }

    public static boolean isPlayerPhasing(int playerId) {
        ClientAstralState state = clientPlayerStates.get(playerId);
        return state != null && state.isPhasing;
    }

    public static float getPlayerTransparency(int playerId) {
        ClientAstralState state = clientPlayerStates.get(playerId);
        return state != null ? state.transparency : 1.0f;
    }

    public static ClientAstralState getPlayerState(int playerId) {
        return clientPlayerStates.get(playerId);
    }

    public static void removePlayerState(int playerId) {
        clientPlayerStates.remove(playerId);
    }

    public static void tickClientStates() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            clientPlayerStates.clear();
            return;
        }

        //更新所有客户端状态的剩余时间
        for (Map.Entry<Integer, ClientAstralState> entry : clientPlayerStates.entrySet()) {
            ClientAstralState state = entry.getValue();

            if (state.isPhasing && state.phaseTicks > 0) {
                state.phaseTicks--;
                if (state.phaseTicks <= 0) {
                    state.isPhasing = false;
                    state.transparency = 1.0f;
                }
            }
        }
    }

    public static void clearAllStates() {
        clientPlayerStates.clear();
    }
}