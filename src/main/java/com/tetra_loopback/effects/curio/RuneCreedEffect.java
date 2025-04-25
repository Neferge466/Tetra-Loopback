package com.tetra_loopback.effects.curio;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.effects.gui.ModEffectStats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID)
public class RuneCreedEffect {
    private static final UUID ARMOR_UUID = UUID.fromString("d9aae8a1-1234-5678-9abc-def012345682");
    private static final UUID ATTACK_UUID = UUID.fromString("d9aae8a1-1234-5678-9abc-def012345683");

    private static final Map<UUID, Integer> lastArmorEnchants = new HashMap<>();
    private static final Map<UUID, Integer> lastAttackEnchants = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;
        if (!hasEffect(player)) {
            clearModifiers(player);
            return;
        }

        if (player.tickCount % 5 == 0) {
            updateArmorBonus(player);
            updateAttackBonus(player);
        }
    }

    private static void updateArmorBonus(Player player) {
        AtomicInteger totalEnchants = new AtomicInteger();

        Arrays.stream(EquipmentSlot.values())
                .filter(slot -> slot.getType() == EquipmentSlot.Type.ARMOR)
                .map(player::getItemBySlot)
                .forEach(stack -> totalEnchants.addAndGet(EnchantmentHelper.getEnchantments(stack).size()));


        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            inv.getCurios().values().forEach(stacksHandler -> {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    totalEnchants.addAndGet(EnchantmentHelper.getEnchantments(stack).size());
                }
            });
        });


        double armorValue = totalEnchants.get() * 0.5;
        applyModifier(player, Attributes.ARMOR, ARMOR_UUID, "rune_armor",
                armorValue, AttributeModifier.Operation.ADDITION);

        lastArmorEnchants.put(player.getUUID(), totalEnchants.get());
    }

    private static void updateAttackBonus(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        int enchants = EnchantmentHelper.getEnchantments(mainHand).size();


        applyModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_UUID, "rune_attack",
                enchants, AttributeModifier.Operation.ADDITION);

        lastAttackEnchants.put(player.getUUID(), enchants);
    }

    private static void applyModifier(Player player, Attribute attribute, UUID uuid,
                                      String name, double value, AttributeModifier.Operation op) {
        Objects.requireNonNull(player.getAttribute(attribute)).removeModifier(uuid);
        if (value > 0) {
            AttributeModifier modifier = new AttributeModifier(uuid, name, value, op);
            player.getAttribute(attribute).addTransientModifier(modifier);
        }
    }

    private static void clearModifiers(Player player) {
        if (lastArmorEnchants.containsKey(player.getUUID())) {
            player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_UUID);
            lastArmorEnchants.remove(player.getUUID());
        }
        if (lastAttackEnchants.containsKey(player.getUUID())) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_UUID);
            lastAttackEnchants.remove(player.getUUID());
        }
    }

    private static boolean hasEffect(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ModEffectStats.runeCreedEffect) > 0
                ))
                .map(list -> !list.isEmpty())
                .orElse(false);
    }
}