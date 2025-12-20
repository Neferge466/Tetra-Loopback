package com.tetra_loopback.integration.jei;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.block.ancientforge.inventory.AncientForgeMenu;
import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipe;
import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipeType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class TLbJEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID =
            new ResourceLocation(Tetra_loopback.MODID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        //注册远古锻炉配方类别
        AncientForgeRecipeCategory recipeCategory = new AncientForgeRecipeCategory(guiHelper);
        registration.addRecipeCategories(recipeCategory);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) return;

        RecipeManager recipeManager = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();

        //获取所有远古锻炉配方
        List<AncientForgeRecipe> recipes = recipeManager.getAllRecipesFor(AncientForgeRecipeType.INSTANCE);

        //转换格式
        List<AncientForgeRecipeDisplay> recipeDisplays = new ArrayList<>();
        for (AncientForgeRecipe recipe : recipes) {
            recipeDisplays.add(new AncientForgeRecipeDisplay(recipe));
        }

        //注册配方
        registration.addRecipes(AncientForgeRecipeCategory.RECIPE_TYPE, recipeDisplays);

        //添加描述
        ItemStack ancientForge = new ItemStack(com.tetra_loopback.TLbRegistry.ANCIENT_FORGE_ITEM.get());
        registration.addIngredientInfo(
                ancientForge,
                VanillaTypes.ITEM_STACK,
                net.minecraft.network.chat.Component.translatable("jei.tetra_loopback.ancient_forge.description")
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        //为游戏内GUI添加JEI点击区域
        registration.addRecipeClickArea(
                com.tetra_loopback.block.ancientforge.screen.AncientForgeScreen.class,
                84, 40,  //进度条区域位置
                23, 7,   //进度条区域大小
                AncientForgeRecipeCategory.RECIPE_TYPE
        );

        //也可以添加燃烧条区域的点击
        registration.addRecipeClickArea(
                com.tetra_loopback.block.ancientforge.screen.AncientForgeScreen.class,
                91, 55,  //燃烧条区域位置
                10, 18,  //燃烧条区域大小
                AncientForgeRecipeCategory.RECIPE_TYPE
        );
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        //注册配方传输处理器
        registration.addRecipeTransferHandler(
                new AncientForgeRecipeTransferHandler(registration.getTransferHelper()),
                AncientForgeRecipeCategory.RECIPE_TYPE
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        //注册为配方催化剂
        ItemStack ancientForge = new ItemStack(
                com.tetra_loopback.TLbRegistry.ANCIENT_FORGE_ITEM.get()
        );
        registration.addRecipeCatalyst(
                ancientForge,
                AncientForgeRecipeCategory.RECIPE_TYPE
        );
    }
}