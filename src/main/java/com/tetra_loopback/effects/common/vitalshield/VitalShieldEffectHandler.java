package com.tetra_loopback.effects.common.vitalshield;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class VitalShieldEffectHandler {
    private static final Map<UUID, ShieldState> shieldStates = new ConcurrentHashMap<>();

    // 护盾配置表: [持续时间(ticks), 吸收效果等级]
    private static final int[][] SHIELD_CONFIG = {
            {160, 3},  //1: 8秒, 吸收IV 16点
            {180, 4},  //2: 9秒, 吸收V 20点
            {200, 5},  //3: 10秒, 吸收VI 24点
            {220, 6},  //4: 11秒, 吸收VII 28点
            {240, 7}   //5: 12秒, 吸收VIII 32点
    };

    private static final int[] COOLDOWN_CONFIG = {
            3600,  //1: 180秒
            3000,  //2: 150秒
            2400,  //3: 120秒
            1800,  //4: 90秒
            1200   //5: 60秒
    };

    private static final long STATE_EXPIRY = 30000L;//30秒状态过期
    private static final int CLEANUP_INTERVAL = 200;

    private static class ShieldState {
        public int remainingTicks = 0;
        public int cooldownTicks = 0;
        public int currentLevel = 0;
        public long lastUpdateTime = System.currentTimeMillis();
        public boolean isActive = false;
        public boolean isOnCooldown = false;

        public boolean isExpired() {
            return System.currentTimeMillis() - lastUpdateTime > STATE_EXPIRY;
        }

        public void activateShield(int newLevel) {
            this.currentLevel = newLevel;
            this.remainingTicks = SHIELD_CONFIG[newLevel - 1][0];
            this.cooldownTicks = COOLDOWN_CONFIG[newLevel - 1];
            this.lastUpdateTime = System.currentTimeMillis();
            this.isActive = true;
            this.isOnCooldown = true;
        }

        public void refreshShield(int newLevel) {
            this.currentLevel = newLevel;
            this.remainingTicks = SHIELD_CONFIG[newLevel - 1][0];
            this.lastUpdateTime = System.currentTimeMillis();
            this.isActive = true;
        }

        public void tick() {
            if (remainingTicks > 0) {
                remainingTicks--;
                if (remainingTicks <= 0) {
                    isActive = false;
                }
            }
            //更新冷却时间
            if (cooldownTicks > 0) {
                cooldownTicks--;
                if (cooldownTicks <= 0) {
                    isOnCooldown = false;
                }
            }
        }

        public int getRemainingCooldownSeconds() {
            return (cooldownTicks + 19) / 20;
        }
    }

    public static int getVitalShieldLevel(Player player) {
        if (player == null) return 0;

        int maxLevel = 0;

        if (player.getMainHandItem().getItem() instanceof ModularItem) {
            int mainHandLevel = (int) ((ModularItem) player.getMainHandItem().getItem())
                    .getEffectLevel(player.getMainHandItem(), ModEffectStats.vitalShieldEffect);
            maxLevel = Math.max(maxLevel, mainHandLevel);
        }

        if (player.getOffhandItem().getItem() instanceof ModularItem) {
            int offhandLevel = (int) ((ModularItem) player.getOffhandItem().getItem())
                    .getEffectLevel(player.getOffhandItem(), ModEffectStats.vitalShieldEffect);
            maxLevel = Math.max(maxLevel, offhandLevel);
        }

        var curiosInventory = CuriosApi.getCuriosInventory(player);
        if (curiosInventory.isPresent()) {
            var inventory = curiosInventory.resolve().orElse(null);
            if (inventory != null) {
                var curios = inventory.findCurios(itemStack ->
                        itemStack.getItem() instanceof ModularItem &&
                                ((ModularItem) itemStack.getItem())
                                        .getEffectLevel(itemStack, ModEffectStats.vitalShieldEffect) > 0
                );

                for (var slotResult : curios) {
                    int curioLevel = (int) ((ModularItem) slotResult.stack().getItem())
                            .getEffectLevel(slotResult.stack(), ModEffectStats.vitalShieldEffect);
                    maxLevel = Math.max(maxLevel, curioLevel);
                }
            }
        }

        return Math.min(maxLevel, 5);
    }

    private static void applyShieldEffect(Player player, int level) {
        if (level < 1 || level > 5) return;

        int[] config = SHIELD_CONFIG[level - 1];
        int duration = config[0];
        int amplifier = config[1];

        MobEffectInstance currentAbsorption = player.getEffect(MobEffects.ABSORPTION);

        if (currentAbsorption == null) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.ABSORPTION,
                    duration,
                    amplifier,
                    false,
                    true,
                    true
            ));
        } else {
            int currentAmplifier = currentAbsorption.getAmplifier();
            int currentDuration = currentAbsorption.getDuration();

            if (amplifier > currentAmplifier) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.ABSORPTION,
                        duration,
                        amplifier,
                        false, true, true
                ));
            } else if (amplifier == currentAmplifier && duration > currentDuration) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.ABSORPTION,
                        duration,
                        amplifier,
                        false, true, true
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Player player = null;

        if (event.getSource().getEntity() instanceof Player attacker) {
            player = attacker;
        }

        if (event.getEntity() instanceof Player victim) {
            player = victim;
        }

        if (player == null) return;

        int shieldLevel = getVitalShieldLevel(player);
        if (shieldLevel < 1) return;

        ShieldState state = shieldStates.computeIfAbsent(
                player.getUUID(),
                uuid -> new ShieldState()
        );

        if (state.isOnCooldown) {
            //冷却中不触发
            return;
        }

        boolean shouldActivate = false;
        boolean shouldRefresh = false;

        if (!state.isActive) {
            shouldActivate = true;
        } else if (shieldLevel > state.currentLevel) {
            shouldRefresh = true;
        } else if (shieldLevel == state.currentLevel) {
            shouldRefresh = true;
        }

        if (shouldActivate) {
            state.activateShield(shieldLevel);
            applyShieldEffect(player, shieldLevel);
        } else if (shouldRefresh) {
            state.refreshShield(shieldLevel);
            applyShieldEffect(player, shieldLevel);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        UUID playerId = player.getUUID();

        int shieldLevel = getVitalShieldLevel(player);
        if (shieldLevel < 1) {
            shieldStates.remove(playerId);
            return;
        }

        //更新状态
        ShieldState state = shieldStates.get(playerId);
        if (state != null) {
            state.tick();
        }

        //定期清理过期数据
        if (player.tickCount % CLEANUP_INTERVAL == 0) {
            cleanupExpiredStates();
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            shieldStates.remove(player.getUUID());
        }
    }

    private static void cleanupExpiredStates() {
        shieldStates.entrySet().removeIf(entry -> {
            ShieldState state = entry.getValue();
            return state.isExpired();
        });
    }

    public static void cleanupPlayer(UUID playerId) {
        shieldStates.remove(playerId);
    }

    public static int getRemainingCooldown(Player player) {
        ShieldState state = shieldStates.get(player.getUUID());
        if (state == null) return 0;
        return state.getRemainingCooldownSeconds();
    }
}