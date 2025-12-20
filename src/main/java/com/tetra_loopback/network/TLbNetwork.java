package com.tetra_loopback.network;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.network.packet.TransferRecipePacket;
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
    }

    public static int nextId() {
        return packetId++;
    }
}