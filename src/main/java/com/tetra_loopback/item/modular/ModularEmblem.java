package com.tetra_loopback.item.modular;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;



public class ModularEmblem extends ModularItem implements ICurio {
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
    public ItemStack getStack() {
        return new ItemStack(instance);
    }
}