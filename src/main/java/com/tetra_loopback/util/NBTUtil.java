package com.tetra_loopback.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class NBTUtil {
    public static boolean hasNbtData(ItemStack stack, String key) {
        return stack.hasTag() && stack.getTag().contains(key);
    }

    public static CompoundTag getNbtData(ItemStack stack, String key) {
        if (hasNbtData(stack, key)) {
            return stack.getTag().getCompound(key);
        }
        return new CompoundTag();
    }

    public static void setNbtData(ItemStack stack, String key, CompoundTag data) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().put(key, data);
    }
}