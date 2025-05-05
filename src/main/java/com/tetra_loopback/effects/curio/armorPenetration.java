package com.tetra_loopback.effects.curio;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ModularItem;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber
public class armorPenetration {
    private static final UUID ARMOR_PEN_UUID = UUID.fromString("d7e3a8b3-5a2f-481c-911d-c8b45e3f01a5");

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            getArmorPenetrationLevel(attacker).ifPresent(level ->
                    applyArmorReduction(event.getEntity(), level)
            );
        }
    }

    private static void applyArmorReduction(LivingEntity target, int effectLevel) {
        AttributeInstance armorAttr = target.getAttribute(Attributes.ARMOR);
        if (armorAttr != null && armorAttr.getModifier(ARMOR_PEN_UUID) == null) {
            armorAttr.addTransientModifier(new AttributeModifier(
                    ARMOR_PEN_UUID,
                    "curio_armor_pen",
                    effectLevel * -0.01,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        AttributeInstance armorAttr = target.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_PEN_UUID);
        }
    }

    private static Optional<Integer> getArmorPenetrationLevel(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(inv -> inv.findCurios(stack ->
                        stack.getItem() instanceof ModularItem &&
                                ((ModularItem) stack.getItem()).getEffectLevel(stack, ItemEffect.get("armorPenetration")) > 0
                ))
                .map(list -> list.stream()
                        .mapToInt(curio ->
                                ((ModularItem) curio.stack().getItem())
                                        .getEffectLevel(curio.stack(), ItemEffect.get("armorPenetration"))
                        )
                        .sum()
                );
    }
}
