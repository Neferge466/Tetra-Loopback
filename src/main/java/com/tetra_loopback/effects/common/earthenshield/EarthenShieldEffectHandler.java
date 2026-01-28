package com.tetra_loopback.effects.common.earthenshield;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class EarthenShieldEffectHandler {
    private static final ConcurrentHashMap<UUID, PlayerEarthenState> playerStates = new ConcurrentHashMap<>();

    private static final int GROUND_BUFFER_TICKS = 10;           //地面缓冲ticks
    private static final int RESISTANCE_DURATION = 80;           //抗性持续时间 (ticks)
    private static final float FALL_DAMAGE_REDUCTION_LEVEL4 = 0.5f; //摔落伤害减免
    private static final float FALL_DAMAGE_REDUCTION_LEVEL5 = 0.25f; //摔落伤害减免
    private static final double AURA_RADIUS = 8.0;               //光环半径

    private static final UUID ARMOR_BOOST_UUID = UUID.fromString("EDAC4B00-0000-4000-8000-000000000000");
    private static final UUID ARMOR_TOUGHNESS_UUID = UUID.fromString("EDAC4B01-0000-4000-8000-000000000001");

    private static class EarthenShieldEffect {
        public int resistanceLevel;      //伤害抗性等级 (0=I, 1=II, 2=III, 3=IV)
        public float damageReduction;    //伤害减免百分比
        public int armorBoost;           //护甲提升
        public int toughnessBoost;       //护甲韧性提升
        public boolean hasFallReduction; //是否有摔落减免
        public boolean hasAura;          //是否有光环

        public EarthenShieldEffect(int resistanceLevel, float damageReduction,
                                   int armorBoost, int toughnessBoost,
                                   boolean hasFallReduction, boolean hasAura) {
            this.resistanceLevel = resistanceLevel;
            this.damageReduction = damageReduction;
            this.armorBoost = armorBoost;
            this.toughnessBoost = toughnessBoost;
            this.hasFallReduction = hasFallReduction;
            this.hasAura = hasAura;
        }
    }

    // 各等级效果配置
    private static final EarthenShieldEffect[] EFFECTS_BY_LEVEL = {
            null,
            new EarthenShieldEffect(0, 0.20f, 0, 0, false, false), //1: 抗性I (20%)
            new EarthenShieldEffect(1, 0.40f, 0, 0, false, false), //2: 抗性II (40%)
            new EarthenShieldEffect(1, 0.40f, 2, 0, false, false), //3: 抗性II + 2护甲
            new EarthenShieldEffect(2, 0.60f, 4, 1, true, false),  //4: 抗性III + 4护甲+1韧性 + 摔落减免
            new EarthenShieldEffect(3, 0.80f, 6, 2, true, true)    //5: 抗性IV + 6护甲+2韧性 + 摔落减免 + 光环
    };

    // 玩家状态类
    private static class PlayerEarthenState {
        public boolean isGrounded = false;                 //当前是否在地面（缓冲后）
        public boolean wasGroundedLastTick = false;        //上一tick是否在地面
        public boolean isActive = false;                   //是否激活抗性效果
        public int effectLevel = 0;                        //大地盾甲效果等级 (1-5)
        public int groundBufferTimer = 0;                  //地面缓冲计时器
        public boolean hasResistanceEffect = false;        //当前是否有抗性效果
        public boolean hasArmorBoost = false;              //当前是否有护甲提升
        public boolean hasAuraActive = false;              //当前是否有光环激活
    }

    //获取大地盾甲
    private static int getEarthenShieldLevel(Player player) {
        if (player == null) return 0;

        int maxLevel = 0;

        //检查主手
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() instanceof ModularItem) {
            int mainHandLevel = (int) ((ModularItem) mainHandItem.getItem())
                    .getEffectLevel(mainHandItem, ModEffectStats.earthenShieldEffect);
            maxLevel = Math.max(maxLevel, mainHandLevel);
        }

        try {
            var curiosInventory = CuriosApi.getCuriosInventory(player);
            if (curiosInventory.isPresent()) {
                var inventory = curiosInventory.resolve().orElse(null);
                if (inventory != null) {
                    var curios = inventory.findCurios(itemStack ->
                            itemStack.getItem() instanceof ModularItem &&
                                    ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack, ModEffectStats.earthenShieldEffect) > 0
                    );

                    for (SlotResult slotResult : curios) {
                        int curioLevel = (int) ((ModularItem) slotResult.stack().getItem())
                                .getEffectLevel(slotResult.stack(), ModEffectStats.earthenShieldEffect);
                        maxLevel = Math.max(maxLevel, curioLevel);
                    }
                }
            }
        } catch (Exception e) {
            //
        }

        return maxLevel;
    }

    private static boolean isProperlyGrounded(Player player) {
        return player.onGround() && !player.getAbilities().flying && !player.isInWater() && !player.isInLava();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        UUID playerId = player.getUUID();

        int effectLevel = getEarthenShieldLevel(player);
        if (effectLevel == 0) {
            cleanupPlayerState(player);
            return;
        }

        PlayerEarthenState state = playerStates.computeIfAbsent(playerId, k -> new PlayerEarthenState());
        state.effectLevel = effectLevel;

        boolean currentGrounded = isProperlyGrounded(player);

        if (currentGrounded) {
            state.isGrounded = true;
            state.groundBufferTimer = 0;
        } else {
            //短暂离开地面不立即失效
            if (state.isGrounded && state.groundBufferTimer == 0) {
                state.groundBufferTimer = GROUND_BUFFER_TICKS;
            }

            if (state.groundBufferTimer > 0) {
                state.groundBufferTimer--;
                state.isGrounded = true; //缓冲期仍视为地面
            } else {
                state.isGrounded = false;
            }
        }

        EarthenShieldEffect effectConfig = effectLevel >= 1 && effectLevel <= 5 ?
                EFFECTS_BY_LEVEL[effectLevel] : null;

        if (effectConfig == null) {
            cleanupPlayerState(player);
            return;
        }

        if (state.isGrounded) {
            applyEffects(player, state, effectConfig);
            state.isActive = true;
        } else {
            removeEffects(player, state);
            state.isActive = false;
        }

        state.wasGroundedLastTick = currentGrounded;

        if (player.tickCount % 200 == 0) {
            cleanupExpiredStates();
        }
    }

    private static void applyEffects(Player player, PlayerEarthenState state, EarthenShieldEffect effectConfig) {
        if (!state.hasResistanceEffect) {
            MobEffectInstance resistanceEffect = new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    RESISTANCE_DURATION,
                    effectConfig.resistanceLevel,
                    false,
                    true,
                    true
            );
            player.addEffect(resistanceEffect);
            state.hasResistanceEffect = true;
        } else {
            MobEffectInstance existingEffect = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (existingEffect != null && existingEffect.getAmplifier() == effectConfig.resistanceLevel) {
                existingEffect.update(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        RESISTANCE_DURATION,
                        effectConfig.resistanceLevel,
                        false, true, true
                ));
            }
        }

        if (effectConfig.armorBoost > 0 && !state.hasArmorBoost) {
            applyArmorBoost(player, effectConfig);
            state.hasArmorBoost = true;
        } else if (effectConfig.armorBoost == 0 && state.hasArmorBoost) {
            removeArmorBoost(player);
            state.hasArmorBoost = false;
        }

        if (effectConfig.hasAura) {
            applyResistanceAura(player);
            state.hasAuraActive = true;
        } else if (state.hasAuraActive) {
            state.hasAuraActive = false;
        }
    }
    private static void removeEffects(Player player, PlayerEarthenState state) {
        if (state.hasResistanceEffect) {
            MobEffectInstance effect = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (effect != null && effect.isVisible() && effect.showIcon()) {
                player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            }
            state.hasResistanceEffect = false;
        }

        if (state.hasArmorBoost) {
            removeArmorBoost(player);
            state.hasArmorBoost = false;
        }

        if (state.hasAuraActive) {
            state.hasAuraActive = false;
        }
    }

    private static void applyArmorBoost(Player player, EarthenShieldEffect effectConfig) {
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            AttributeModifier existingModifier = armorAttr.getModifier(ARMOR_BOOST_UUID);
            if (existingModifier == null || existingModifier.getAmount() != effectConfig.armorBoost) {
                armorAttr.removeModifier(ARMOR_BOOST_UUID);
                AttributeModifier modifier = new AttributeModifier(
                        ARMOR_BOOST_UUID,
                        "earthen_shield_armor_boost",
                        effectConfig.armorBoost,
                        AttributeModifier.Operation.ADDITION
                );
                armorAttr.addTransientModifier(modifier);
            }
        }

        if (effectConfig.toughnessBoost > 0) {
            AttributeInstance toughnessAttr = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (toughnessAttr != null) {
                AttributeModifier existingModifier = toughnessAttr.getModifier(ARMOR_TOUGHNESS_UUID);
                if (existingModifier == null || existingModifier.getAmount() != effectConfig.toughnessBoost) {
                    toughnessAttr.removeModifier(ARMOR_TOUGHNESS_UUID);
                    AttributeModifier modifier = new AttributeModifier(
                            ARMOR_TOUGHNESS_UUID,
                            "earthen_shield_toughness_boost",
                            effectConfig.toughnessBoost,
                            AttributeModifier.Operation.ADDITION
                    );
                    toughnessAttr.addTransientModifier(modifier);
                }
            }
        }
    }
    //移除护甲提升
    private static void removeArmorBoost(Player player) {
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_BOOST_UUID);
        }

        AttributeInstance toughnessAttr = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughnessAttr != null) {
            toughnessAttr.removeModifier(ARMOR_TOUGHNESS_UUID);
        }
    }

    //应用范围抗性光环（等级5）
    private static void applyResistanceAura(Player player) {
        //查找附近8格内的队友（非敌对玩家）
        AABB area = new AABB(
                player.getX() - AURA_RADIUS,
                player.getY() - 2.0,
                player.getZ() - AURA_RADIUS,
                player.getX() + AURA_RADIUS,
                player.getY() + 3.0,
                player.getZ() + AURA_RADIUS
        );

        player.level().getEntitiesOfClass(Player.class, area).forEach(nearbyPlayer -> {
            //排除自己
            if (nearbyPlayer == player) return;

            //抗性I给队友
            MobEffectInstance auraEffect = new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    60,
                    0,
                    false,
                    false,
                    false
            );
            MobEffectInstance existingEffect = nearbyPlayer.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (existingEffect == null || existingEffect.getAmplifier() < auraEffect.getAmplifier()) {
                nearbyPlayer.addEffect(auraEffect);
            }
        });
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        PlayerEarthenState state = playerStates.get(player.getUUID());

        if (state != null && state.isActive && state.effectLevel >= 4) {
            EarthenShieldEffect effectConfig = EFFECTS_BY_LEVEL[state.effectLevel];
            if (effectConfig.hasFallReduction) {
                if (state.effectLevel == 4) {
                    event.setDamageMultiplier(FALL_DAMAGE_REDUCTION_LEVEL4); //50%减免
                } else if (state.effectLevel == 5) {
                    event.setDamageMultiplier(FALL_DAMAGE_REDUCTION_LEVEL5); //75%减免
                }
            }
        }
    }

    private static void cleanupPlayerState(Player player) {
        UUID playerId = player.getUUID();
        PlayerEarthenState state = playerStates.get(playerId);

        if (state != null) {
            removeEffects(player, state);
        }

        playerStates.remove(playerId);
    }

    private static void cleanupExpiredStates() {
        long currentTime = System.currentTimeMillis();
        playerStates.entrySet().removeIf(entry -> {
            PlayerEarthenState state = entry.getValue();
            return !state.isActive && (System.currentTimeMillis() - currentTime > 30000);
        });
    }

    public static void cleanupPlayer(UUID playerId) {
        PlayerEarthenState state = playerStates.get(playerId);
        if (state != null) {
        }
        playerStates.remove(playerId);
    }
}