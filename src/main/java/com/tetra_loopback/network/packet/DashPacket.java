package com.tetra_loopback.network.packet;

import com.tetra_loopback.effects.curio.windlight.WindLightEffectHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class DashPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger("DashPacket");
    private final double x;
    private final double y;
    private final double z;
    private final int direction;

    public DashPacket(double x, double y, double z, int direction) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
    }

    public DashPacket(int direction) {
        this(0, 0, 0, direction);
    }

    public DashPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.direction = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeInt(direction);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (x != 0 || y != 0 || z != 0) {
                    //使用客户端计算的向量
                    WindLightEffectHandler.handleDash(player, new Vec3(x, y, z));
                } else {
                    WindLightEffectHandler.handleDash(player, direction);
                }
            }
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}