package com.tetra_loopback.effects.curio.lifeessence;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.getter.resonance.ResonanceGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class LifeEssenceEffectHandler {

    private static final Set<Block> NATURAL_BLOCKS = new HashSet<>();
    private static final Map<UUID, Integer> healingCooldowns = new HashMap<>();
    private static final Map<UUID, Integer> overflowTimers = new HashMap<>();

    static {
        NATURAL_BLOCKS.add(Blocks.GRASS_BLOCK);
        NATURAL_BLOCKS.add(Blocks.MOSS_BLOCK);
        NATURAL_BLOCKS.add(Blocks.MOSS_CARPET);
        NATURAL_BLOCKS.add(Blocks.OAK_LEAVES);
        NATURAL_BLOCKS.add(Blocks.SPRUCE_LEAVES);
        NATURAL_BLOCKS.add(Blocks.BIRCH_LEAVES);
        NATURAL_BLOCKS.add(Blocks.JUNGLE_LEAVES);
        NATURAL_BLOCKS.add(Blocks.ACACIA_LEAVES);
        NATURAL_BLOCKS.add(Blocks.DARK_OAK_LEAVES);
        NATURAL_BLOCKS.add(Blocks.MANGROVE_LEAVES);
        NATURAL_BLOCKS.add(Blocks.AZALEA_LEAVES);
        NATURAL_BLOCKS.add(Blocks.FLOWERING_AZALEA_LEAVES);
        NATURAL_BLOCKS.add(Blocks.PODZOL);
        NATURAL_BLOCKS.add(Blocks.MYCELIUM);
        NATURAL_BLOCKS.add(Blocks.DIRT);
        NATURAL_BLOCKS.add(Blocks.COARSE_DIRT);
        NATURAL_BLOCKS.add(Blocks.ROOTED_DIRT);
        NATURAL_BLOCKS.add(Blocks.FARMLAND);
        NATURAL_BLOCKS.add(Blocks.CLAY);
        NATURAL_BLOCKS.add(Blocks.STONE);
        NATURAL_BLOCKS.add(Blocks.COBBLESTONE);
        NATURAL_BLOCKS.add(Blocks.ANDESITE);
        NATURAL_BLOCKS.add(Blocks.DIORITE);
        NATURAL_BLOCKS.add(Blocks.GRANITE);
        NATURAL_BLOCKS.add(Blocks.DEEPSLATE);
        NATURAL_BLOCKS.add(Blocks.TUFF);
    }

    public static int getLifeEssenceLevel(Player player) {
        if (player == null) return 0;
        try {
            return CuriosApi.getCuriosInventory(player)
                    .map(inv -> inv.findCurios(itemStack ->
                            itemStack.getItem() instanceof ModularItem &&
                                    ((ModularItem) itemStack.getItem()).getEffectLevel(itemStack,
                                            com.tetra_loopback.effects.gui.ModEffectStats.lifeEssenceEffect) > 0
                    ))
                    .map(list -> list.stream()
                            .mapToInt(curio -> (int) ((ModularItem) curio.stack().getItem())
                                    .getEffectLevel(curio.stack(), com.tetra_loopback.effects.gui.ModEffectStats.lifeEssenceEffect))
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
        int lifeEssenceLevel = getLifeEssenceLevel(player);

        if (lifeEssenceLevel == 0) {
            healingCooldowns.remove(player.getUUID());
            overflowTimers.remove(player.getUUID());
            return;
        }

        int resonanceStage = getResonanceStage(player);
        updateCooldowns(player);

        if (shouldApplyNaturalHealing(player, resonanceStage)) {
            applyNaturalHealing(player, resonanceStage);
        }

        handleOverflowCompensation(player, resonanceStage);
    }

    private static void updateCooldowns(Player player) {
        UUID playerId = player.getUUID();

        if (healingCooldowns.containsKey(playerId)) {
            int cooldown = healingCooldowns.get(playerId);
            if (cooldown <= 1) {
                healingCooldowns.remove(playerId);
            } else {
                healingCooldowns.put(playerId, cooldown - 1);
            }
        }

        if (overflowTimers.containsKey(playerId)) {
            int timer = overflowTimers.get(playerId);
            if (timer <= 1) {
                overflowTimers.remove(playerId);
            } else {
                overflowTimers.put(playerId, timer - 1);
            }
        }
    }

    private static boolean shouldApplyNaturalHealing(Player player, int resonanceStage) {
        if (getLifeEssenceLevel(player) == 0) return false;
        if (healingCooldowns.containsKey(player.getUUID())) return false;

        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        if (resonanceStage == 1) {
            return level.getMaxLocalRawBrightness(playerPos) > 12;
        }

        boolean hasEnoughLight = level.getMaxLocalRawBrightness(playerPos) > 12;
        if (hasEnoughLight) return true;

        BlockPos groundPos = playerPos.below();
        Block groundBlock = level.getBlockState(groundPos).getBlock();
        return NATURAL_BLOCKS.contains(groundBlock);
    }

    private static void applyNaturalHealing(Player player, int resonanceStage) {
        UUID playerId = player.getUUID();
        int amplifier = 0, duration = 0, cooldownTicks = 0;

        switch (resonanceStage) {
            case 1:
                duration = 40; cooldownTicks = 200; break;
            case 2:
                duration = 60; cooldownTicks = 120; break;
            case 3:
                duration = 80; cooldownTicks = 80; break;
            case 4:
                duration = 100; cooldownTicks = 40; break;
            default: return;
        }

        MobEffectInstance existingEffect = player.getEffect(MobEffects.REGENERATION);
        if (existingEffect == null || existingEffect.getAmplifier() < amplifier ||
                existingEffect.getDuration() < duration / 2) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION,
                    duration,
                    amplifier,
                    false, false, true
            ));
        }

        healingCooldowns.put(playerId, cooldownTicks);
    }

    private static void handleOverflowCompensation(Player player, int resonanceStage) {
        if (resonanceStage < 2) return;

        if (player.getHealth() >= player.getMaxHealth()) {
            UUID playerId = player.getUUID();
            int timer = overflowTimers.getOrDefault(playerId, 0);
            int checkInterval = 0, hungerAmount = 0;
            float saturationAmount = 0.0f;

            switch (resonanceStage) {
                case 2:
                    checkInterval = 200; hungerAmount = 1; break;
                case 3:
                    checkInterval = 160; hungerAmount = 2; saturationAmount = 0.25f; break;
                case 4:
                    checkInterval = 100; hungerAmount = 3; saturationAmount = 0.5f; break;
                default: return;
            }

            if (timer <= 0) {
                player.getFoodData().eat(hungerAmount, saturationAmount);
                overflowTimers.put(playerId, checkInterval);
            }
        } else {
            overflowTimers.remove(player.getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        int lifeEssenceLevel = getLifeEssenceLevel(player);
        if (lifeEssenceLevel == 0) return;

        MobEffectInstance effect = event.getEffectInstance();
        if (effect.getEffect().isBeneficial()) return;

        int resonanceStage = getResonanceStage(player);
        int originalDuration = effect.getDuration();
        int reducedDuration = (int) (originalDuration * (1.0f - getDebuffReduction(resonanceStage)));

        if (reducedDuration < 1) reducedDuration = 1;
        if (reducedDuration < originalDuration) {
            MobEffectInstance newEffect = new MobEffectInstance(
                    effect.getEffect(),
                    reducedDuration,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon()
            );

            player.removeEffect(effect.getEffect());
            player.addEffect(newEffect);
        }
    }

    private static float getDebuffReduction(int resonanceStage) {
        switch (resonanceStage) {
            case 1: return 0.15f;
            case 2: return 0.25f;
            case 3: return 0.35f;
            case 4: return 0.50f;
            default: return 0.15f;
        }
    }

    public static void cleanupPlayer(UUID playerId) {
        healingCooldowns.remove(playerId);
        overflowTimers.remove(playerId);
    }
}