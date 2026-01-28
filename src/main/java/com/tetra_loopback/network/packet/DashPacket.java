// DashPacket.java
package com.tetra_loopback.network.packet;

import com.tetra_loopback.effects.curio.windlight.WindLightEffectHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class DashPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger("DashPacket");
    private final int direction;

    public DashPacket(int direction) {
        this.direction = direction;
    }

    public DashPacket(FriendlyByteBuf buf) {
        this.direction = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(direction);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                WindLightEffectHandler.handleDash(player, direction);
            }
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}