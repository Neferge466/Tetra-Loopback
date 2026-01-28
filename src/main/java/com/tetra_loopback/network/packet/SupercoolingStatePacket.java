package com.tetra_loopback.network.packet;

import com.tetra_loopback.effects.curio.supercooling.client.ClientSupercoolingData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SupercoolingStatePacket {
    private final boolean isActive;
    private final float progress;
    private final int resonanceStage;

    public SupercoolingStatePacket(boolean isActive, float progress, int resonanceStage) {
        this.isActive = isActive;
        this.progress = progress;
        this.resonanceStage = resonanceStage;
    }

    public SupercoolingStatePacket(FriendlyByteBuf buf) {
        this.isActive = buf.readBoolean();
        this.progress = buf.readFloat();
        this.resonanceStage = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isActive);
        buf.writeFloat(progress);
        buf.writeInt(resonanceStage);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //使用DistExecutor确保只在客户端执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                //更新本地玩家的过冷状态
                ClientSupercoolingData.updateState(isActive, progress, resonanceStage);
            });
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}