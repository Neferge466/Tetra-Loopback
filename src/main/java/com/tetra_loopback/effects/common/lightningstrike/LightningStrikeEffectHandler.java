package com.tetra_loopback.effects.common.lightningstrike;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class LightningStrikeEffectHandler {

    private static final Map<UUID, Map<UUID, GlowData>> playerGlowData = new ConcurrentHashMap<>();
    private static final String LIGHTNING_TEAM_PREFIX = "lightning_strike_";

    //概率配置
    private static final float[] TRIGGER_PROBABILITIES = {
            0.10f, //1
            0.25f, //2
            0.45f, //3
            0.70f, //4
            1.00f  //5
    };

    //伤害
    private static final float BASE_LIGHTNING_DAMAGE = 3.0f;
    private static final float LEVEL_DAMAGE_MULTIPLIER = 1.8f;

    private static final int BASE_GLOW_DURATION = 100;   //5秒
    private static final int BASE_SLOWNESS_DURATION = 80; //4秒
    private static final int DURATION_PER_LEVEL = 20;    //每级增加1秒

    private static final int[] SLOWNESS_AMPLIFIERS = {0, 0, 1, 1, 2};

    private static final ChatFormatting GLOW_COLOR = ChatFormatting.BLUE;
    private static final Random RANDOM = new Random();

    public static int getLightningStrikeLevel(Player player) {
        int maxLevel = 0;
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof ModularItem) {
            int level = (int) ((ModularItem) mainHand.getItem())
                    .getEffectLevel(mainHand, ModEffectStats.lightningStrikeEffect);
            maxLevel = Math.max(maxLevel, level);
        }
        var curiosInventory = CuriosApi.getCuriosInventory(player);
        if (curiosInventory.isPresent()) {
            var inventory = curiosInventory.resolve().orElse(null);
            if (inventory != null) {
                var curios = inventory.findCurios(itemStack ->
                        itemStack.getItem() instanceof ModularItem &&
                                ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack,
                                        ModEffectStats.lightningStrikeEffect) > 0
                );

                for (SlotResult slotResult : curios) {
                    int level = (int) ((ModularItem) slotResult.stack().getItem())
                            .getEffectLevel(slotResult.stack(), ModEffectStats.lightningStrikeEffect);
                    maxLevel = Math.max(maxLevel, level);
                }
            }
        }

        return Math.min(maxLevel, 5);
    }
    private static boolean shouldTriggerLightning(int effectLevel) {
        if (effectLevel < 1 || effectLevel > 5) return false;

        float probability = TRIGGER_PROBABILITIES[effectLevel - 1];
        return RANDOM.nextFloat() < probability;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity target = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();

        Player player = null;
        if (sourceEntity instanceof Player) {
            player = (Player) sourceEntity;
        }
        else if (sourceEntity instanceof Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof Player) {
                player = (Player) owner;
            }
        }

        if (player == null) return;

        int lightningLevel = getLightningStrikeLevel(player);
        if (lightningLevel < 1) return;

        if (!shouldTriggerLightning(lightningLevel)) return;

        //闪电
        summonLightningBolt(target, lightningLevel);
        float extraDamage = calculateLightningDamage(lightningLevel);
        event.setAmount(event.getAmount() + extraDamage);

        applyLightningEffects(player, target, lightningLevel);

        playLightningEffects((ServerLevel) target.level(), target, lightningLevel);
    }

    //闪电
    private static void summonLightningBolt(LivingEntity target, int lightningLevel) {
        if (target.level().isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) target.level();

        //创建闪电实体
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (lightningBolt != null) {
            //设置目标位置
            lightningBolt.moveTo(target.getX(), target.getY(), target.getZ());

            if (lightningLevel >= 4) {
                lightningBolt.setVisualOnly(false);
            } else {
                lightningBolt.setVisualOnly(true);
            }

            if (lightningLevel >= 3) {
                lightningBolt.setCause(null);
            }

            serverLevel.addFreshEntity(lightningBolt);
        }
    }

    private static float calculateLightningDamage(int lightningLevel) {
        return BASE_LIGHTNING_DAMAGE + (lightningLevel - 1) * LEVEL_DAMAGE_MULTIPLIER;
    }

    private static void applyLightningEffects(Player player, LivingEntity target, int lightningLevel) {
        applyGlowingEffect(player, target, lightningLevel);
        applySlownessEffect(target, lightningLevel);

        if (lightningLevel >= 4) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    BASE_SLOWNESS_DURATION + (lightningLevel * DURATION_PER_LEVEL),
                    0,
                    false, true, true
            ));
        }

        if (lightningLevel >= 5) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    BASE_GLOW_DURATION + (lightningLevel * DURATION_PER_LEVEL),
                    0,
                    false, true, true
            ));
        }
    }

    private static void applyGlowingEffect(Player player, LivingEntity target, int lightningLevel) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        UUID targetId = target.getUUID();
        ChatFormatting glowColor = GLOW_COLOR;
        Scoreboard scoreboard = player.level().getScoreboard();
        String teamName = LIGHTNING_TEAM_PREFIX + playerId.toString().replace("-", "_") + "_" + lightningLevel;

        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setColor(glowColor);
        }

        int glowDuration = BASE_GLOW_DURATION + (lightningLevel * DURATION_PER_LEVEL);

        target.setGlowingTag(true);
        scoreboard.addPlayerToTeam(target.getScoreboardName(), team);

        Map<UUID, GlowData> playerData = playerGlowData.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        playerData.put(targetId, new GlowData(teamName, System.currentTimeMillis(), glowDuration));

        removeFromOtherTeams(player, target, teamName);
    }

    //从其他团队中移除实体
    private static void removeFromOtherTeams(Player player, LivingEntity target, String currentTeamName) {
        Scoreboard scoreboard = player.level().getScoreboard();

        for (PlayerTeam team : scoreboard.getPlayerTeams()) {
            String teamName = team.getName();
            if (teamName.startsWith(LIGHTNING_TEAM_PREFIX) && !teamName.equals(currentTeamName)) {
                if (team.getPlayers().contains(target.getScoreboardName())) {
                    scoreboard.removePlayerFromTeam(target.getScoreboardName(), team);
                }
            }
        }
    }

    private static void applySlownessEffect(LivingEntity target, int lightningLevel) {
        int duration = BASE_SLOWNESS_DURATION + (lightningLevel * DURATION_PER_LEVEL);
        int amplifier = SLOWNESS_AMPLIFIERS[lightningLevel - 1];

        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                duration,
                amplifier,
                false,
                true,
                true
        ));
    }

    private static void playLightningEffects(ServerLevel level, LivingEntity target, int lightningLevel) {
        level.playSound(null, target.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER,
                0.8f + (lightningLevel * 0.05f), 0.9f + level.random.nextFloat() * 0.2f);

        level.playSound(null, target.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER,
                0.9f, 1.0f + level.random.nextFloat() * 0.2f);

        int particleCount = 15 + (lightningLevel * 5);
        for (int i = 0; i < particleCount; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.5;
            double offsetY = level.random.nextDouble() * target.getBbHeight();
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.5;

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    2, 0.15, 0.15, 0.15, 0.08);

            //蓝色火焰
            if (lightningLevel >= 3 && i % 3 == 0) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX() + offsetX * 0.7,
                        target.getY() + offsetY,
                        target.getZ() + offsetZ * 0.7,
                        1, 0.08, 0.08, 0.08, 0.02);
            }
        }

        //爆炸粒子
        if (lightningLevel >= 3) {
            level.sendParticles(ParticleTypes.EXPLOSION,
                    target.getX(),
                    target.getY() + target.getBbHeight() / 2,
                    target.getZ(),
                    1, 0.6, 0.6, 0.6, 0.0);
        }

        if (lightningLevel >= 5) {
            for (int i = 0; i < 8; i++) {
                double angle = (i * 45) * Math.PI / 180;
                double radius = 1.5;

                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        target.getX() + Math.cos(angle) * radius,
                        target.getY() + target.getBbHeight() * 0.8,
                        target.getZ() + Math.sin(angle) * radius,
                        3, 0.2, 0.2, 0.2, 0.1);
            }
        }
    }

    //定期清理
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (event.getServer().getTickCount() % 100 == 0) {
            cleanupExpiredGlowEffects(event.getServer());
        }
    }

    private static void cleanupExpiredGlowEffects(net.minecraft.server.MinecraftServer server) {
        long currentTime = System.currentTimeMillis();

        for (var playerEntry : playerGlowData.entrySet()) {
            UUID playerId = playerEntry.getKey();
            Map<UUID, GlowData> targetData = playerEntry.getValue();

            Player player = server.getPlayerList().getPlayer(playerId);
            if (player == null) {
                cleanupOfflinePlayerGlowEffects(playerId, server);
                continue;
            }

            Scoreboard scoreboard = player.level().getScoreboard();
            Iterator<Map.Entry<UUID, GlowData>> iterator = targetData.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, GlowData> entry = iterator.next();
                UUID targetId = entry.getKey();
                GlowData glowData = entry.getValue();

                if (currentTime - glowData.startTime > glowData.duration * 50L) { // ticks to ms
                    LivingEntity target = findEntityByUUID(player.level(), targetId);
                    if (target != null) {
                        PlayerTeam team = scoreboard.getPlayerTeam(glowData.teamName);
                        if (team != null) {
                            target.setGlowingTag(false);
                            scoreboard.removePlayerFromTeam(target.getScoreboardName(), team);
                            if (team.getPlayers().isEmpty()) {
                                scoreboard.removePlayerTeam(team);
                            }
                        }
                    }
                    iterator.remove();
                }
            }
            if (targetData.isEmpty()) {
                playerGlowData.remove(playerId);
            }
        }
    }

    private static void cleanupOfflinePlayerGlowEffects(UUID playerId, net.minecraft.server.MinecraftServer server) {
        Map<UUID, GlowData> targetData = playerGlowData.remove(playerId);
        if (targetData != null) {
            for (GlowData glowData : targetData.values()) {
                for (ServerLevel level : server.getAllLevels()) {
                    Scoreboard scoreboard = level.getScoreboard();
                    PlayerTeam team = scoreboard.getPlayerTeam(glowData.teamName);
                    if (team != null) {
                        scoreboard.removePlayerTeam(team);
                    }
                }
            }
        }
    }

    private static LivingEntity findEntityByUUID(net.minecraft.world.level.Level level, UUID uuid) {
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            cleanupPlayerGlowEffects(event.getEntity());
        }
    }

    private static void cleanupPlayerGlowEffects(Player player) {
        UUID playerId = player.getUUID();
        Map<UUID, GlowData> targetData = playerGlowData.remove(playerId);

        if (targetData != null) {
            Scoreboard scoreboard = player.level().getScoreboard();

            for (GlowData glowData : targetData.values()) {
                PlayerTeam team = scoreboard.getPlayerTeam(glowData.teamName);
                if (team != null) {
                    List<String> players = new ArrayList<>(team.getPlayers());
                    for (String playerName : players) {
                        scoreboard.removePlayerFromTeam(playerName, team);
                    }
                    scoreboard.removePlayerTeam(team);
                }
            }
        }
    }

    private static class GlowData {
        final String teamName;
        final long startTime;
        final int duration;

        GlowData(String teamName, long startTime, int duration) {
            this.teamName = teamName;
            this.startTime = startTime;
            this.duration = duration;
        }
    }
}