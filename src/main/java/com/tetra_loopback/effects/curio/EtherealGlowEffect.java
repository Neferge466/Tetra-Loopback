package com.tetra_loopback.effects.curio;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class EtherealGlowEffect {
    private static final UUID SPEED_UUID = UUID.fromString("d9aae8a1-1234-5678-9abc-def012345681");
    private static final int CHECK_INTERVAL = 20; // 20tick
    private static final double RANGE = 12.0; // 12

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;
        boolean hasEffect = hasEffect(player);

        handleMovementSpeed(player, hasEffect);

        handleNightVision(player, hasEffect);

        if (player.tickCount % CHECK_INTERVAL == 0 && hasEffect) {
            applyGlowToMobs(player);
        }
    }

    private static void handleMovementSpeed(Player player, boolean active) {
        AttributeModifier modifier = new AttributeModifier(
                SPEED_UUID,
                "ethereal_glow_speed",
                0.2,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        if (active) {
            if (!player.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(modifier)) {
                player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(modifier);
            }
        } else {
            player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_UUID);
        }
    }

    private static void handleNightVision(Player player, boolean active) {
        final int DURATION = 400; // 20s(20*20=400 ticks)
        final int REFRESH_THRESHOLD = 200;

        if (active) {
            MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);


            if (current == null ||
                    current.getDuration() < REFRESH_THRESHOLD ||
                    !current.getDescriptionId().equals("effect.tetra_loopback.night_vision")) {

                player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        DURATION,
                        0,
                        false,
                        false,
                        true
                ) {
                    @Override
                    public String getDescriptionId() {
                        return "effect.tetra_loopback.night_vision";
                    }
                });
            }
        } else {
            MobEffectInstance effect = player.getEffect(MobEffects.NIGHT_VISION);
            if (effect != null && effect.getDescriptionId().equals("effect.tetra_loopback.night_vision")) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }

    private static void applyGlowToMobs(Player player) {
        int duration = 60; // 3s（60 ticks）

        player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RANGE))
                .stream()
                .filter(entity -> entity instanceof Enemy)
                .forEach(entity -> {

                    entity.addEffect(new MobEffectInstance(
                            MobEffects.GLOWING,
                            duration,
                            0,
                            false,
                            true,
                            true    // icon
                    ));

                    //
                    entity.setDeltaMovement(entity.getDeltaMovement());
                });
    }

    private static boolean hasEffect(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(itemStack ->
                        itemStack.getItem() instanceof ModularItem &&
                                ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack, ModEffectStats.etherealGlowEffect) > 0
                ))
                .map(list -> !list.isEmpty())
                .orElse(false);
    }
}
