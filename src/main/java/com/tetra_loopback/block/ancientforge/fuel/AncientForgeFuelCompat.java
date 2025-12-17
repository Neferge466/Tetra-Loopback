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

        //如果是配方指定燃料，使用配方能量值
        if (recipe != null) {
            AncientForgeRecipe.EnergySourceData energyData = recipe.getEnergySourceData();
            if (energyData.isValidFuel(fuel)) {
                return energyData.getEnergyValue();
            }

            //如果配方接受任何燃料
            if (energyData.acceptAnyFuel()) {
                int baseTime = getBaseBurnTime(fuel);
                if (baseTime > 0) {
                    return (int) (baseTime * energyData.getFuelMultiplier());
                }
            }
        }

        //如果没有配方或配方不接受通用燃料，返回0
        return 0;
    }

    private static int getBaseBurnTime(ItemStack fuel) {
        int burnTime = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);

        return Math.max(burnTime, 0);
    }

    public static boolean isValidFuel(ItemStack fuel, @Nullable AncientForgeRecipe recipe) {
        return getFuelValue(fuel, recipe) > 0;
    }
}