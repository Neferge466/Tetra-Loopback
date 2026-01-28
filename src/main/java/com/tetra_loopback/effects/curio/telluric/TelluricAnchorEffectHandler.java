package com.tetra_loopback.effects.curio.telluric;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.getter.resonance.ResonanceGetter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class TelluricAnchorEffectHandler {
    private static final ConcurrentHashMap<UUID, PlayerTelluricState> playerStates = new ConcurrentHashMap<>();

    private static final float[] KNOCKBACK_RESISTANCE_VALUES = {0.3f, 0.6f, 0.9f};
    private static final double KINETIC_RADIUS = 3.0;
    private static final float KINETIC_MIN_FALL = 1.0f;
    private static final float KINETIC_BASE_KNOCKBACK = 0.5f;
    private static final float KINETIC_FALL_MULTIPLIER = 0.3f;
    private static final long KINETIC_COOLDOWN_MS = 500;
    private static final float ROOTED_SLOWDOWN = 0.8f;
    private static final int HUNGER_RESTORE_INTERVAL = 20;
    private static final int HUNGER_RESTORE_AMOUNT = 1;
    //缓冲
    private static final int GROUND_BUFFER_TICKS = 10;

    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("EDAC4A00-0000-4000-8000-000000000000");
    private static final UUID MOVEMENT_SLOW_UUID = UUID.fromString("EDAC4A01-0000-4000-8000-000000000001");

    private static class PlayerTelluricState {
        public int resonanceStage = 0;
        public boolean isGrounded = false;
        public boolean wasGroundedLastTick = false;
        public boolean isRooted = false;
        public float fallDistance = 0.0f;
        public long lastKineticReleaseTime = 0;
        public boolean hasKnockbackResistance = false;
        public boolean hasMovementSlow = false;
        public int hungerTimer = 0;
        public int groundBufferTimer = 0;
        public int currentEffectLevel = 0;
        public boolean shouldHaveKnockbackResistance = false;
    }

    private static int getTelluricAnchorLevel(Player player) {
        if (player == null) return 0;
        try {
            return CuriosApi.getCuriosInventory(player)
                    .map(inv -> inv.findCurios(itemStack ->
                            itemStack.getItem() instanceof ModularItem &&
                                    ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack,
                                            com.tetra_loopback.effects.gui.ModEffectStats.telluricAnchorEffect) > 0
                    ))
                    .map(list -> list.stream()
                            .mapToInt(curio -> (int) ((ModularItem) curio.stack().getItem())
                                    .getEffectLevel(curio.stack(), com.tetra_loopback.effects.gui.ModEffectStats.telluricAnchorEffect))
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

    private static boolean isProperlyGrounded(Player player) {
        return player.onGround() && !player.getAbilities().flying && !player.isInWater();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        UUID playerId = player.getUUID();

        int effectLevel = getTelluricAnchorLevel(player);
        if (effectLevel == 0) {
            cleanupPlayerState(player);
            return;
        }

        PlayerTelluricState state = playerStates.computeIfAbsent(playerId, k -> new PlayerTelluricState());
        state.currentEffectLevel = effectLevel;

        int newStage = getResonanceStage(player);
        if (state.resonanceStage != newStage) {
            state.resonanceStage = newStage;
        }

        boolean currentGrounded = isProperlyGrounded(player);

        if (currentGrounded) {
            state.isGrounded = true;
            state.groundBufferTimer = 0;
        } else {
            if (state.isGrounded && state.groundBufferTimer == 0) {
                state.groundBufferTimer = GROUND_BUFFER_TICKS;
            }

            if (state.groundBufferTimer > 0) {
                state.groundBufferTimer--;
                state.isGrounded = true;
            } else {
                state.isGrounded = false;
            }
        }

        boolean justLanded = !state.wasGroundedLastTick && currentGrounded;

        applyStageEffects(player, state, effectLevel, justLanded);

        //更新上一tick状态
        state.wasGroundedLastTick = currentGrounded;
    }

    private static void applyStageEffects(Player player, PlayerTelluricState state, int effectLevel, boolean justLanded) {
        boolean shouldHaveResistance = state.resonanceStage >= 2 && state.isGrounded;
        state.shouldHaveKnockbackResistance = shouldHaveResistance;

        //阶段2
        if (shouldHaveResistance) {
            applyKnockbackResistance(player, state, effectLevel);
        } else {
            removeKnockbackResistance(player, state);
        }

        //阶段3
        if (state.resonanceStage >= 3 && justLanded && state.fallDistance >= KINETIC_MIN_FALL) {
            checkKineticDissipation(player, state);
        }

        if (justLanded) {
            state.fallDistance = 0.0f;
        }

        //阶段4
        if (state.resonanceStage >= 4) {
            handleRootedState(player, state);
        } else {
            if (state.isRooted) {
                state.isRooted = false;
                removeRootedEffects(player, state);
            }
        }
    }

    private static void applyKnockbackResistance(Player player, PlayerTelluricState state, int effectLevel) {
        if (effectLevel < 1) {
            if (state.hasKnockbackResistance) {
                removeKnockbackResistance(player, state);
            }
            return;
        }

        int levelIndex = Math.min(effectLevel, 3) - 1;
        if (levelIndex < 0) {//
            if (state.hasKnockbackResistance) {
                removeKnockbackResistance(player, state);
            }
            return;
        }

        AttributeInstance attribute = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (attribute == null) {
            state.hasKnockbackResistance = false;
            return;
        }

        AttributeModifier existingModifier = attribute.getModifier(KNOCKBACK_RESISTANCE_UUID);
        float targetAmount = KNOCKBACK_RESISTANCE_VALUES[levelIndex];

        if (existingModifier != null) {
            float currentAmount = (float) existingModifier.getAmount();

            if (Math.abs(currentAmount - targetAmount) > 0.0001f) {
                attribute.removeModifier(KNOCKBACK_RESISTANCE_UUID);
                existingModifier = null;
            } else {
                state.hasKnockbackResistance = true;
                return;
            }
        }

        AttributeModifier modifier = new AttributeModifier(
                KNOCKBACK_RESISTANCE_UUID,
                "telluric_anchor_knockback_resistance",
                targetAmount,
                AttributeModifier.Operation.ADDITION
        );

        attribute.addTransientModifier(modifier);
        state.hasKnockbackResistance = true;
    }

    private static void removeKnockbackResistance(Player player, PlayerTelluricState state) {
        if (!state.hasKnockbackResistance) return;

        AttributeInstance attribute = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (attribute != null) {
            attribute.removeModifier(KNOCKBACK_RESISTANCE_UUID);
        }
        state.hasKnockbackResistance = false;
    }

    private static void checkKineticDissipation(Player player, PlayerTelluricState state) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - state.lastKineticReleaseTime >= KINETIC_COOLDOWN_MS) {
            releaseKineticEnergy(player, state);
            state.lastKineticReleaseTime = currentTime;
        }
    }

    private static void releaseKineticEnergy(Player player, PlayerTelluricState state) {
        //计算击退力度
        float knockbackStrength = KINETIC_BASE_KNOCKBACK +
                (state.fallDistance * KINETIC_FALL_MULTIPLIER);
        knockbackStrength = Math.min(knockbackStrength, 2.0f);

        AABB area = new AABB(
                player.getX() - KINETIC_RADIUS,
                player.getY() - 1.0,
                player.getZ() - KINETIC_RADIUS,
                player.getX() + KINETIC_RADIUS,
                player.getY() + 2.0,
                player.getZ() + KINETIC_RADIUS
        );

        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity != player && entity.isAlive()
        );

        for (LivingEntity entity : entities) {
            Vec3 direction = entity.position()
                    .subtract(player.position())
                    .normalize()
                    .scale(knockbackStrength);

            entity.knockback(knockbackStrength, direction.x, direction.z);
        }
    }

    private static void handleRootedState(Player player, PlayerTelluricState state) {
        boolean wantsToRoot = player.isShiftKeyDown() && state.isGrounded;

        if (wantsToRoot && !state.isRooted) {
            state.isRooted = true;
            applyRootedEffects(player, state);
        } else if (!wantsToRoot && state.isRooted) {
            state.isRooted = false;
            removeRootedEffects(player, state);
        }

        if (state.isRooted) {
            state.hungerTimer++;
            if (state.hungerTimer >= HUNGER_RESTORE_INTERVAL) {
                state.hungerTimer = 0;
                player.getFoodData().eat(HUNGER_RESTORE_AMOUNT, 0.1f);
            }
        } else {
            state.hungerTimer = 0;
        }
    }

    private static void applyRootedEffects(Player player, PlayerTelluricState state) {
        if (state.hasMovementSlow) return;

        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;

        AttributeModifier slowModifier = new AttributeModifier(
                MOVEMENT_SLOW_UUID,
                "telluric_anchor_rooted_slow",
                -ROOTED_SLOWDOWN,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        attribute.addTransientModifier(slowModifier);
        state.hasMovementSlow = true;
    }

    private static void removeRootedEffects(Player player, PlayerTelluricState state) {
        if (!state.hasMovementSlow) return;

        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(MOVEMENT_SLOW_UUID);
        }
        state.hasMovementSlow = false;
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        PlayerTelluricState state = playerStates.get(player.getUUID());

        if (state != null) {
            state.fallDistance = event.getDistance();
            if (state.resonanceStage >= 4) {
                event.setDamageMultiplier(0.5f);
            }
        }
    }

    private static void cleanupPlayerState(Player player) {
        UUID playerId = player.getUUID();
        PlayerTelluricState state = playerStates.get(playerId);

        if (state != null) {
            removeKnockbackResistance(player, state);
            removeRootedEffects(player, state);
        }

        playerStates.remove(playerId);
    }
}