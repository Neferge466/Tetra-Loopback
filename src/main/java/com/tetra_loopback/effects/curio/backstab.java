package com.tetra_loopback.effects.curio;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class backstab {

    private static final float BASE_DAMAGE_MULTIPLIER = 1.25f;
    private static final float LEVEL_BONUS = 0.25f;
    private static final float BASE_ANGLE_THRESHOLD = 60f;
    private static final float ANGLE_REDUCTION_PER_LEVEL = 5f;
    private static final float MIN_ANGLE_THRESHOLD = 15f;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() == null || !(event.getSource().getEntity() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getSource().getEntity();

        if (attacker.level().isClientSide()) {
            return;
        }

        int backstabLevel = getCurioBackstabLevel(attacker);
        if (backstabLevel <= 0) {
            return;
        }

        LivingEntity target = event.getEntity();
        float threshold = calculateAngleThreshold(backstabLevel);
        float actualAngle = calculateAttackAngle(attacker, target);

        if (actualAngle < threshold) {
            float originalDamage = event.getAmount();
            float damageMultiplier = BASE_DAMAGE_MULTIPLIER + (LEVEL_BONUS * backstabLevel);
            float newDamage = originalDamage * damageMultiplier;
            event.setAmount(newDamage);
        }
    }

    private static int getCurioBackstabLevel(Player player) {
        Optional<Integer> levelOptional = CuriosApi.getCuriosInventory(player)
                .map(inventory -> {
                    List<SlotResult> foundCurios = inventory.findCurios(stack -> {
                        Item item = stack.getItem();
                        return item instanceof IModularItem &&
                                ((IModularItem) item).getEffectLevel(stack, ItemEffect.backstab) > 0;
                    });

                    return foundCurios.stream()
                            .mapToInt(curio -> {
                                ItemStack stack = curio.stack();
                                return ((IModularItem) stack.getItem())
                                        .getEffectLevel(stack, ItemEffect.backstab);
                            })
                            .sum();
                });

        return levelOptional.orElse(0);
    }

    private static float calculateAngleThreshold(int level) {
        float threshold = BASE_ANGLE_THRESHOLD - (ANGLE_REDUCTION_PER_LEVEL * level);
        return Math.max(threshold, MIN_ANGLE_THRESHOLD);
    }

    private static float calculateAttackAngle(Player attacker, LivingEntity target) {
        float attackerYaw = normalizeAngle(attacker.getYRot());
        float targetYaw = normalizeAngle(target.getYRot());
        float angleDiff = Math.abs(attackerYaw - targetYaw);
        if (angleDiff > 180f) {
            angleDiff = 360f - angleDiff;
        }
        return angleDiff;
    }

    private static float normalizeAngle(float angle) {
        angle = angle % 360f;
        if (angle < 0f) {
            angle += 360f;
        }
        return angle;
    }
}
