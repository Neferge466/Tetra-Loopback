package com.tetra_loopback.recipe;

import com.tetra_loopback.Tetra_loopback;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TLbRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Tetra_loopback.MODID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Tetra_loopback.MODID);

    public static final RegistryObject<RecipeSerializer<AncientForgeRecipe>> ANCIENT_FORGE_SERIALIZER =
            SERIALIZERS.register("ancient_forge", AncientForgeRecipe.Serializer::new);


    public static final RegistryObject<RecipeType<AncientForgeRecipe>> ANCIENT_FORGE_TYPE =
            RECIPE_TYPES.register("ancient_forge", () -> AncientForgeRecipeType.INSTANCE);
}