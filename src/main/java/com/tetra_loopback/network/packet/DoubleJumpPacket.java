package com.tetra_loopback.network.packet;

import com.tetra_loopback.effects.curio.windlight.WindLightEffectHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class DoubleJumpPacket {

    private static final Logger LOGGER = LoggerFactory.getLogger("DoubleJumpPacket");

    public DoubleJumpPacket() {}

    public DoubleJumpPacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                WindLightEffectHandler.handleDoubleJump(player);
            }
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}