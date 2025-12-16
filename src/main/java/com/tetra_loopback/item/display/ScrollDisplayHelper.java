package com.tetra_loopback.item.display;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;

public class ScrollDisplayHelper {

    public static ItemStack createDisplayScroll(String schematicKey, String category,
                                                String[] schematics, boolean intricate,
                                                int material, int ribbonColor, Integer... glyphs) {
        ItemStack scrollStack = new ItemStack(getTetraScrollItem());

        //构建卷轴NBT数据
        CompoundTag displayTag = createScrollNBT(schematicKey, category, schematics,
                intricate, material, ribbonColor, glyphs);
        scrollStack.setTag(displayTag);

        return scrollStack;
    }

    private static CompoundTag createScrollNBT(String key, String details, String[] schematics,
                                               boolean isIntricate, int material, int ribbon,
                                               Integer[] glyphs) {
        CompoundTag tag = new CompoundTag();
        CompoundTag blockEntityTag = new CompoundTag();
        ListTag dataList = new ListTag();
        CompoundTag scrollData = new CompoundTag();

        //设置卷轴基本数据
        scrollData.putString("key", key);
        if (details != null) {
            scrollData.putString("details", details);
        }
        scrollData.putBoolean("intricate", isIntricate);
        scrollData.putInt("material", material);
        scrollData.putString("ribbon", Integer.toHexString(ribbon));

        //设置glyph列表
        ListTag glyphList = new ListTag();
        for (int glyph : glyphs) {
            CompoundTag glyphTag = new CompoundTag();
            glyphTag.putInt("", glyph);
            glyphList.add(glyphTag);
        }
        scrollData.put("glyphs", glyphList);

        //设置原理图列表
        ListTag schematicList = new ListTag();
        for (String schematic : schematics) {
            CompoundTag schematicTag = new CompoundTag();
            schematicTag.putString("", "tetra:" + schematic);
            schematicList.add(schematicTag);
        }
        scrollData.put("schematics", schematicList);

        //设置效果列表（空列表）
        scrollData.put("effects", new ListTag());

        dataList.add(scrollData);
        blockEntityTag.put("data", dataList);
        tag.put("BlockEntityTag", blockEntityTag);

        return tag;
    }

    //获取Tetra卷轴物品
    private static net.minecraft.world.item.Item getTetraScrollItem() {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM
                .get(new ResourceLocation("tetra", "scroll_rolled"));
    }
}