package com.tetra_loopback.item.modular;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ModularEmblem extends ModularItem implements ICurioItem {
    public final static String emblemBase = "emblem/base";
    public final static String emblemPattern = "emblem/pattern";
    public final static String emblemCarving = "emblem/carving";

    public static final String identifier = "modular_emblem";

    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(4, 20, -12, 20);
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(-13, -1);

    @ObjectHolder(
            registryName = "item",
            value = "tetra:modular_emblem"
    )
    public static ModularEmblem instance;

    public ModularEmblem() {
        super(new Item.Properties().stacksTo(1).fireResistant());

        canHone = false;

        majorModuleKeys = new String[]{emblemBase, emblemPattern};
        minorModuleKeys = new String[]{emblemCarving};

        requiredModules = new String[]{emblemBase, emblemPattern};
    }

    @Override
    public Collection<ItemModule> getAllModules(ItemStack stack) {
        CompoundTag stackTag = stack.getTag();
        if (stackTag != null) {
            return Stream.concat(Arrays.stream(getMajorModuleKeys(stack)), Arrays.stream(getMinorModuleKeys(stack)))
                    .map(stackTag::getString)
                    .map(ItemUpgradeRegistry.instance::getModule)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        return majorOffsets;
    }

    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        return minorOffsets;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();
        if (!this.isBroken(stack)) {
            Multimap<Attribute, AttributeModifier> Tetra = this.getAttributeModifiersCached(stack);
            result.putAll(Tetra);
        }
        return result;
    }



    //ICurioItem
    public abstract boolean canEquipFromUse(SlotContext slotContext, ItemStack stack);

//    ways
//    @Override
//    public void curioTick(SlotContext slotContext, ItemStack stack) {
//        //粒子效果,动态渲染
//        /*
//        LivingEntity entity = slotContext.entity();
//        if (entity.level().isClientSide && entity.tickCount % 10 == 0) {
//            entity.level().addParticle(
//                ParticleTypes.END_ROD,
//                entity.getX(),
//                entity.getY() + 1.5,
//                entity.getZ(),
//                0, 0, 0
//            );
//        }
//        */
//    }
//
//    @Override
//    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
//        //装备时逻辑
//        //播放声音
//        /*
//        LivingEntity entity = slotContext.entity();
//        if (!entity.level().isClientSide) {
//            entity.level().playSound(null, entity.blockPosition(),
//                SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS,
//                1.0F, 1.0F);
//        }
//        */
//    }
//
//    @Override
//    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
//        //卸下时逻辑
//        //播放声音
//        /*
//        LivingEntity entity = slotContext.entity();
//        if (!entity.level().isClientSide) {
//            entity.level().playSound(null, entity.blockPosition(),
//                SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS,
//                1.0F, 0.8F);
//        }
//        */
//    }
}
