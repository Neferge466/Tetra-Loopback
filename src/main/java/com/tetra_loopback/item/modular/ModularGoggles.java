package com.tetra_loopback.item.modular;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.util.CuriosAttributesUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.ModuleModel;
import se.mickelus.tetra.properties.AttributeHelper;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



public abstract class ModularGoggles extends ModularItem implements ICurioItem {
    public final static String gogglesRightlense = "goggles/rightlense";
    public final static String gogglesLeftlense = "goggles/leftlense";
    public final static String gogglesStrap = "goggles/strap";
    public final static String gogglesBuckle = "goggles/buckle";

    public static final String identifier = "modular_goggles";

    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(4, 20, -12, 20, -13, -1, 4,-1);
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets();


    // 添加一个常量定义Resonance_NBT_KEY
    public static final String RESONANCE_NBT_KEY = "Resonance";
    public static final String RESONANCE_VALUE_KEY = "value";

    // 设置固定的共鸣值
    public static final double FIXED_RESONANCE_VALUE =16;


    @ObjectHolder(
            registryName = "item",
            value = "tetra:modular_goggles"
    )
    public static ModularGoggles instance;



    public ModularGoggles() {
        super(new Properties().stacksTo(1).fireResistant());

        canHone = false;

        majorModuleKeys = new String[]{gogglesRightlense, gogglesLeftlense,gogglesStrap,gogglesBuckle};
        minorModuleKeys = new String[]{};

        requiredModules = new String[]{gogglesStrap};
        Tetra_loopback.items.add(this);
    }

    // Synergies
    public void commonInit(PacketHandler packetHandler) {
        DataManager.instance.synergyData.onReload(() -> {
            this.synergies = DataManager.instance.synergyData.getOrdered("goggles/");

        });
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
    public ImmutableList<ModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        return Stream.concat(
                        Arrays.stream(getSynergyData(itemStack)).flatMap(synergyData -> Arrays.stream(synergyData.models)),
                        getAllModules(itemStack).stream()
                                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                                .flatMap(itemModule -> Arrays.stream(itemModule.getModels(itemStack)))
                )
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ModuleModel::getRenderLayer))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }



    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        return majorOffsets;
    }

    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        return minorOffsets;
    }


    public static Multimap<Attribute, AttributeModifier> Curios$fixIdentifiers(SlotContext slotContext, Multimap<Attribute, AttributeModifier> modifiers) {
        return Optional.ofNullable(modifiers)
                .map(Multimap::entries)
                .map(Collection::stream)
                .map((entries) -> entries.collect(
                        Multimaps.toMultimap(
                                Map.Entry::getKey,
                                (entry) ->
                                        new AttributeModifier(
                                                entry.getValue().getName() + slotContext.identifier() + slotContext.index(),
                                                entry.getValue().getAmount(),
                                                entry.getValue().getOperation()
                                        ),
                                ArrayListMultimap::create))
                ).orElse(null);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();
        if (!this.isBroken(stack)) {
            Multimap<Attribute, AttributeModifier> Tetra = this.getAttributeModifiersCached(stack);
            result.putAll(Tetra);
        }
        return CuriosAttributesUtil.Curios$fixIdentifiers(slotContext, result);
    }









    /**
     * 设置物品的共鸣值
     * @param stack 物品堆栈
     * @param value 共鸣值（-100到100之间）
     */
    public static void setResonanceValue(ItemStack stack, double value) {
        // 确保值在0-20范围内
        double clampedValue = Mth.clamp(value, 0, 20);

        CompoundTag ResonanceValueTag = new CompoundTag();
        ResonanceValueTag.putDouble(RESONANCE_VALUE_KEY, clampedValue);

        CompoundTag stackTag = stack.getOrCreateTag();
        stackTag.put(RESONANCE_NBT_KEY, ResonanceValueTag);
    }

    /**
     * 获取物品的共鸣值
     * @param stack 物品堆栈
     * @return 共鸣值，如果没有设置则返回0
     */
    public static double getResonanceValue(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(RESONANCE_NBT_KEY)) {
            CompoundTag ResonanceTag = stack.getTag().getCompound(RESONANCE_NBT_KEY);
            if (ResonanceTag.contains(RESONANCE_VALUE_KEY)) {
                return ResonanceTag.getDouble(RESONANCE_VALUE_KEY);
            }
        }
        return 0;
    }


//        itemStack.getOrCreateTag().putBoolean("gempeltate",
//                getModuleFromSlot(itemStack,"goggles/base")
//                        .getVariantData(itemStack).key
//                        .startsWith("gempeltate/"));
//        super.assemble(itemStack, world, severity);
// 在物品组装时写入固定的共鸣值到NBT


    @Override
    public void assemble(ItemStack itemStack, @Nullable Level world, float severity) {
        super.assemble(itemStack, world, severity);
        setResonanceValue(itemStack, FIXED_RESONANCE_VALUE);
    }







//    @Override
//    public String[] getMajorModuleKeys(ItemStack itemStack) {
//        CompoundTag tag = itemStack.getTag();
//        //check gempeltate  true
//        if (tag != null && tag.contains("gempeltate") && tag.getBoolean("gempeltate")) {
//            return new String[]{"emblem/base", "emblem/gemcore"};
//        } else {
//            return new String[]{"emblem/base", "emblem/pattern"};
//        }
//    }


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
