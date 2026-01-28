package com.tetra_loopback.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AstralPhaseParticlePacket {
    private final double x;
    private final double y;
    private final double z;
    private final boolean isDodge;
    private final boolean isEntering;
    private final int effectLevel;

    public AstralPhaseParticlePacket(double x, double y, double z, boolean isDodge, boolean isEntering, int effectLevel) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.isDodge = isDodge;
        this.isEntering = isEntering;
        this.effectLevel = effectLevel;
    }

    public AstralPhaseParticlePacket(FriendlyByteBuf buffer) {
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.isDodge = buffer.readBoolean();
        this.isEntering = buffer.readBoolean();
        this.effectLevel = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeBoolean(isDodge);
        buffer.writeBoolean(isEntering);
        buffer.writeInt(effectLevel);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        context.get().setPacketHandled(true);
    }

    private void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        ClientLevel level = mc.level;
        RandomSource random = level.random;

        int particleCount = 20 + effectLevel * 5;

        if (isEntering) {
            //进入相位
            for (int i = 0; i < particleCount; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 2.0;
                double offsetY = (random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (random.nextDouble() - 0.5) * 2.0;

                //传送门粒子
                level.addParticle(ParticleTypes.PORTAL,
                        x + offsetX, y + offsetY, z + offsetZ,
                        (random.nextDouble() - 0.5) * 0.1,
                        random.nextDouble() * 0.1,
                        (random.nextDouble() - 0.5) * 0.1);

                //末地烛粒子
                if (i % 3 == 0) {
                    level.addParticle(ParticleTypes.END_ROD,
                            x + offsetX * 0.5, y + offsetY * 0.5, z + offsetZ * 0.5,
                            0, 0.05, 0);
                }

                //灵魂火焰粒子
                if (i % 4 == 0) {
                    level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            x + offsetX, y + offsetY, z + offsetZ,
                            0, 0.02, 0);
                }

                //白色粒子
                if (i % 5 == 0) {
                    level.addParticle(ParticleTypes.GLOW_SQUID_INK,
                            x + offsetX, y + offsetY, z + offsetZ,
                            0, 0.03, 0);
                }

                //气泡柱向上粒子
                if (i % 6 == 0) {
                    level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP,
                            x + offsetX * 0.7, y + offsetY * 0.7, z + offsetZ * 0.7,
                            0, 0.2, 0);
                }
            }

            if (!isDodge && mc.player != null) {
                double distance = mc.player.distanceToSqr(x, y, z);
                float volume = (float) Math.max(0.1, 1.0 - distance / 100.0);

                if (volume > 0.1) {
                    //末影人传送
                    level.playLocalSound(x, y, z,
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                            volume, 2.0f, false);

                    //延迟紫水晶
                    mc.execute(() -> {
                        if (mc.level != null && mc.player != null) {
                            double currentDistance = mc.player.distanceToSqr(x, y, z);
                            float currentVolume = (float) Math.max(0.1, 1.0 - currentDistance / 100.0);

                            if (currentVolume > 0.1) {
                                level.playLocalSound(x, y, z,
                                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                                        currentVolume * 0.75f, 1.2f, false);
                            }
                        }
                    });
                }
            }
        } else {
            double dirX = 0;
            double dirY = 0;
            double dirZ = 0;

            if (mc.player != null) {
                dirX = mc.player.getLookAngle().x;
                dirY = 0; //水平推进
                dirZ = mc.player.getLookAngle().z;
                double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
                if (length > 0) {
                    dirX /= length;
                    dirZ /= length;
                }
            }

            for (int i = 0; i < particleCount; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 1.5;
                double offsetY = (random.nextDouble() - 0.5) * 1.5;
                double offsetZ = (random.nextDouble() - 0.5) * 1.5;

                //烟花粒子
                level.addParticle(ParticleTypes.FIREWORK,
                        x + offsetX, y + offsetY, z + offsetZ,
                        dirX * 0.3 + (random.nextDouble() - 0.5) * 0.1,
                        dirY * 0.3 + random.nextDouble() * 0.1,
                        dirZ * 0.3 + (random.nextDouble() - 0.5) * 0.1);

                //电火花粒子
                if (i % 2 == 0) {
                    level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                            x + offsetX, y + offsetY, z + offsetZ,
                            dirX * 0.2 + (random.nextDouble() - 0.5) * 0.15,
                            dirY * 0.2 + random.nextDouble() * 0.15,
                            dirZ * 0.2 + (random.nextDouble() - 0.5) * 0.15);
                }

                //烟雾粒子
                if (i % 3 == 0) {
                    level.addParticle(ParticleTypes.SMOKE,
                            x + offsetX, y + offsetY, z + offsetZ,
                            dirX * 0.1,
                            dirY * 0.1,
                            dirZ * 0.1);
                }

                if (i % 4 == 0) {
                    level.addParticle(ParticleTypes.GLOW,
                            x + offsetX, y + offsetY, z + offsetZ,
                            dirX * 0.15,
                            dirY * 0.15,
                            dirZ * 0.15);
                }
            }

            //播放推进音效
            if (mc.player != null) {
                double distance = mc.player.distanceToSqr(x, y, z);
                float volume = (float) Math.max(0.1, 1.0 - distance / 100.0);

                if (volume > 0.1) {
                    level.playLocalSound(x, y, z,
                            SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS,
                            volume, 1.5f, false);
                }
            }
        }
    }
}