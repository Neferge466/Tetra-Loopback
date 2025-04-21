package com.tetra_loopback.effects.curio;

import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

@Mod.EventBusSubscriber(modid = "tetra_loopback")
public class AttributeCurioEffect {
    private static final Map<UUID, Map<Attribute, AttributeModifier>> playerModifiers = new HashMap<>();
    private static final Map<ItemEffect, Attribute> effectToAttribute = Map.of(
            ModEffectStats.attackDamageEffect, Attributes.ATTACK_DAMAGE,
            ModEffectStats.armorEffect, Attributes.ARMOR,
            ModEffectStats.armorToughnessEffect, Attributes.ARMOR_TOUGHNESS,
            ModEffectStats.movementSpeedEffect, Attributes.MOVEMENT_SPEED
    );

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;
        UUID playerId = player.getUUID();
        clearOldModifiers(player);

        Map<Attribute, Integer> totalLevels = new HashMap<>();
        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            inv.findCurios(itemStack -> itemStack.getItem() instanceof ModularItem)
                    .forEach(slotResult -> {
                        ItemStack stack = slotResult.stack();
                        ModularItem item = (ModularItem) stack.getItem();
                        effectToAttribute.forEach((effect, attribute) -> {
                            int level = item.getEffectLevel(stack, effect);
                            if (level > 0) {
                                totalLevels.merge(attribute, level, Integer::sum);
                            }
                        });
                    });
        });

        applyModifiers(player, totalLevels);
    }

    private static void clearOldModifiers(Player player) {
        Map<Attribute, AttributeModifier> modifiers = playerModifiers.getOrDefault(player.getUUID(), new HashMap<>());
        modifiers.forEach((attribute, modifier) -> {
            if (player.getAttribute(attribute) != null) {
                player.getAttribute(attribute).removeModifier(modifier);
            }
        });
        playerModifiers.remove(player.getUUID());
    }

    private static void applyModifiers(Player player, Map<Attribute, Integer> totalLevels) {
        Map<Attribute, AttributeModifier> applied = new HashMap<>();
        totalLevels.forEach((attribute, level) -> {
            double value = getValueByAttribute(attribute, level);
            AttributeModifier modifier = new AttributeModifier(
                    UUID.nameUUIDFromBytes(attribute.getDescriptionId().getBytes()),
                    "tetra_loopback_attribute",
                    value,
                    getOperation(attribute)
            );
            if (player.getAttribute(attribute) != null) {
                player.getAttribute(attribute).addTransientModifier(modifier);
                applied.put(attribute, modifier);
            }
        });
        playerModifiers.put(player.getUUID(), applied);
    }

    private static double getValueByAttribute(Attribute attribute, int level) {
        if (attribute == Attributes.MOVEMENT_SPEED) {
            return 0.01 * level;
        } else {
            return level;
        }
    }

    private static AttributeModifier.Operation getOperation(Attribute attribute) {
        return (attribute == Attributes.MOVEMENT_SPEED) ?
                AttributeModifier.Operation.MULTIPLY_TOTAL :
                AttributeModifier.Operation.ADDITION;
    }
}