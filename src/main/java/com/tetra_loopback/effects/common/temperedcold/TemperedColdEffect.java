package com.tetra_loopback.effects.common.temperedcold;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class TemperedColdEffect {
    private static final Map<UUID, FrostData> frostDataMap = new ConcurrentHashMap<>();

    private static final UUID ARMOR_PENETRATION_UUID = UUID.fromString("b4c5d6e7-f8a9-4b1c-8d3e-5f6a7b8c9d0e");
    private static final String ARMOR_PENETRATION_NAME = "tempered_cold_armor_pen";

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        //检查玩家攻击
        if (event.getSource().getEntity() instanceof Player attacker) {
            if (hasEffect(attacker)) {
                LivingEntity target = event.getEntity();
                //获取目标的frost data
                FrostData data = frostDataMap.computeIfAbsent(target.getUUID(),
                        uuid -> new FrostData());
                int requiredLayers = getRequiredLayersForEnvironment(target.level(), target.blockPosition());
                int currentLayers = data.getLayers();

                //检查是否脆化爆发
                boolean shouldTriggerBrittle = false;
                if (currentLayers >= requiredLayers) {
                    shouldTriggerBrittle = true;
                    //标记下一次伤害触发脆化
                    data.setBrittleReady(true);
                    data.resetLayers();

                    if (!target.level().isClientSide()) {
                        playBrittleEffects((ServerLevel) target.level(), target);
                        //层满
                        playLayerFullSound((ServerLevel) target.level(), target);
                    }
                }

                //若无触发脆化爆发，则增加一层霜冻标记
                if (!shouldTriggerBrittle) {
                    int newLayers = Math.min(currentLayers + 1, 9);
                    data.setLayers(newLayers, target.tickCount);
                    applySlowEffects(target, newLayers);
                }

                frostDataMap.put(target.getUUID(), data);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        //检查玩家攻击并且触发脆化
        if (event.getSource().getEntity() instanceof Player attacker &&
                hasEffect(attacker)) {

            LivingEntity target = event.getEntity();
            FrostData data = frostDataMap.get(target.getUUID());

            if (data != null && data.isBrittleReady()) {
                //应用30%护甲穿透
                applyArmorPenetration(target, 0.3f); //30%护甲穿透

                //计算脆化爆发伤害（1.5）
                float originalDamage = event.getAmount();
                float brittleDamage = originalDamage * 1.5f;

                event.setAmount(brittleDamage);

                //重置触发标志
                data.setBrittleReady(false);

                if (!target.level().isClientSide()) {
                    sendBrittleDamageParticles((ServerLevel) target.level(), target);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        //移除护甲穿透
        LivingEntity target = event.getEntity();
        var armorAttr = target.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_PENETRATION_UUID);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        //清理死亡实体的数据
        frostDataMap.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        //定期清理过期数据
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END &&
                event.player.tickCount % 200 == 0) { //10秒
            cleanupFrostData();
        }
    }

    private static boolean hasEffect(Player player) {
        int totalLevel = 0;

        if (player.getMainHandItem().getItem() instanceof ModularItem) {
            totalLevel += ((ModularItem) player.getMainHandItem().getItem())
                    .getEffectLevel(player.getMainHandItem(), ModEffectStats.temperedColdEffect);
        }

        var curiosInventory = CuriosApi.getCuriosInventory(player);
        if (curiosInventory.isPresent()) {
            var inventory = curiosInventory.resolve().orElse(null);
            if (inventory != null) {
                var curios = inventory.findCurios(itemStack ->
                        itemStack.getItem() instanceof ModularItem &&
                                ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack, ModEffectStats.temperedColdEffect) > 0
                );

                for (SlotResult slotResult : curios) {
                    totalLevel += ((ModularItem) slotResult.stack().getItem())
                            .getEffectLevel(slotResult.stack(), ModEffectStats.temperedColdEffect);
                }
            }
        }

        return totalLevel > 0;
    }

    private static int getRequiredLayersForEnvironment(Level level, BlockPos pos) {
        if (level.dimension() == Level.NETHER) {
            return 9;
        }
        Biome biome = level.getBiome(pos).value();
        float temperature = biome.getBaseTemperature();
        //寒冷生物群系的标准温度（低于0.15）
        if (temperature < 0.15f) {
            return 2;
        }
        return 6;//默认
    }

    private static void applySlowEffects(LivingEntity target, int layers) {
        if (layers > 0) {
            //计算减速百分比（每层10%，最高30%）
            float slowPercent = Math.min(layers * 0.1f, 0.3f);
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    100, //5秒
                    Mth.floor(slowPercent * 2.5f), //每10%减速对应约等级1
                    false, false, true));

            target.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    100, //5秒
                    Mth.floor(slowPercent * 2.5f), //每10%减速对应约等级1
                    false, false, true));

            //视觉反馈
            if (!target.level().isClientSide()) {
                sendFrostParticles((ServerLevel) target.level(), target, layers);
            }
        }
    }

    private static void applyArmorPenetration(LivingEntity target, float penetrationPercent) {
        //应用护甲穿透
        var armorAttr = target.getAttribute(Attributes.ARMOR);
        if (armorAttr != null && armorAttr.getModifier(ARMOR_PENETRATION_UUID) == null) {
            armorAttr.addTransientModifier(new AttributeModifier(
                    ARMOR_PENETRATION_UUID,
                    ARMOR_PENETRATION_NAME,
                    -penetrationPercent,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private static void playBrittleEffects(ServerLevel level, LivingEntity target) {
        for (int i = 0; i < 30; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = level.random.nextDouble() * target.getBbHeight();
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;

            if (i % 3 == 0) {
                //雪球粒子
                level.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                        target.getX() + offsetX,
                        target.getY() + offsetY,
                        target.getZ() + offsetZ,
                        2, 0.1, 0.1, 0.1, 0.05);
            } else {
                //暴击粒子
                level.sendParticles(ParticleTypes.CRIT,
                        target.getX() + offsetX,
                        target.getY() + offsetY,
                        target.getZ() + offsetZ,
                        3, 0.1, 0.1, 0.1, 0.1);
            }

            if (i % 5 == 0) {
                level.sendParticles(ParticleTypes.SNOWFLAKE,
                        target.getX() + offsetX * 0.5,
                        target.getY() + offsetY,
                        target.getZ() + offsetZ * 0.5,
                        1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        level.playSound(null, target.blockPosition(),
                SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS, 0.8f, 0.5f + level.random.nextFloat() * 0.2f); // 音调: 0.5 - 0.7

        level.playSound(null, target.blockPosition(),
                SoundEvents.ANVIL_LAND,
                SoundSource.PLAYERS, 0.6f, 1.5f + level.random.nextFloat() * 0.5f); // 音调: 1.5 - 2.0

        level.playSound(null, target.blockPosition(),
                SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS, 0.7f, 0.2f); //音调: 0.2
    }

    private static void playLayerFullSound(ServerLevel level, LivingEntity target) {
        level.playSound(null, target.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_BREAK,
                SoundSource.PLAYERS, 0.5f, 1.2f); //音调: 1.2
    }

    private static void sendFrostParticles(ServerLevel level, LivingEntity target, int layers) {
        int particleCount = 5 + layers * 3;

        for (int i = 0; i < particleCount; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.0;
            double offsetY = level.random.nextDouble() * target.getBbHeight();
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.0;

            level.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    1, 0.05, 0.05, 0.05, 0.02);

            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.SNOWFLAKE,
                        target.getX() + offsetX * 0.8,
                        target.getY() + offsetY,
                        target.getZ() + offsetZ * 0.8,
                        1, 0.03, 0.03, 0.03, 0.01);
            }
        }
    }

    private static void sendBrittleDamageParticles(ServerLevel level, LivingEntity target) {
        for (int i = 0; i < 15; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * target.getBbHeight();
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;

            level.sendParticles(ParticleTypes.CRIT,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    2, 0.2, 0.2, 0.2, 0.2);

            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    target.getX() + offsetX * 0.5,
                    target.getY() + offsetY + 0.5,
                    target.getZ() + offsetZ * 0.5,
                    1, 0.1, 0.1, 0.1, 0.05);
        }
    }

    private static void cleanupFrostData() {
        //清理过期的数据
        long currentTime = System.currentTimeMillis();
        frostDataMap.entrySet().removeIf(entry -> {
            FrostData data = entry.getValue();
            return data.isExpired(currentTime);
        });
    }

    //存储
    private static class FrostData {
        private int layers = 0;
        private long lastUpdateTime = 0;
        private boolean brittleReady = false;
        private static final long EXPIRY_TIME = 30 * 1000; //30秒后过期

        public int getLayers() {
            return layers;
        }

        public void setLayers(int layers, int currentTick) {
            this.layers = layers;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public void resetLayers() {
            this.layers = 0;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public boolean isBrittleReady() {
            return brittleReady;
        }

        public void setBrittleReady(boolean ready) {
            this.brittleReady = ready;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public boolean isExpired(long currentTime) {
            return System.currentTimeMillis() - lastUpdateTime > EXPIRY_TIME;
        }
    }
}