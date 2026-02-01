package com.tetra_loopback.effects.curio.windlight;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.getter.resonance.ResonanceGetter;
import com.tetra_loopback.network.TLbNetwork;
import com.tetra_loopback.network.packet.WindLightMotionPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class WindLightEffectHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("WindLightEffect");
    private static final Map<UUID, PlayerWindState> playerStates = new HashMap<>();

    //网络包发送限制
    private static final long MIN_PACKET_INTERVAL = 50;
    private static final Map<UUID, Long> lastPacketTime = new HashMap<>();

    public static class PlayerWindState {
        public boolean hasDoubleJumped = false;
        public boolean isDashing = false;
        public int dashCooldown = 0;
        public int dashTicks = 0;
        public Vec3 dashDirection = Vec3.ZERO;
        public boolean wasOnGround = true;
    }

    //发motion包到客户端
    private static void sendMotionToClient(Player player, double motionX, double motionY, double motionZ,
                                           WindLightMotionPacket.Operation operation) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        //检查发包频率
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastPacketTime.get(playerId);
        if (lastTime != null && (currentTime - lastTime) < MIN_PACKET_INTERVAL) {
            return;
        }

        TLbNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player),
                new WindLightMotionPacket(motionX, motionY, motionZ, operation)
        );

        lastPacketTime.put(playerId, currentTime);
    }

    private static boolean canPerformWindLightAbility(Player player, AbilityType ability) {
        if (player == null) return false;

        //检查是否有WindLight
        int windLightLevel = getWindLightLevel(player);
        if (windLightLevel == 0) {
            return false;
        }

        //检查是否在水，岩浆，飞行
        if (player.isInWater() || player.isInLava() || player.getAbilities().flying) {
            return false;
        }

        //获取共鸣阶段
        int maxResonance = getMaxResonanceFromCurios(player);
        int stageGroup = getResonanceStageGroup(maxResonance);

        //根据能力类型检查阶段要求
        switch (ability) {
            case FAST_FALL:
                return stageGroup >= 3 && !player.onGround(); //3，必须在空中
            case DOUBLE_JUMP:
                return stageGroup >= 4 && !player.onGround(); //4，必须在空中
            case DASH:
                return stageGroup >= 4 && !player.onGround(); //4，必须在空中
            default:
                return false;
        }
    }

    //能力类型枚举
    private enum AbilityType {
        FAST_FALL,
        DOUBLE_JUMP,
        DASH
    }

    //获取最大共鸣值
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

    //获取等级
    public static int getWindLightLevel(Player player) {
        if (player == null) return 0;
        try {
            return CuriosApi.getCuriosInventory(player)
                    .map(inv -> inv.findCurios(itemStack ->
                            itemStack.getItem() instanceof ModularItem &&
                                    ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack,
                                            com.tetra_loopback.effects.gui.ModEffectStats.windLightEffect) > 0
                    ))
                    .map(list -> list.stream()
                            .mapToInt(curio -> (int) ((ModularItem) curio.stack().getItem())
                                    .getEffectLevel(curio.stack(), com.tetra_loopback.effects.gui.ModEffectStats.windLightEffect))
                            .max()
                            .orElse(0))
                    .orElse(0);
        } catch (Exception e) {
            return 0;
        }
    }

    //获取共鸣阶段
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
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        UUID playerId = player.getUUID();
        int windLightLevel = getWindLightLevel(player);

        if (windLightLevel == 0) {
            cleanupPlayerState(player);
            return;
        }

        PlayerWindState state = playerStates.computeIfAbsent(playerId, k -> new PlayerWindState());

        //重置二段跳状态和Dash冷却
        boolean currentOnGround = player.onGround();
        if (state.wasOnGround && !currentOnGround) {
            //玩家起跳时重置状态
            state.hasDoubleJumped = false;
            //state.dashCooldown = 0; //重置Dash冷却
        }
        state.wasOnGround = currentOnGround;

        if (currentOnGround) {
            state.hasDoubleJumped = false;
            //在地面时也可以重置Dash冷却
            //state.dashCooldown = 0;
        }

        updateStateTimers(state);
        int maxResonance = getMaxResonanceFromCurios(player);
        int stageGroup = getResonanceStageGroup(maxResonance);

        applyBaseEffects(player, windLightLevel, stageGroup);
        handleDashMovement(player, state);
    }


    private static void updateStateTimers(PlayerWindState state) {
        if (state.dashCooldown > 0) state.dashCooldown--;
        if (state.dashTicks > 0) {
            state.dashTicks--;
            if (state.dashTicks == 0) {
                state.isDashing = false;
            }
        }
    }

    private static void applyOrRefreshEffect(Player player, MobEffectInstance newEffect) {
        MobEffectInstance existing = player.getEffect(newEffect.getEffect());

        if (existing == null) {
            player.addEffect(newEffect);
        } else if (existing.getAmplifier() < newEffect.getAmplifier()) {
            player.addEffect(newEffect);
        } else if (existing.getAmplifier() == newEffect.getAmplifier()) {
            if (existing.getDuration() < 200) {
                player.addEffect(new MobEffectInstance(
                        newEffect.getEffect(),
                        newEffect.getDuration(),
                        newEffect.getAmplifier(),
                        newEffect.isAmbient(),
                        newEffect.isVisible(),
                        newEffect.showIcon()
                ));
            }
        }
    }

    //应用基础效果
    private static void applyBaseEffects(Player player, int windLightLevel, int stageGroup) {
        int duration = 200;
        boolean ambient = true;
        boolean showIcon = true;
        int effectLevel = (stageGroup == 4) ? 1 : 0;

        applyOrRefreshEffect(player, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, effectLevel, ambient, showIcon));
        if (windLightLevel >= 2) {
            applyOrRefreshEffect(player, new MobEffectInstance(MobEffects.JUMP, duration, effectLevel, ambient, showIcon));
        }
    }

    //处理快速坠落
    public static void handleFastFall(Player player) {
        //服务器端验证
        if (!canPerformWindLightAbility(player, AbilityType.FAST_FALL)) {
            return;
        }

        UUID playerId = player.getUUID();
        PlayerWindState state = playerStates.get(playerId);

        if (state == null) {
            state = new PlayerWindState();
            playerStates.put(playerId, state);
        }

        if (!player.onGround()) {
            sendMotionToClient(player, player.getDeltaMovement().x,
                    com.tetra_loopback.Config.fastFallSpeed, player.getDeltaMovement().z,
                    WindLightMotionPacket.Operation.SET_ABSOLUTE);
        }
    }

    //Dash移动处理
    private static void handleDashMovement(Player player, PlayerWindState state) {
        if (state.isDashing && state.dashTicks > 0) {
            if (state.dashTicks % 3 == 0 || state.dashTicks <= 2) {
                double decay = (double) state.dashTicks / com.tetra_loopback.Config.dashDuration;
                Vec3 currentDash = state.dashDirection.scale(decay);
                sendMotionToClient(player, currentDash.x, currentDash.y, currentDash.z,
                        WindLightMotionPacket.Operation.SET_ABSOLUTE);
            }
        }
    }

    //处理二段跳
    public static void handleDoubleJump(Player player) {
        if (!canPerformWindLightAbility(player, AbilityType.DOUBLE_JUMP)) {
            return;
        }

        UUID playerId = player.getUUID();
        PlayerWindState state = playerStates.get(playerId);

        if (state == null) {
            state = new PlayerWindState();
            playerStates.put(playerId, state);
        }

        //检查是否已经二段跳
        if (state.hasDoubleJumped) {
            return;
        }

        if (!player.onGround()) {
            sendMotionToClient(player, 0, com.tetra_loopback.Config.doubleJumpForce, 0,
                    WindLightMotionPacket.Operation.ADD_RELATIVE);

            state.hasDoubleJumped = true;
        }
    }

    //处理Dash
    public static void handleDash(Player player, Vec3 dashVector) {
        if (player.level().isClientSide()) return;
        if (!canPerformWindLightAbility(player, AbilityType.DASH)) {
            return;
        }

        UUID playerId = player.getUUID();
        PlayerWindState state = playerStates.get(playerId);
        if (state == null) {
            state = new PlayerWindState();
            playerStates.put(playerId, state);
        }

        //检查冷却
        if (state.dashCooldown > 0) {
            return;
        }

        if (player.onGround()) {
            return; //在地面不能dash
        }

        if (dashVector.equals(Vec3.ZERO)) {
            //如果向量为零，使用方向代码计算
            return;
        }

        sendMotionToClient(player, dashVector.x, dashVector.y, dashVector.z,
                WindLightMotionPacket.Operation.SET_ABSOLUTE);

        state.isDashing = true;
        state.dashTicks = com.tetra_loopback.Config.dashDuration;
        state.dashCooldown = com.tetra_loopback.Config.dashCooldown;
        state.dashDirection = dashVector;
    }

    // 保持向后兼容的方法
    public static void handleDash(Player player, int direction) {
        Vec3 dashVector = calculateDashDirection(player, direction);
        handleDash(player, dashVector);
    }

    private static Vec3 calculateDashDirection(Player player, int direction) {
        Vec3 look = player.getLookAngle();
        Vec3 result = Vec3.ZERO;

        Vec3 horizontalLook = new Vec3(look.x, 0, look.z).normalize();
        if (horizontalLook.lengthSqr() == 0) {
            //如果水平向量为零，使用默认方向
            horizontalLook = new Vec3(0, 0, 1);
        }

        //计算左右向量
        Vec3 left = new Vec3(horizontalLook.z, 0, -horizontalLook.x).normalize();
        Vec3 right = new Vec3(-horizontalLook.z, 0, horizontalLook.x).normalize();

        switch (direction) {
            case 0: //前
                result = horizontalLook;
                break;
            case 1: //后
                result = horizontalLook.scale(-1);
                break;
            case 2: //左
                result = left;
                break;
            case 3: //右
                result = right;
                break;
            case 4: //前左
                result = horizontalLook.add(left).normalize();
                break;
            case 5: //前右
                result = horizontalLook.add(right).normalize();
                break;
            case 6: //后左
                result = horizontalLook.scale(-1).add(left).normalize();
                break;
            case 7: //后右
                result = horizontalLook.scale(-1).add(right).normalize();
                break;
            default:
                result = horizontalLook;
                break;
        }

        if (result.lengthSqr() == 0) {
            result = horizontalLook;
        }

        //应用配置的速度和垂直
        float dashSpeed = com.tetra_loopback.Config.dashSpeed;
        float dashVerticalBoost = com.tetra_loopback.Config.dashVerticalBoost;
        result = result.scale(dashSpeed).add(0, dashVerticalBoost, 0);
        return result;
    }

    //清理玩家状态
    private static void cleanupPlayerState(Player player) {
        UUID playerId = player.getUUID();
        playerStates.remove(playerId);
        lastPacketTime.remove(playerId);
    }

    public static void cleanupPlayer(UUID playerId) {
        playerStates.remove(playerId);
        lastPacketTime.remove(playerId);
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        int windLightLevel = getWindLightLevel(player);
        if (windLightLevel > 0) {
            int maxResonance = getMaxResonanceFromCurios(player);
            int stageGroup = getResonanceStageGroup(maxResonance);

            if (stageGroup >= 2) {
                event.setCanceled(true);
            }
        }
    }
}