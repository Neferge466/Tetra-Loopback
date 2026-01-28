package com.tetra_loopback.effects.curio.astralphase;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import com.tetra_loopback.network.TLbNetwork;
import com.tetra_loopback.network.packet.AstralPhaseStatePacket;
import com.tetra_loopback.network.packet.AstralPhaseParticlePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class AstralPhaseEffectHandler {
    private static final Map<UUID, PlayerAstralState> playerStates = new HashMap<>();
    private static final Map<UUID, Long> lastPhaseTime = new HashMap<>();

    private static final float BASE_TRIGGER_CHANCE = 0.15f;      //基础触发几率15%
    private static final float SPEED_CHANCE_MULTIPLIER = 0.01f;  //每m/s增加的触发几率
    private static final float MAX_SPEED_BONUS = 0.20f;          // 最大速度加成
    private static final int PHASE_DURATION = 60;
    private static final float PUSH_STRENGTH = 1.8f;             //推进强度
    private static final int MIN_PHASE_COOLDOWN = 40;            //最小冷却

    // 增强粒子效果的参数
    private static final int ENTER_PARTICLE_COUNT = 25;          //进入相位的粒子数
    private static final float ENTER_PARTICLE_SPREAD = 2.0f;     //粒子扩散范围

    public static class PlayerAstralState {
        public boolean isPhasing = false;               //是否处于相位偏移状态
        public int phaseTicks = 0;                      //相位状态剩余ticks
        public float velocityFactor = 0.0f;             //速度加成因子
        public Vec3 lastMoveDirection = Vec3.ZERO;      //最后移动方向
        public long lastPhaseTime = 0;                  //上次触发时间
        public boolean hasTriggeredThisTick = false;    //防止单tick重复触发
    }

    public static int getAstralPhaseLevel(Player player) {
        if (player == null) return 0;
        try {
            return CuriosApi.getCuriosInventory(player)
                    .map(inv -> inv.findCurios(itemStack ->
                            itemStack.getItem() instanceof ModularItem &&
                                    ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack,
                                            ModEffectStats.astralPhaseEffect) > 0
                    ))
                    .map(list -> list.stream()
                            .mapToInt(curio -> (int) ((ModularItem) curio.stack().getItem())
                                    .getEffectLevel(curio.stack(), ModEffectStats.astralPhaseEffect))
                            .max()
                            .orElse(0))
                    .orElse(0);
        } catch (Exception e) {
            return 0;
        }
    }

    private static float calculateDynamicChance(Player player) {
        Vec3 velocity = player.getDeltaMovement();
        float horizontalSpeed = (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        float speedBonus = Math.min(horizontalSpeed * SPEED_CHANCE_MULTIPLIER, MAX_SPEED_BONUS);
        return BASE_TRIGGER_CHANCE + speedBonus;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        UUID playerId = player.getUUID();

        int astralLevel = getAstralPhaseLevel(player);
        if (astralLevel == 0) {
            cleanupPlayerState(player);
            return;
        }

        PlayerAstralState state = playerStates.computeIfAbsent(playerId, k -> new PlayerAstralState());

        //重置单tick标记
        state.hasTriggeredThisTick = false;

        //记录移动方向
        if (player.getDeltaMovement().lengthSqr() > 0.001) {
            state.lastMoveDirection = player.getDeltaMovement().normalize();
        }

        //更新相位状态
        if (state.isPhasing) {
            state.phaseTicks--;

            if (player.swinging) {
                exitPhaseShift(player, state, true);
            }

            if (state.phaseTicks <= 0) {
                exitPhaseShift(player, state, false);
            }
        }
        state.velocityFactor = calculateDynamicChance(player);
        sendStateToClient(player, state);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) ||
                event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            return;
        }

        UUID playerId = player.getUUID();

        int astralLevel = getAstralPhaseLevel(player);
        if (astralLevel == 0) return;

        PlayerAstralState state = playerStates.get(playerId);
        if (state == null) return;

        //免疫伤害
        if (state.isPhasing) {
            event.setCanceled(true);
            //播放规避粒子
            sendParticlePacket(player, true, true);
            return;
        }

        long currentTime = player.level().getGameTime();
        if (state.lastPhaseTime > 0 && (currentTime - state.lastPhaseTime) < MIN_PHASE_COOLDOWN) {
            return;
        }

        if (state.hasTriggeredThisTick) return;

        float triggerChance = state.velocityFactor;

        if (player.getRandom().nextFloat() < triggerChance) {
            state.isPhasing = true;
            state.phaseTicks = PHASE_DURATION;
            state.hasTriggeredThisTick = true;
            state.lastPhaseTime = currentTime;

            event.setCanceled(true);

            triggerPhaseShiftEffect(player);
            sendStateToClient(player, state);
        }
    }


    private static void triggerPhaseShiftEffect(Player player) {
        if (player.level().isClientSide()) return;

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 2.0f);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.2f);

        sendParticlePacket(player, false, true);
    }


    private static void exitPhaseShift(Player player, PlayerAstralState state, boolean earlyExit) {
        state.isPhasing = false;
        state.phaseTicks = 0;

        if (!earlyExit) {
            applyPhasePush(player, state);
            resetFallDamage(player);
        }

        triggerPhaseExitEffect(player, earlyExit);
        sendStateToClient(player, state);
    }

    private static void triggerPhaseExitEffect(Player player, boolean earlyExit) {
        if (player.level().isClientSide()) return;

        if (!earlyExit) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.7f, 1.5f);
        }
        sendParticlePacket(player, false, false);
    }


    private static void applyPhasePush(Player player, PlayerAstralState state) {
        Vec3 pushDirection = state.lastMoveDirection;

        //使用视线方向
        if (pushDirection.lengthSqr() < 0.001) {
            pushDirection = player.getLookAngle();
        }

        //水平推进
        Vec3 horizontalPush = new Vec3(pushDirection.x, 0, pushDirection.z)
                .normalize()
                .scale(PUSH_STRENGTH);

        player.setDeltaMovement(
                player.getDeltaMovement().add(horizontalPush.x, 0, horizontalPush.z)
        );

        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, 10, 1, false, false, false
        ));
    }

    private static void resetFallDamage(Player player) {
        player.fallDistance = 0f;
    }

    private static void sendStateToClient(Player player, PlayerAstralState state) {
        if (player.level().isClientSide()) return;

        TLbNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new AstralPhaseStatePacket(
                        player.getId(),
                        state.isPhasing,
                        state.phaseTicks,
                        state.velocityFactor
                )
        );
    }

    private static void sendParticlePacket(Player player, boolean isDodge, boolean isEntering) {
        if (player.level().isClientSide()) return;

        int astralLevel = getAstralPhaseLevel(player);
        if (astralLevel <= 0) return;

        TLbNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new AstralPhaseParticlePacket(
                        player.getX(),
                        player.getY() + player.getBbHeight() * 0.5, //玩家身体中部
                        player.getZ(),
                        isDodge,
                        isEntering,
                        astralLevel
                )
        );
    }

    private static void cleanupPlayerState(Player player) {
        playerStates.remove(player.getUUID());
        lastPhaseTime.remove(player.getUUID());
    }

    public static void cleanupPlayer(UUID playerId) {
        playerStates.remove(playerId);
        lastPhaseTime.remove(playerId);
    }

    public static boolean isPlayerPhasing(Player player) {
        PlayerAstralState state = playerStates.get(player.getUUID());
        return state != null && state.isPhasing;
    }
}