package com.tetra_loopback.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WindLightMotionPacket {
    private final double motionX;
    private final double motionY;
    private final double motionZ;
    private final Operation operation;

    public enum Operation {
        SET_ABSOLUTE,  //设置绝对速度
        ADD_RELATIVE   //添加相对速度
    }

    public WindLightMotionPacket(double motionX, double motionY, double motionZ, Operation operation) {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.operation = operation;
    }

    public WindLightMotionPacket(FriendlyByteBuf buf) {
        this.motionX = buf.readDouble();
        this.motionY = buf.readDouble();
        this.motionZ = buf.readDouble();
        this.operation = buf.readEnum(Operation.class);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(motionX);
        buf.writeDouble(motionY);
        buf.writeDouble(motionZ);
        buf.writeEnum(operation);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //使用DistExecutor确保只在客户端执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        ctx.get().setPacketHandled(true);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null) {
            switch (operation) {
                case SET_ABSOLUTE:
                    mc.player.setDeltaMovement(motionX, motionY, motionZ);
                    break;
                case ADD_RELATIVE:
                    mc.player.setDeltaMovement(
                            mc.player.getDeltaMovement().x + motionX,
                            mc.player.getDeltaMovement().y + motionY,
                            mc.player.getDeltaMovement().z + motionZ
                    );
                    break;
            }
        }
    }
}