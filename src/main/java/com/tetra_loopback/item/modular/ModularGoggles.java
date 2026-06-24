package com.tetra_loopback.item.modular;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.tetra_loopback.TLbRegistry;
import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.sound.TLbSoundEvents;
import com.tetra_loopback.util.CuriosAttributesUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.ModuleModel;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



public abstract class ModularGoggles extends ModularItem implements ICurioItem {


    public final static String gogglesFrame = "goggles/frame";
    public final static String gogglesEdge = "goggles/edge";
    public final static String gogglesSinglelense = "goggles/singlelense";

    public final static String gogglesRightlense = "goggles/rightlense";
    public final static String gogglesLeftlense = "goggles/leftlense";
    public final static String gogglesStrap = "goggles/strap";
    public final static String gogglesBuckle = "goggles/buckle";

    public static final String identifier = "modular_goggles";

    //GUI偏移量
    private static final GuiModuleOffsets majorOffsetsWithBuckle = new GuiModuleOffsets(1,22, -10,22, -10,-4, 1,-4);
    private static final GuiModuleOffsets majorOffsetsWithoutBuckle = new GuiModuleOffsets(1,22, -10,22, -10,-4);

    private static final GuiModuleOffsets minorOffsetsBinocular = new GuiModuleOffsets(-22, 12, 14, 12);
    private static final GuiModuleOffsets minorOffsetsMonocular = new GuiModuleOffsets(-22, 12);


    //添加Resonance_NBT_KEY
    public static final String RESONANCE_NBT_KEY = "Resonance";
    public static final String RESONANCE_VALUE_KEY = "value";

    //设置共鸣值
    public static final double FIXED_RESONANCE_VALUE =0;


    @ObjectHolder(
            registryName = "item",
            value = "tetra:modular_goggles"
    )
    public static ModularGoggles instance;



    public ModularGoggles() {
        super(new Properties().stacksTo(1).fireResistant());

        canHone = false;

        majorModuleKeys = new String[]{gogglesFrame,gogglesStrap,gogglesEdge,gogglesBuckle};
        minorModuleKeys = new String[]{gogglesSinglelense,gogglesRightlense, gogglesLeftlense};

        requiredModules = new String[]{gogglesFrame,gogglesStrap,gogglesEdge};
        Tetra_loopback.items.add(this);
    }

    //Synergies
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
                .sorted(Comparator.comparing(ModuleModel::getRenderLayer))  // 修改
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }



    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        //bandage
        if (tag != null && tag.contains("bandage") && tag.getBoolean("bandage")) {
            return majorOffsetsWithBuckle; //4
        } else {
            return majorOffsetsWithoutBuckle; //3
        }
    }

    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        //binocular_frame
        if (tag != null && tag.contains("binocular_frame") && tag.getBoolean("binocular_frame")) {
            return minorOffsetsBinocular; //双
        } else {
            return minorOffsetsMonocular; //单
        }
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









    //设置物品的共鸣值
    public static void setResonanceValue(ItemStack stack, double value) {
        //0-20
        double clampedValue = Mth.clamp(value, 0, 20);

        CompoundTag ResonanceValueTag = new CompoundTag();
        ResonanceValueTag.putDouble(RESONANCE_VALUE_KEY, clampedValue);

        CompoundTag stackTag = stack.getOrCreateTag();
        stackTag.put(RESONANCE_NBT_KEY, ResonanceValueTag);

    }


    //获取物品的共鸣值
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



    @Override
    public void assemble(ItemStack itemStack, @Nullable Level world, float severity) {
        super.assemble(itemStack, world, severity);
        setResonanceValue(itemStack, FIXED_RESONANCE_VALUE);
        itemStack.getOrCreateTag().putBoolean("binocular_frame",
                getModuleFromSlot(itemStack,"goggles/frame")
                        .getVariantData(itemStack).key
                        .startsWith("binocular_frame/"));
        super.assemble(itemStack, world, severity);


        itemStack.getOrCreateTag().putBoolean("bandage",
                getModuleFromSlot(itemStack,"goggles/strap")
                        .getVariantData(itemStack).key
                        .startsWith("bandage/"));
        super.assemble(itemStack, world, severity);

    }

    @Override
    public String[] getMinorModuleKeys(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();

        if (tag != null && tag.contains("binocular_frame") && tag.getBoolean("binocular_frame")) {
            return new String[]{"goggles/rightlense", "goggles/leftlense"};
        } else {
            return new String[]{"goggles/singlelense"};
        }
    }

    @Override
    public String[] getMajorModuleKeys(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("bandage") && tag.getBoolean("bandage")) {
            return new String[]{"goggles/frame","goggles/strap","goggles/edge","goggles/buckle"};
        } else {
            return new String[]{"goggles/frame","goggles/strap","goggles/edge"};
        }
    }







    //ICurioItem
    public abstract boolean canEquipFromUse(SlotContext slotContext, ItemStack stack);


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
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {

        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide()) {
            entity.level().playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    TLbSoundEvents.GOGGLES_EQUIP.get(),
                    SoundSource.PLAYERS,
                    1.0F, //音量
                    1.0F  //音高
            );
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide()) {
            entity.level().playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    TLbSoundEvents.GOGGLES_UNEQUIP.get(),
                    SoundSource.PLAYERS,
                    1.0F, //音量
                    0.8F  //音高
            );
        }
    }


    public static Collection<ItemStack> getCreativeTabItemStacks() {
        Item gogglesItem = TLbRegistry.MODULAR_GOGGLES.get();

        return Arrays.asList(
                createBasicGoggles(gogglesItem)
        );
    }

    private static ItemStack createBasicGoggles(Item item) {
        ItemStack itemStack = new ItemStack(item);

        IModularItem.putModuleInSlot(itemStack, gogglesEdge,
                "goggles/edge/edge",
                "goggles/edge/edge_material",
                "edge/iron");

        IModularItem.putModuleInSlot(itemStack, gogglesFrame,
                "goggles/frame/single_frame",
                "goggles/frame/single_frame_material",
                "single_frame/fuse_steel_ingot");

        IModularItem.putModuleInSlot(itemStack, gogglesStrap,
                "goggles/strap/binding_rope",
                "goggles/strap/binding_rope_material",
                "binding_rope/string");

        IModularItem.updateIdentifier(itemStack);


        return itemStack;
    }


}