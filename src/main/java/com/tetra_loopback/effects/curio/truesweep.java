package com.tetra_loopback.effects.curio;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.SweepingEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class truesweep {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            checkAndTriggerTruesweep(attacker);
        }
    }

    private static void checkAndTriggerTruesweep(Player player) {
        if (player.getAttackStrengthScale(0.5f) > 0.9f
                && player.onGround()
                && !player.isSprinting()
                && !player.getPersistentData().contains("ProcessingTruesweep")) {

            player.getPersistentData().putBoolean("ProcessingTruesweep", true);
            try {
                getTruesweepItems(player).ifPresent(items ->
                        items.forEach(itemStack ->
                                SweepingEffect.truesweep(itemStack, player, false)
                        )
                );
            } finally {
                player.getPersistentData().remove("ProcessingTruesweep");
            }
        }
    }

    private static Optional<List<ItemStack>> getTruesweepItems(Player player) {
        List<ItemStack> validStacks = new ArrayList<>();

        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            inv.getCurios().forEach((identifier, stacksHandler) -> {
                IDynamicStackHandler stacks = stacksHandler.getStacks();
                IDynamicStackHandler cosmetics = stacksHandler.getCosmeticStacks();

                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (isValidTruesweepStack(stack)) {
                        validStacks.add(stack);
                    }
                }

                for (int i = 0; i < cosmetics.getSlots(); i++) {
                    ItemStack cosmetic = cosmetics.getStackInSlot(i);
                    if (isValidTruesweepStack(cosmetic)) {
                        validStacks.add(cosmetic);
                    }
                }
            });
        });

        return validStacks.isEmpty() ? Optional.empty() : Optional.of(validStacks);
    }

    private static boolean isValidTruesweepStack(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof ModularItem
                && ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("truesweep")) > 0;
    }
}
