package com.tetra_loopback.network.packet;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class SupercoolingAttackPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger("SupercoolingAttackPacket");

    private final double hitX, hitY, hitZ;
    private final int resonanceStage;

    public SupercoolingAttackPacket(double hitX, double hitY, double hitZ, int resonanceStage) {
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
        this.resonanceStage = resonanceStage;
    }

    public SupercoolingAttackPacket(FriendlyByteBuf buf) {
        this.hitX = buf.readDouble();
        this.hitY = buf.readDouble();
        this.hitZ = buf.readDouble();
        this.resonanceStage = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(hitX);
        buf.writeDouble(hitY);
        buf.writeDouble(hitZ);
        buf.writeInt(resonanceStage);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 使用DistExecutor确保只在客户端执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        ctx.get().setPacketHandled(true);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return;

        //播放组合音效
        playSoundEffects(mc);
        //生成粒子效果
        spawnParticleEffects(mc);
    }

    @OnlyIn(Dist.CLIENT)
    private void playSoundEffects(net.minecraft.client.Minecraft mc) {
        var level = mc.level;
        var player = mc.player;
        if (level == null || player == null) return;

        level.playSound(player, hitX, hitY, hitZ,
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.6f);

        level.playSound(player, hitX, hitY, hitZ,
                SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0f, 0.7f);

        level.playSound(player, hitX, hitY, hitZ,
                SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 0.8f, 0.5f);

        if (resonanceStage >= 2) {
            level.playSound(player, hitX, hitY, hitZ,
                    SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.5f, 1.2f);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticleEffects(net.minecraft.client.Minecraft mc) {
        var level = mc.level;
        if (level == null) return;

        for (int i = 0; i < 20; i++) {
            level.addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                    hitX + (Math.random() - 0.5) * 0.5,
                    hitY + (Math.random() - 0.5) * 0.5,
                    hitZ + (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5
            );
        }

        level.addParticle(ParticleTypes.SWEEP_ATTACK,
                hitX, hitY + 0.5, hitZ,
                0, 0, 0);

        for (int i = 0; i < 15; i++) {
            level.addParticle(ParticleTypes.SNOWFLAKE,
                    hitX + (Math.random() - 0.5),
                    hitY + Math.random(),
                    hitZ + (Math.random() - 0.5),
                    0, -0.05, 0);
        }

        if (resonanceStage >= 3) {
            level.addParticle(ParticleTypes.EXPLOSION,
                    hitX, hitY + 0.5, hitZ,
                    0, 0, 0);
        }

        if (resonanceStage >= 4) {
            for (int i = 0; i < 10; i++) {
                level.addParticle(ParticleTypes.CLOUD,
                        hitX + (Math.random() - 0.5) * 0.3,
                        hitY,
                        hitZ + (Math.random() - 0.5) * 0.3,
                        0, 0.02, 0);
            }
        }
    }
}