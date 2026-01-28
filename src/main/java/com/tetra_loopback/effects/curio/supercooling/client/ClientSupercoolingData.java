package com.tetra_loopback.effects.curio.supercooling.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientSupercoolingData {
    private static final Map<UUID, PlayerSupercoolState> clientPlayerStates = new HashMap<>();

    public static class PlayerSupercoolState {
        public boolean isActive = false;
        public float progress = 0.0f;
        public int resonanceStage = 0;
    }

    public static void updateState(UUID playerId, boolean isActive, float progress, int resonanceStage) {
        //清除状态
        if (!isActive && progress <= 0.0f && resonanceStage == 0) {
            PlayerSupercoolState removedState = clientPlayerStates.remove(playerId);
            if (removedState != null) {
                System.out.println("[Supercooling] Cleared client state for player (cleanup packet)");
            }
            return;
        }

        PlayerSupercoolState state = clientPlayerStates.computeIfAbsent(playerId, k -> new PlayerSupercoolState());

        //检查状态变化
        boolean changed = (state.isActive != isActive ||
                Math.abs(state.progress - progress) > 0.001f ||
                state.resonanceStage != resonanceStage);

        state.isActive = isActive;
        state.progress = progress;
        state.resonanceStage = resonanceStage;
    }

    public static void updateState(boolean isActive, float progress, int resonanceStage) {
        UUID localPlayerId = getLocalPlayerId();
        if (localPlayerId != null) {
            updateState(localPlayerId, isActive, progress, resonanceStage);
        }
    }

    public static PlayerSupercoolState getState(UUID playerId) {
        return clientPlayerStates.get(playerId);
    }

    public static PlayerSupercoolState getLocalPlayerState() {
        UUID localPlayerId = getLocalPlayerId();
        if (localPlayerId == null) return null;
        return getState(localPlayerId);
    }

    private static UUID getLocalPlayerId() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return null;
        return mc.player.getUUID();
    }

    public static void cleanup() {
        clientPlayerStates.clear();
    }

    public static void clearLocalPlayerState() {
        UUID localPlayerId = getLocalPlayerId();
        if (localPlayerId != null) {
            clientPlayerStates.remove(localPlayerId);
            System.out.println("[Supercooling] Manually cleared local player state");
        }
    }
}