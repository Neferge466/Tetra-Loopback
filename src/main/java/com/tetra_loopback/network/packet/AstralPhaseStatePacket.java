package com.tetra_loopback.network.packet;

import com.tetra_loopback.effects.curio.astralphase.client.ClientAstralPhaseData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AstralPhaseStatePacket {
    private final int playerId;
    private final boolean isPhasing;
    private final int phaseTicks;
    private final float velocityFactor;

    public AstralPhaseStatePacket(int playerId, boolean isPhasing, int phaseTicks, float velocityFactor) {
        this.playerId = playerId;
        this.isPhasing = isPhasing;
        this.phaseTicks = phaseTicks;
        this.velocityFactor = velocityFactor;
    }

    public AstralPhaseStatePacket(FriendlyByteBuf buffer) {
        this.playerId = buffer.readInt();
        this.isPhasing = buffer.readBoolean();
        this.phaseTicks = buffer.readInt();
        this.velocityFactor = buffer.readFloat();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(playerId);
        buffer.writeBoolean(isPhasing);
        buffer.writeInt(phaseTicks);
        buffer.writeFloat(velocityFactor);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            //仅在客户端处理
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        context.get().setPacketHandled(true);
    }

    private void handleClient() {
        //更新客户端状态存储
        ClientAstralPhaseData.updatePlayerState(playerId, isPhasing, phaseTicks, velocityFactor);
    }

    //Getter方法
    public int getPlayerId() {
        return playerId;
    }

    public boolean isPhasing() {
        return isPhasing;
    }

    public int getPhaseTicks() {
        return phaseTicks;
    }

    public float getVelocityFactor() {
        return velocityFactor;
    }
}