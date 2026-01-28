package com.tetra_loopback.network;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.network.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class TLbNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel CHANNEL;
    private static int packetId = 0;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Tetra_loopback.MODID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        //注册数据包
        registerPackets();
    }

    private static void registerPackets() {
        CHANNEL.registerMessage(
                packetId++,
                TransferRecipePacket.class,
                TransferRecipePacket::encode,
                TransferRecipePacket::decode,
                TransferRecipePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                DoubleJumpPacket.class,
                DoubleJumpPacket::encode,
                DoubleJumpPacket::new,
                DoubleJumpPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                DashPacket.class,
                DashPacket::encode,
                DashPacket::new,
                DashPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                WindLightMotionPacket.class,
                WindLightMotionPacket::encode,
                WindLightMotionPacket::new,
                WindLightMotionPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                FastFallPacket.class,
                FastFallPacket::encode,
                FastFallPacket::new,
                FastFallPacket::handle
        );


        CHANNEL.registerMessage(
                packetId++,
                SupercoolingStatePacket.class,
                SupercoolingStatePacket::encode,
                SupercoolingStatePacket::new,
                SupercoolingStatePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                SupercoolingAttackPacket.class,
                SupercoolingAttackPacket::encode,
                SupercoolingAttackPacket::new,
                SupercoolingAttackPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                AstralPhaseStatePacket.class,
                AstralPhaseStatePacket::encode,
                AstralPhaseStatePacket::new,
                AstralPhaseStatePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                AstralPhaseParticlePacket.class,
                AstralPhaseParticlePacket::encode,
                AstralPhaseParticlePacket::new,
                AstralPhaseParticlePacket::handle
        );




    }

    public static int nextId() {
        return packetId++;
    }
}