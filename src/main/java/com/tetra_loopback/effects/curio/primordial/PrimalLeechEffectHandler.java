package com.tetra_loopback.effects.curio.primordial;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.getter.resonance.ResonanceGetter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class PrimalLeechEffectHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("PrimalLeech");

    private static final float BASE_LEECH_RATIO = 0.15f;        //基础偷取比例15%
    private static final float BASE_REFLECT_RATIO = 0.20f;      //基础反伤比例20%
    private static final float LOW_HP_THRESHOLD = 0.20f;        //低血量阈值20%
    private static final float LOW_HP_MULTIPLIER = 2.0f;        //低血量时倍率
    private static final int KILL_HEAL_AMOUNT = 4;              //击杀治疗量4点

    //清除列表
    private static final List<MobEffect> NEGATIVE_EFFECTS = Arrays.asList(
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.WEAKNESS,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.BLINDNESS,
            MobEffects.HUNGER,
            MobEffects.CONFUSION
    );

    //获取荒芜掠夺
    public static int getPrimalLeechLevel(Player player) {
        if (player == null) return 0;
        try {
            return CuriosApi.getCuriosInventory(player)
                    .map(inv -> inv.findCurios(itemStack ->
                            itemStack.getItem() instanceof ModularItem &&
                                    ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack,
                                            com.tetra_loopback.effects.gui.ModEffectStats.primalLeechEffect) > 0
                    ))
                    .map(list -> list.stream()
                            .mapToInt(curio -> (int) ((ModularItem) curio.stack().getItem())
                                    .getEffectLevel(curio.stack(), com.tetra_loopback.effects.gui.ModEffectStats.primalLeechEffect))
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

    private static float getLeechRatio(int resonanceStage, float healthRatio) {
        float baseRatio = BASE_LEECH_RATIO;

        switch (resonanceStage) {
            case 2: baseRatio = 0.20f; break;
            case 3: baseRatio = 0.25f; break;
            case 4: baseRatio = 0.30f; break;
        }

        //低血量强化
        if (healthRatio <= LOW_HP_THRESHOLD) {
            baseRatio *= LOW_HP_MULTIPLIER;
        }

        return Math.min(baseRatio, 0.50f);//上限50%
    }

    private static float getReflectRatio(int resonanceStage, float healthRatio) {
        float baseRatio = BASE_REFLECT_RATIO;

        //共鸣阶段加成
        switch (resonanceStage) {
            case 2: baseRatio = 0.25f; break;
            case 3: baseRatio = 0.30f; break;
            case 4: baseRatio = 0.35f; break;
        }

        //低血量强化
        if (healthRatio <= LOW_HP_THRESHOLD) {
            baseRatio *= LOW_HP_MULTIPLIER;
        }

        return Math.min(baseRatio, 0.50f); //上限50%
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        //玩家攻击敌人
        if (event.getSource().getEntity() instanceof Player) {
            handlePlayerAttack(event);
        }
        //玩家被攻击
        else if (event.getEntity() instanceof Player) {
            handlePlayerDamaged(event);
        }
    }

    private static void handlePlayerAttack(LivingDamageEvent event) {
        Player player = (Player) event.getSource().getEntity();
        LivingEntity target = event.getEntity();

        int leechLevel = getPrimalLeechLevel(player);
        if (leechLevel == 0) return;

        int resonanceStage = getResonanceStage(player);
        if (resonanceStage == 0) return;

        float damage = event.getAmount();
        float healthRatio = player.getHealth() / player.getMaxHealth();
        float leechRatio = getLeechRatio(resonanceStage, healthRatio);
        float healAmount = damage * leechRatio;

        if (healAmount > 0) {
            player.heal(healAmount);
        }
    }

    private static void handlePlayerDamaged(LivingDamageEvent event) {
        Player player = (Player) event.getEntity();

        //只处理近战伤害
        if (!event.getSource().isIndirect()) {
            int leechLevel = getPrimalLeechLevel(player);
            if (leechLevel == 0) return;

            int resonanceStage = getResonanceStage(player);
            if (resonanceStage == 0) return;

            //检查攻击者
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                float healthRatio = player.getHealth() / player.getMaxHealth();
                float reflectRatio = getReflectRatio(resonanceStage, healthRatio);
                float reflectDamage = event.getAmount() * reflectRatio;

                //对攻击者造成反噬伤害
                if (reflectDamage > 0) {
                    attacker.hurt(player.damageSources().indirectMagic(attacker, player), reflectDamage);

                    //反噬伤害的100%转化为生命值
                    float healFromReflect = reflectDamage * 1.0f;
                    player.heal(healFromReflect);

                    LOGGER.debug("Player {} reflected {} damage and healed {}",
                            player.getName().getString(), reflectDamage, healFromReflect);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        //检查是否是玩家造成的击杀
        if (event.getSource().getEntity() instanceof Player player) {
            int leechLevel = getPrimalLeechLevel(player);
            if (leechLevel == 0) return;
            int resonanceStage = getResonanceStage(player);
            if (resonanceStage == 0) return;
            handleKillBonus(player, resonanceStage);
        }
    }

    //处理增益
    private static void handleKillBonus(Player player, int resonanceStage) {
        //基础治疗2颗红心
        player.heal(KILL_HEAL_AMOUNT);

        //根据共鸣阶段提供额外效果
        switch (resonanceStage) {
            case 1:
                //阶段1:清除一个随机负面效果
                clearRandomNegativeEffect(player);
                break;

            case 2:
                //阶段2:清除一个负面效果 + 伤害吸收效果(8秒)
                clearRandomNegativeEffect(player);
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 0)); //8秒伤害吸收I
                break;

            case 3:
                //阶段3:清除所有负面效果
                clearAllNegativeEffects(player);
                break;

            case 4:
                //阶段4
                clearAllNegativeEffects(player);
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 1)); //12秒伤害吸收II
                healNearbyAllies(player);
                break;
        }

        LOGGER.debug("Player {} executed kill bonus at resonance stage {}",
                player.getName().getString(), resonanceStage);
    }

    //清除一个随机负面效果
    private static void clearRandomNegativeEffect(Player player) {
        List<MobEffectInstance> negativeEffects = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (NEGATIVE_EFFECTS.contains(effect.getEffect())) {
                negativeEffects.add(effect);
            }
        }

        //随机清除
        if (!negativeEffects.isEmpty()) {
            Random random = new Random();
            MobEffectInstance toRemove = negativeEffects.get(random.nextInt(negativeEffects.size()));
            player.removeEffect(toRemove.getEffect());
        }
    }

    //清除所有负面效果
    private static void clearAllNegativeEffects(Player player) {
        int clearedCount = 0;
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (NEGATIVE_EFFECTS.contains(effect.getEffect())) {
                player.removeEffect(effect.getEffect());
                clearedCount++;
            }
        }
    }

    //治疗附近队友
    private static void healNearbyAllies(Player player) {
        if (player.level().isClientSide()) return;

        double range = 3.0; //3x3范围
        int healAmount = 2;  //1

        player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(range))
                .stream()
                .filter(ally -> ally != player && ally.isAlive())
                .forEach(ally -> {
                    ally.heal(healAmount);
                    LOGGER.debug("Healed nearby ally {} for {} health", ally.getName().getString(), healAmount);
                });
    }
}