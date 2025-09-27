package com.tetra_loopback.recipe;

import net.minecraft.world.item.crafting.RecipeType;

public class AncientForgeRecipeType implements RecipeType<AncientForgeRecipe> {
    public static final AncientForgeRecipeType INSTANCE = new AncientForgeRecipeType();

    private AncientForgeRecipeType() {}

    @Override
    public String toString() {
        return "tetra_loopback:ancient_forge";
    }
}