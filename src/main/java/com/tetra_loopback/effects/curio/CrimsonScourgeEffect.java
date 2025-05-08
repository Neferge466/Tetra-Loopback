package com.tetra_loopback.effects.curio;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class CrimsonScourgeEffect {
    // 配置参数
    private static final int BASE_DURATION = 3 * 20;   // 总持续时间5秒
    private static final int DAMAGE_INTERVAL = 20;     // 伤害间隔1秒
    private static final float BASE_DAMAGE = 0.01f;    // 基础伤害1%
    private static final float STACK_BONUS = 0.001f;   // 每层加成0.1%
    private static final int MAX_STACKS = 10;

    // 粒子颜色配置
    private static final Vector3f PURPLE_COLOR = new Vector3f(0.5f, 0.0f, 0.5f); // 紫色
    private static final Vector3f BLACK_COLOR = new Vector3f(0.1f, 0.1f, 0.1f);  // 黑色
    private static final DustParticleOptions PURPLE_PARTICLE = new DustParticleOptions(PURPLE_COLOR, 0.8f);
    private static final DustParticleOptions BLACK_PARTICLE = new DustParticleOptions(BLACK_COLOR, 1.5f);
    private static final DustParticleOptions RED_PARTICLE = new DustParticleOptions(new Vector3f(0.9f, 0.1f, 0.1f), 0.5f);

    private static class CurseData {
        int stacks;
        long endTime;
        long lastDamageTick;

        CurseData(int stacks, long endTime) {
            this.stacks = Math.min(stacks, MAX_STACKS);
            this.endTime = endTime;
            this.lastDamageTick = 0;
        }
    }

    private static final Map<UUID, CurseData> curseDataMap = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker &&
                hasEffect(attacker)) {

            LivingEntity target = event.getEntity();
            if (target.getHealth() < target.getMaxHealth() * 0.4f) return;

            UUID targetId = target.getUUID();
            long currentTime = target.level().getGameTime();

            CurseData data = curseDataMap.compute(targetId, (k, v) ->
                    v == null ? new CurseData(0, currentTime + BASE_DURATION)
                            : new CurseData(v.stacks, currentTime + BASE_DURATION));

            // 叠加紫色印记
            if (Math.random() < 0.25 && data.stacks < MAX_STACKS) {
                data = new CurseData(data.stacks + 1, data.endTime);
                curseDataMap.put(targetId, data);
                spawnParticles(target, PURPLE_PARTICLE, 15, 0.5); // 紫色粒子
            }

            // 黑色爆发效果
            if (data.stacks >= MAX_STACKS) {
                if (target.getHealth() >= target.getMaxHealth() * 0.4f) {
                    target.hurt(target.damageSources().magic(), target.getMaxHealth() * 0.4f);
                    spawnParticles(target, BLACK_PARTICLE, 50, 1.0); // 黑色爆发粒子
                }
                curseDataMap.remove(targetId);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        UUID uuid = entity.getUUID();
        CurseData data = curseDataMap.get(uuid);

        if (data != null && entity.level() instanceof ServerLevel serverLevel) {
            long currentTime = serverLevel.getGameTime();

            if (currentTime > data.endTime || entity.getHealth() < entity.getMaxHealth() * 0.4f) {
                curseDataMap.remove(uuid);
                return;
            }

            if (currentTime - data.lastDamageTick >= DAMAGE_INTERVAL) {
                float damagePercent = BASE_DAMAGE + (STACK_BONUS * data.stacks);
                entity.hurt(entity.damageSources().magic(), entity.getMaxHealth() * damagePercent);
                data.lastDamageTick = currentTime;

                // 动态红色流血粒子
                spawnParticles(entity, RED_PARTICLE, 10 + data.stacks * 3, 0.5);
                curseDataMap.put(uuid, data);
            }
        }
    }

    // 增强版粒子生成方法
    private static void spawnParticles(LivingEntity target, DustParticleOptions options, int count, double spread) {
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    options,
                    target.getX(),
                    target.getY() + target.getBbHeight()/2,
                    target.getZ(),
                    count,
                    spread, spread, spread, // 散布范围
                    0.3 // 基础速度
            );
        }
    }

    private static boolean hasEffect(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ModEffectStats.crimsonScourgeEffect) > 0
                ))
                .map(list -> !list.isEmpty())
                .orElse(false);
    }
}