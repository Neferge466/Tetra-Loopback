package com.tetra_loopback.effects.curio.supercooling;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.getter.resonance.ResonanceGetter;
import com.tetra_loopback.network.TLbNetwork;
import com.tetra_loopback.network.packet.SupercoolingStatePacket;
import com.tetra_loopback.network.packet.SupercoolingAttackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class SupercoolingEffectHandler {
    private static final Map<UUID, PlayerSupercoolState> playerStates = new HashMap<>();
    private static final Map<UUID, Long> lastStatePacket = new HashMap<>();
    private static final long MIN_STATE_INTERVAL = 50;

    // 配置参数
    private static final float SUPERCOOL_CHARGE_RATE = 0.05f;
    private static final float SUPERCOOL_COOLDOWN_RESET = 10;
    private static final float ARMOR_TOUGHNESS_DAMAGE_RATIO = 0.3f;
    private static final float ARMOR_TOUGHNESS_KNOCKBACK_RATIO = 0.5f;

    public static class PlayerSupercoolState {
        public boolean isActive = false;
        public float supercoolProgress = 0.0f;
        public int resonanceStage = 0;
        public boolean wasCoolingMax = false;
        public int cooldownOverride = 0;
        public boolean hasSupercoolAttack = false;
        public float lastAttackStrength = 0.0f;
        public float attackSupercoolProgress = 0.0f;
    }

    public static int getSupercoolingLevel(Player player) {
        if (player == null) return 0;
        try {
            return CuriosApi.getCuriosInventory(player)
                    .map(inv -> inv.findCurios(itemStack ->
                            itemStack.getItem() instanceof ModularItem &&
                                    ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack,
                                            com.tetra_loopback.effects.gui.ModEffectStats.supercoolingEffect) > 0
                    ))
                    .map(list -> list.stream()
                            .mapToInt(curio -> (int) ((ModularItem) curio.stack().getItem())
                                    .getEffectLevel(curio.stack(), com.tetra_loopback.effects.gui.ModEffectStats.supercoolingEffect))
                            .max()
                            .orElse(0))
                    .orElse(0);
        } catch (Exception e) {
            return 0;
        }
    }

    private static int getResonanceStage(Player player) {
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

        if (maxResonance[0] >= 16) return 4;
        if (maxResonance[0] >= 11) return 3;
        if (maxResonance[0] >= 6) return 2;
        if (maxResonance[0] >= 1) return 1;
        return 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        UUID playerId = player.getUUID();
        int supercoolLevel = getSupercoolingLevel(player);

        if (supercoolLevel == 0) {
            PlayerSupercoolState state = playerStates.get(playerId);
            if (state != null && (state.isActive || state.supercoolProgress > 0)) {
                sendCleanupPacket(player);
            }
            cleanupPlayerState(player);
            return;
        }

        PlayerSupercoolState state = playerStates.computeIfAbsent(playerId, k -> new PlayerSupercoolState());
        state.resonanceStage = getResonanceStage(player);

        if (state.cooldownOverride > 0) {
            state.cooldownOverride--;
            if (state.cooldownOverride == 0) {
                state.isActive = false;
                state.supercoolProgress = 0.0f;
                state.wasCoolingMax = false;
                state.attackSupercoolProgress = 0.0f;
                sendStateToClient(player, state);
            }
            return;
        }

        float attackStrength = player.getAttackStrengthScale(0.0F);
        if (attackStrength >= 0.99f && !player.swinging) {
            if (!state.wasCoolingMax) {
                state.isActive = true;
                state.supercoolProgress = 0.0f;
                state.wasCoolingMax = true;
                sendStateToClient(player, state);
            } else {
                float oldProgress = state.supercoolProgress;
                state.supercoolProgress = Math.min(1.0f,
                        state.supercoolProgress + SUPERCOOL_CHARGE_RATE);
                if (Math.abs(state.supercoolProgress - oldProgress) > 0.01f) {
                    sendStateToClient(player, state);
                }
            }
        } else {
            state.wasCoolingMax = false;
            if (state.supercoolProgress < 1.0f && state.isActive) {
                state.isActive = false;
                state.supercoolProgress = 0.0f;
                state.attackSupercoolProgress = 0.0f;
                sendStateToClient(player, state);
            }
        }
    }

    private static void sendStateToClient(Player player, PlayerSupercoolState state) {
        if (player.level().isClientSide()) return;
        UUID playerId = player.getUUID();

        long currentTime = System.currentTimeMillis();
        Long lastTime = lastStatePacket.get(playerId);
        if (lastTime != null && (currentTime - lastTime) < MIN_STATE_INTERVAL) {
            return;
        }

        TLbNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new SupercoolingStatePacket(
                        state.isActive,
                        state.supercoolProgress,
                        state.resonanceStage
                )
        );
        lastStatePacket.put(playerId, currentTime);
    }

    private static void sendCleanupPacket(Player player) {
        if (player.level().isClientSide()) return;

        TLbNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new SupercoolingStatePacket(false, 0.0f, 0)
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.isCanceled()) return;
        if (!(event.getSource().getEntity() instanceof Player)) return;

        Player player = (Player) event.getSource().getEntity();
        UUID playerId = player.getUUID();
        if (getSupercoolingLevel(player) == 0) return;

        PlayerSupercoolState state = playerStates.get(playerId);
        if (state == null) return;

        state.lastAttackStrength = player.getAttackStrengthScale(0.0F);
        float attackStrength = state.lastAttackStrength;
        boolean isFullStrength = attackStrength >= 0.99f;

        if (state.isActive && state.supercoolProgress >= 0.95f && isFullStrength) {
            state.hasSupercoolAttack = true;
            state.attackSupercoolProgress = state.supercoolProgress;

            if (state.resonanceStage >= 2) {
                event.getEntity().invulnerableTime = 0;
            }

            state.cooldownOverride = (int) SUPERCOOL_COOLDOWN_RESET;
            state.isActive = false;
            state.supercoolProgress = 0.0f;
            state.wasCoolingMax = false;

            sendStateToClient(player, state);
        } else {
            state.hasSupercoolAttack = false;
            state.attackSupercoolProgress = 0.0f;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof Player)) return;

        Player player = (Player) event.getSource().getEntity();
        LivingEntity target = event.getEntity();
        UUID playerId = player.getUUID();

        if (getSupercoolingLevel(player) == 0) return;

        PlayerSupercoolState state = playerStates.get(playerId);
        if (state == null) return;

        if (state.hasSupercoolAttack || state.attackSupercoolProgress >= 0.95f) {
            applySupercoolEffects(player, target, state, event);
            sendAttackEffectToClient(player, target, state.resonanceStage);
            state.hasSupercoolAttack = false;
        }
    }

    private static void applySupercoolEffects(Player player, LivingEntity target,
                                              PlayerSupercoolState state, LivingHurtEvent event) {

        int resonanceStage = state.resonanceStage;
        float attackProgress = state.attackSupercoolProgress > 0 ? state.attackSupercoolProgress : state.supercoolProgress;

        if (resonanceStage >= 1) {
            float damageMultiplier = 1.0f + (attackProgress * 0.5f);
            event.setAmount(event.getAmount() * damageMultiplier);
        }

        if (resonanceStage >= 2) {
            target.invulnerableTime = 0;
            if (target.isUsingItem()) {
                target.stopUsingItem();
            }
        }

        if (resonanceStage >= 3) {
            float armorToughness = (float) player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            float convertedDamage = armorToughness * ARMOR_TOUGHNESS_DAMAGE_RATIO;
            event.setAmount(event.getAmount() + convertedDamage);
        }

        if (resonanceStage >= 4) {
            float armorToughness = (float) player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            float knockbackStrength = 0.5f + (armorToughness * ARMOR_TOUGHNESS_KNOCKBACK_RATIO);

            Vec3 playerPos = player.position();
            Vec3 targetPos = target.position();
            Vec3 direction = targetPos.subtract(playerPos).normalize();

            target.knockback(knockbackStrength, direction.x, direction.z);
            target.setDeltaMovement(target.getDeltaMovement().add(0, 0.2, 0));
        }
    }

    private static void sendAttackEffectToClient(Player player, LivingEntity target, int resonanceStage) {
        if (player.level().isClientSide()) return;

        TLbNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new SupercoolingAttackPacket(
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        resonanceStage
                )
        );
    }

    private static void cleanupPlayerState(Player player) {
        UUID playerId = player.getUUID();
        playerStates.remove(playerId);
        lastStatePacket.remove(playerId);
    }

    public static void cleanupPlayer(UUID playerId) {
        playerStates.remove(playerId);
        lastStatePacket.remove(playerId);
    }
}