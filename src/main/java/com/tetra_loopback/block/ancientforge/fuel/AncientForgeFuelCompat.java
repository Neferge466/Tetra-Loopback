package com.tetra_loopback.block.ancientforge.fuel;

import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;

public class AncientForgeFuelCompat {

    public static int getFuelValue(ItemStack fuel, @Nullable AncientForgeRecipe recipe) {
        if (fuel.isEmpty()) {
            return 0;
        }
        if (recipe != null) {
            AncientForgeRecipe.EnergySourceData energyData = recipe.getEnergySourceData();

            if (energyData.isValidFuel(fuel)) {
                return energyData.getEnergyValue();
            }

            if (energyData.acceptAnyFuel()) {
                int baseTime = getBaseBurnTime(fuel);
                if (baseTime > 0) {
                    return (int) (baseTime * energyData.getFuelMultiplier());
                }
            }
        } else {
            int baseTime = getBaseBurnTime(fuel);
            if (baseTime > 0) {
                //默认标准倍率
                return baseTime;
            }
        }

        return 0;
    }

    private static int getBaseBurnTime(ItemStack fuel) {
        int burnTime = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);

        if (burnTime <= 0) {
            if (fuel.is(TLbFuelTags.ANCIENT_FORGE_FUELS)) {
                return 1600;
            }

            if (fuel.is(TLbFuelTags.HIGH_ENERGY_FUELS)) {
                return 3200;
            }
        }

        return Math.max(burnTime, 0);
    }

    public static boolean isValidFuel(ItemStack fuel, @Nullable AncientForgeRecipe recipe) {
        return getFuelValue(fuel, recipe) > 0;
    }

    //检查燃料是否兼容（是否可以使用相同的燃烧时间）
    public static boolean isFuelCompatible(ItemStack fuel1, ItemStack fuel2,
                                           @Nullable AncientForgeRecipe recipe1,
                                           @Nullable AncientForgeRecipe recipe2) {
        //相同的物品
        if (ItemStack.isSameItemSameTags(fuel1, fuel2)) {
            return true;
        }

        if (fuel1.isEmpty() && fuel2.isEmpty()) {
            return true;
        }

        //检查燃料值是否相同
        int burnTime1 = getFuelValue(fuel1, recipe1);
        int burnTime2 = getFuelValue(fuel2, recipe2);

        //燃烧时间相同
        return burnTime1 == burnTime2;
    }
}