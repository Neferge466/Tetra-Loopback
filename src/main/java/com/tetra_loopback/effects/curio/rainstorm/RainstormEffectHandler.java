package com.tetra_loopback.effects.curio.rainstorm;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.getter.resonance.ResonanceGetter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class RainstormEffectHandler {

    private static final Map<UUID, Set<UUID>> playerHighlightedEntities = new HashMap<>();
    private static final String GLOW_TEAM_PREFIX = "rainstorm_glow_";
    //用于跟踪上次处理时玩家是否在雨中，以便检测状态变化
    private static final Map<UUID, Boolean> playerWasInRain = new HashMap<>();

    //从饰品获取共鸣值（单个最高值）
    private static int getMaxResonanceFromCurios(Player player) {
        ResonanceGetter resonanceGetter = new ResonanceGetter();
        final int[] maxResonance = {0};

        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            inv.findCurios(itemStack ->
                    itemStack.getItem() instanceof ModularItem &&
                            resonanceGetter.getValue(player, itemStack) > 0
            ).forEach(curio -> {
                double resonance = resonanceGetter.getValue(player, curio.stack());
                maxResonance[0] = Math.max(maxResonance[0], (int) Math.round(resonance));
            });
        });

        return maxResonance[0];
    }

    //检查是否下雨
    private static boolean isRaining(Player player) {
        Level level = player.level();
        return level.isRaining();
    }

    //检查玩家是否有暴雨效果并获取等级
    private static int getRainstormLevel(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(itemStack ->
                        itemStack.getItem() instanceof ModularItem &&
                                ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack,
                                        com.tetra_loopback.effects.gui.ModEffectStats.rainstormEffect) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio -> (int) ((ModularItem) curio.stack().getItem())
                                .getEffectLevel(curio.stack(), com.tetra_loopback.effects.gui.ModEffectStats.rainstormEffect))
                        .max()
                        .orElse(0))
                .orElse(0);
    }

    //获取共鸣阶段组
    private static int getResonanceStageGroup(int resonanceValue) {
        if (resonanceValue >= 16) return 4;
        if (resonanceValue >= 11) return 3;
        if (resonanceValue >= 6) return 2;
        if (resonanceValue >= 1) return 1;
        return 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        //只在服务器端处理
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        UUID playerId = player.getUUID();
        int rainstormLevel = getRainstormLevel(player);
        boolean isRainingNow = isRaining(player);
        boolean wasInRain = playerWasInRain.getOrDefault(playerId, false);

        //检测天气状态变化：从下雨变为不下雨
        if (wasInRain && !isRainingNow) {
            //天气变晴，立即移除所有效果
            removeAllEffects(player);
            playerWasInRain.put(playerId, false);
            return;
        }

        //如果当前没有下雨，或没有暴雨效果，确保效果被清除并返回
        if (rainstormLevel == 0 || !isRainingNow) {
            //如果上次记录是在雨中，但这次没效果，也需要清理
            if (wasInRain) {
                removeAllEffects(player);
                playerWasInRain.put(playerId, false);
            }
            return;
        }

        //记录当前在雨中
        playerWasInRain.put(playerId, true);

        int maxResonance = getMaxResonanceFromCurios(player);
        int stageGroup = getResonanceStageGroup(maxResonance);

        applyBaseEffects(player, rainstormLevel, stageGroup);
        applyResonanceEffects(player, stageGroup);
    }

    private static void applyBaseEffects(Player player, int rainstormLevel, int stageGroup) {
        //基础效果持续时间：10秒（200 ticks），足够长以维持，但会在天气变化时被主动清除
        int duration = 200;
        boolean ambient = true;
        boolean showIcon = true; // 调整为true，让玩家能看到图标

        //阶段4时，基础效果等级+1
        int effectLevel = (stageGroup == 4) ? 1 : 0;

        //基础效果：速度I（下雨时）
        //使用唯一的来源标记，便于识别和移除
        applyOrRefreshEffect(player, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, effectLevel, ambient, showIcon));

        if (rainstormLevel >= 2) {
            //二级效果：跳跃提升I
            applyOrRefreshEffect(player, new MobEffectInstance(MobEffects.JUMP, duration, effectLevel, ambient, showIcon));
        }

        if (rainstormLevel >= 3) {
            //三级效果：抗性提升I
            applyOrRefreshEffect(player, new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, effectLevel, ambient, showIcon));
        }
    }

    //应用或刷新效果，避免重复添加造成图标闪烁
    private static void applyOrRefreshEffect(Player player, MobEffectInstance newEffect) {
        //检查是否已有同类型效果
        MobEffectInstance existing = player.getEffect(newEffect.getEffect());
        if (existing != null && existing.getAmplifier() == newEffect.getAmplifier()) {
            //已有相同等级效果，仅刷新持续时间（如果剩余时间较短）
            if (existing.getDuration() < 100) { //如果剩余时间少于5秒
                player.addEffect(new MobEffectInstance(
                        newEffect.getEffect(),
                        newEffect.getDuration(),
                        newEffect.getAmplifier(),
                        newEffect.isAmbient(),
                        newEffect.isVisible(),
                        newEffect.showIcon()
                ));
            }
        } else {
            //没有效果或效果等级不同，直接添加
            player.addEffect(newEffect);
        }
    }

    private static void applyResonanceEffects(Player player, int stageGroup) {
        int duration = 200;
        boolean ambient = true;
        boolean showIcon = true;

        //阶段4时，所有由共鸣触发的效果等级额外+1
        int resonanceExtraLevel = (stageGroup == 4) ? 1 : 0;

        //阶段2 (6-10) 和阶段4 (16-20)：力量
        //阶段2时为力量I(0)，阶段4时为力量II(1)
        if (stageGroup >= 2) {
            applyOrRefreshEffect(player, new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 0 + resonanceExtraLevel, ambient, showIcon));
        }

        //阶段3 (11-15) 和阶段4 (16-20)：高亮周围生物
        //阶段4时同样需要高亮效果
        if (stageGroup >= 3) {
            highlightNearbyEntities(player);
        }
    }

    //移除所有暴雨相关效果
    private static void removeAllEffects(Player player) {
        //移除玩家身上的暴雨相关药水效果
        removeRainstormPotionEffects(player);
        //移除高亮效果
        clearHighlightedEntities(player);
    }

    //移除添加的药水效果
    private static void removeRainstormPotionEffects(Player player) {
        //需要移除的效果列表
        List<net.minecraft.world.effect.MobEffect> effectsToRemove = Arrays.asList(
                MobEffects.MOVEMENT_SPEED,
                MobEffects.JUMP,
                MobEffects.DAMAGE_RESISTANCE,
                MobEffects.DAMAGE_BOOST
        );

        for (net.minecraft.world.effect.MobEffect effect : effectsToRemove) {
            if (player.hasEffect(effect)) {
                player.removeEffect(effect);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        //只在服务器端处理
        if (event.getEntity().level().isClientSide()) return;

        if (event.getSource().getEntity() instanceof Player) {
            Player player = (Player) event.getSource().getEntity();

            if (getRainstormLevel(player) > 0 && isRaining(player)) {
                int maxResonance = getMaxResonanceFromCurios(player);
                int stageGroup = getResonanceStageGroup(maxResonance);

                //阶段1 (1-5) 和阶段4 (16-20)：攻击施加缓慢
                //阶段1-5是阶段1，阶段6-20包括阶段2/3/4，所以条件应为 >=1
                if (stageGroup >= 1) {
                    Entity targetEntity = event.getEntity();
                    if (targetEntity instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) targetEntity;
                        // 阶段4时缓慢等级+1：阶段1时为缓慢I(0)，阶段4时为缓慢II(1)
                        int slownessLevel = (stageGroup == 4) ? 1 : 0;
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, slownessLevel, true, false));
                    }
                }
            }
        }
    }

    //高亮周围生物
    private static void highlightNearbyEntities(Player player) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        Set<UUID> currentlyHighlighted = playerHighlightedEntities.getOrDefault(playerId, new HashSet<>());
        Set<UUID> shouldHighlight = new HashSet<>();

        // 搜索半径12格内的生物
        AABB searchArea = player.getBoundingBox().inflate(12.0);
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                entity -> entity != player && entity.isAlive());

        Scoreboard scoreboard = player.level().getScoreboard();
        String teamName = GLOW_TEAM_PREFIX + playerId.toString().replace("-", "_");

        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setColor(net.minecraft.ChatFormatting.BLUE);
        }

        //高亮当前范围内的生物
        for (LivingEntity entity : nearbyEntities) {
            UUID entityId = entity.getUUID();
            shouldHighlight.add(entityId);

            if (!currentlyHighlighted.contains(entityId)) {
                entity.setGlowingTag(true);
                scoreboard.addPlayerToTeam(entity.getScoreboardName(), team);
            }
        }

        //移除不再范围内的生物的高亮
        for (UUID oldEntityId : currentlyHighlighted) {
            if (!shouldHighlight.contains(oldEntityId)) {
                LivingEntity oldEntity = findEntityByUUID(player.level(), oldEntityId);
                if (oldEntity != null) {
                    oldEntity.setGlowingTag(false);
                    scoreboard.removePlayerFromTeam(oldEntity.getScoreboardName(), team);
                }
            }
        }

        playerHighlightedEntities.put(playerId, shouldHighlight);
    }

    //清除高亮生物
    private static void clearHighlightedEntities(Player player) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        Set<UUID> highlighted = playerHighlightedEntities.remove(playerId);

        if (highlighted != null) {
            Scoreboard scoreboard = player.level().getScoreboard();
            String teamName = GLOW_TEAM_PREFIX + playerId.toString().replace("-", "_");
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);

            for (UUID entityId : highlighted) {
                LivingEntity entity = findEntityByUUID(player.level(), entityId);
                if (entity != null) {
                    entity.setGlowingTag(false);
                    if (team != null) {
                        scoreboard.removePlayerFromTeam(entity.getScoreboardName(), team);
                    }
                }
            }

            if (team != null) {
                scoreboard.removePlayerTeam(team);
            }
        }

        //同时从天气状态跟踪中移除
        playerWasInRain.remove(playerId);
    }

    //通过UUID查找实体
    private static LivingEntity findEntityByUUID(Level level, UUID uuid) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }
}