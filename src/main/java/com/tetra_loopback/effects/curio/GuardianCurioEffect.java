package com.tetra_loopback.effects.curio;

import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "tetra_loopback")
public class GuardianCurioEffect {
    private static final UUID GUARDIAN_ARMOR_UUID = UUID.fromString("d9aae8a1-1234-5678-9abc-def012345678");
    private static final String GUARDIAN_LEVEL_TAG = "GuardianArmorLevel";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide) return;

        int totalLevel = CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(itemStack -> {
                    if (!(itemStack.getItem() instanceof ModularItem)) return false;

                    ModularItem item = (ModularItem) itemStack.getItem();

                    int level = item.getEffectLevel(itemStack, ModEffectStats.guardianEffect);

                    return level > 0;
                }))
                .map(list -> list.stream()
                        .mapToInt(slotResult -> {
                            ItemStack stack = slotResult.stack();
                            ModularItem item = (ModularItem) stack.getItem();

                            return item.getEffectLevel(stack, ModEffectStats.guardianEffect);
                        })
                        .sum()
                )
                .orElse(0);

        double armorBonus = 4 * totalLevel;

        if (player.getAttributes().hasAttribute(Attributes.ARMOR)) {
            player.getAttribute(Attributes.ARMOR).removeModifier(GUARDIAN_ARMOR_UUID);

            if (totalLevel > 0) {
                AttributeModifier modifier = new AttributeModifier(
                        GUARDIAN_ARMOR_UUID,
                        "Guardian Armor Bonus",
                        armorBonus,
                        AttributeModifier.Operation.ADDITION
                );
                player.getAttribute(Attributes.ARMOR).addTransientModifier(modifier);
            }
        }

    }
}