package com.tetra_loopback.integration.jei;

import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class AncientForgeRecipeDisplay {

    private final AncientForgeRecipe recipe;

    public AncientForgeRecipeDisplay(AncientForgeRecipe recipe) {
        this.recipe = recipe;
    }

    public AncientForgeRecipe getRecipe() {
        return recipe;
    }

    //获取所有输入（搜索）
    public List<Ingredient> getInputs() {
        List<Ingredient> inputs = new ArrayList<>(recipe.getMaterials());
        if (!recipe.getCatalystData().getIngredient().isEmpty()) {
            inputs.add(recipe.getCatalystData().getIngredient());
        }
        if (!recipe.getEnergySourceData().getIngredient().isEmpty()) {
            inputs.add(recipe.getEnergySourceData().getIngredient());
        }
        return inputs;
    }

    //获取所有输出（搜索）
    public List<ItemStack> getOutputs() {
        List<ItemStack> outputs = new ArrayList<>();

        //主输出
        List<ItemStack> mainOutputs = recipe.getMainOutputs();
        if (!mainOutputs.isEmpty()) {
            ItemStack output = mainOutputs.get(0).copy();
            if (recipe.getMainOutputMultiplier() > 1.0f) {
                int newCount = (int) Math.ceil(output.getCount() * recipe.getMainOutputMultiplier());
                output.setCount(Math.min(newCount, output.getMaxStackSize()));
            }
            outputs.add(output);
        }

        //副输出
        List<ItemStack> sideOutputs = recipe.getSideOutputs();
        if (!sideOutputs.isEmpty()) {
            ItemStack output = sideOutputs.get(0).copy();
            float finalMultiplier = recipe.getSideOutputMultiplier() / recipe.getMainToSideRatio();
            int newCount = (int) Math.ceil(output.getCount() * finalMultiplier);
            output.setCount(Math.min(newCount, output.getMaxStackSize()));
            outputs.add(output);
        }

        //返还材料
        List<ItemStack> returnMaterials = recipe.getReturnMaterials();
        if (!returnMaterials.isEmpty() && recipe.getReturnChance() > 0) {
            outputs.add(returnMaterials.get(0).copy());
        }

        return outputs;
    }

    //获取配方ID
    public String getRecipeId() {
        return recipe.getId().toString();
    }

    //检查是否包含催化剂
    public boolean hasCatalyst() {
        return !recipe.getCatalystData().getIngredient().isEmpty();
    }

    //获取催化剂信息
    public String getCatalystInfo() {
        if (hasCatalyst()) {
            StringBuilder info = new StringBuilder();
            if (recipe.getCatalystData().isRequired()) {
                info.append("Required");
            }
            if (recipe.getCatalystData().getTimeReduction() > 0) {
                if (!info.isEmpty()) info.append(", ");
                info.append(String.format("-%.0f%% Time",
                        recipe.getCatalystData().getTimeReduction() * 100));
            }
            return info.toString();
        }
        return "";
    }

    //获取能量信息
    public String getEnergyInfo() {
        int energy = recipe.getEnergySourceData().getEnergyValue();
        if (recipe.getEnergySourceData().acceptAnyFuel()) {
            return String.format("%d ticks (Any Fuel)", energy);
        }
        return String.format("%d ticks", energy);
    }

    //获取合成时间
    public int getProcessTimeSeconds() {
        return recipe.getProcessTime() / 20;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AncientForgeRecipeDisplay other)) return false;
        return recipe.getId().equals(other.recipe.getId());
    }

    @Override
    public int hashCode() {
        return recipe.getId().hashCode();
    }
}