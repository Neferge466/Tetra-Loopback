package com.tetra_loopback.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.tetra_loopback.block.entity.AncientForgeBlockEntity.MATERIAL_SLOTS;

public class AncientForgeRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final NonNullList<Ingredient> materials;
    private final Ingredient catalyst;
    private final Ingredient energySource;
    private final NonNullList<ItemStack> mainOutputs;
    private final NonNullList<ItemStack> sideOutputs;
    private final NonNullList<ItemStack> returnMaterials;
    private final int processTime;

    // 新增字段
    private final int energyValue; //能量物品的燃烧时间（tick）
    private final float timeReduction; //催化剂时间缩减比例（0-1）
    private final float mainOutputMultiplier; //主产物增产倍数
    private final float sideOutputMultiplier; //副产物增产倍数
    private final float mainToSideRatio; //主副产物比例
    private final float returnChance; //返还材料几率（0-1）

    public AncientForgeRecipe(ResourceLocation id, NonNullList<Ingredient> materials, Ingredient catalyst,
                              Ingredient energySource, NonNullList<ItemStack> mainOutputs,
                              NonNullList<ItemStack> sideOutputs, NonNullList<ItemStack> returnMaterials,
                              int processTime, int energyValue, float timeReduction,
                              float mainOutputMultiplier, float sideOutputMultiplier,
                              float mainToSideRatio, float returnChance) {
        this.id = id;
        this.materials = materials;
        this.catalyst = catalyst;
        this.energySource = energySource;
        this.mainOutputs = mainOutputs;
        this.sideOutputs = sideOutputs;
        this.returnMaterials = returnMaterials;
        this.processTime = processTime;
        this.energyValue = energyValue;
        this.timeReduction = timeReduction;
        this.mainOutputMultiplier = mainOutputMultiplier;
        this.sideOutputMultiplier = sideOutputMultiplier;
        this.mainToSideRatio = mainToSideRatio;
        this.returnChance = returnChance;
    }

    //matches方法
    @Override
    public boolean matches(Container container, Level level) {
        //检查原料是否匹配
        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            ItemStack stack = container.getItem(i);
            Ingredient ingredient = materials.get(i);
            if (!ingredient.test(stack)) {
                return false;
            }
        }

        //检查催化剂
        ItemStack catalystStack = container.getItem(MATERIAL_SLOTS);
        if (!catalyst.test(catalystStack)) {
            return false;
        }

        //检查能量源
        ItemStack energyStack = container.getItem(MATERIAL_SLOTS + 1);
        if (!energySource.test(energyStack)) {
            return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return applyOutputMultipliers(mainOutputs.get(0).copy());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return applyOutputMultipliers(mainOutputs.get(0).copy());
    }

    //应用产出倍率
    private ItemStack applyOutputMultipliers(ItemStack stack) {
        if (mainOutputMultiplier > 1.0f) {
            int newCount = (int) Math.ceil(stack.getCount() * mainOutputMultiplier);
            stack.setCount(Math.min(newCount, stack.getMaxStackSize()));
        }
        return stack;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TLbRecipes.ANCIENT_FORGE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return AncientForgeRecipeType.INSTANCE;
    }

    public int getProcessTime() {
        return processTime;
    }

    public NonNullList<Ingredient> getMaterials() {
        return materials;
    }

    public Ingredient getCatalyst() {
        return catalyst;
    }

    public Ingredient getEnergySource() {
        return energySource;
    }

    public NonNullList<ItemStack> getMainOutputs() {
        return mainOutputs;
    }

    public NonNullList<ItemStack> getSideOutputs() {
        return sideOutputs;
    }

    public NonNullList<ItemStack> getReturnMaterials() {
        return returnMaterials;
    }





    public int getEnergyValue() {
        return energyValue;
    }

    public float getTimeReduction() {
        return timeReduction;
    }

    public float getMainOutputMultiplier() {
        return mainOutputMultiplier;
    }

    public float getSideOutputMultiplier() {
        return sideOutputMultiplier;
    }

    public float getMainToSideRatio() {
        return mainToSideRatio;
    }

    public float getReturnChance() {
        return returnChance;
    }







    public static class Serializer implements RecipeSerializer<AncientForgeRecipe> {

        @Override
        public AncientForgeRecipe fromJson(ResourceLocation recipeId, JsonObject json) {

            JsonArray materialsArray = GsonHelper.getAsJsonArray(json, "materials");
            NonNullList<Ingredient> materials = NonNullList.withSize(9, Ingredient.EMPTY);

            for (int i = 0; i < materialsArray.size(); i++) {
                materials.set(i, Ingredient.fromJson(materialsArray.get(i)));
            }

            Ingredient catalyst = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "catalyst"));
            Ingredient energySource = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "energy_source"));

            JsonArray mainOutputsArray = GsonHelper.getAsJsonArray(json, "main_outputs");
            NonNullList<ItemStack> mainOutputs = NonNullList.create();

            for (int i = 0; i < mainOutputsArray.size(); i++) {
                JsonObject outputObject = mainOutputsArray.get(i).getAsJsonObject();
                mainOutputs.add(ShapedRecipe.itemStackFromJson(outputObject));
            }

            JsonArray sideOutputsArray = GsonHelper.getAsJsonArray(json, "side_outputs");
            NonNullList<ItemStack> sideOutputs = NonNullList.create();

            for (int i = 0; i < sideOutputsArray.size(); i++) {
                JsonObject outputObject = sideOutputsArray.get(i).getAsJsonObject();
                sideOutputs.add(ShapedRecipe.itemStackFromJson(outputObject));
            }

            JsonArray returnMaterialsArray = GsonHelper.getAsJsonArray(json, "return_materials");
            NonNullList<ItemStack> returnMaterials = NonNullList.create();

            for (int i = 0; i < returnMaterialsArray.size(); i++) {
                JsonObject outputObject = returnMaterialsArray.get(i).getAsJsonObject();
                returnMaterials.add(ShapedRecipe.itemStackFromJson(outputObject));
            }

            int processTime = GsonHelper.getAsInt(json, "process_time", 200);

            //解析
            int energyValue = GsonHelper.getAsInt(json, "energy_value", 200); // 默认200ticks（10秒）
            float timeReduction = GsonHelper.getAsFloat(json, "time_reduction", 0.0f); // 默认无缩减
            float mainOutputMultiplier = GsonHelper.getAsFloat(json, "main_output_multiplier", 1.0f); // 默认无增产
            float sideOutputMultiplier = GsonHelper.getAsFloat(json, "side_output_multiplier", 1.0f); // 默认无增产
            float mainToSideRatio = GsonHelper.getAsFloat(json, "main_to_side_ratio", 2.0f); // 默认2:1
            float returnChance = GsonHelper.getAsFloat(json, "return_chance", 0.0f); // 默认不返还

            return new AncientForgeRecipe(recipeId, materials, catalyst, energySource,
                    mainOutputs, sideOutputs, returnMaterials, processTime, energyValue,
                    timeReduction, mainOutputMultiplier, sideOutputMultiplier,
                    mainToSideRatio, returnChance);
        }

        @Override
        public AncientForgeRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> materials = NonNullList.withSize(9, Ingredient.EMPTY);

            for (int i = 0; i < 9; i++) {
                materials.set(i, Ingredient.fromNetwork(buffer));
            }

            Ingredient catalyst = Ingredient.fromNetwork(buffer);
            Ingredient energySource = Ingredient.fromNetwork(buffer);

            int mainOutputCount = buffer.readVarInt();
            NonNullList<ItemStack> mainOutputs = NonNullList.withSize(mainOutputCount, ItemStack.EMPTY);

            for (int i = 0; i < mainOutputCount; i++) {
                mainOutputs.set(i, buffer.readItem());
            }

            int sideOutputCount = buffer.readVarInt();
            NonNullList<ItemStack> sideOutputs = NonNullList.withSize(sideOutputCount, ItemStack.EMPTY);

            for (int i = 0; i < sideOutputCount; i++) {
                sideOutputs.set(i, buffer.readItem());
            }

            int returnMaterialCount = buffer.readVarInt();
            NonNullList<ItemStack> returnMaterials = NonNullList.withSize(returnMaterialCount, ItemStack.EMPTY);

            for (int i = 0; i < returnMaterialCount; i++) {
                returnMaterials.set(i, buffer.readItem());
            }

            int processTime = buffer.readVarInt();

            //
            int energyValue = buffer.readVarInt();
            float timeReduction = buffer.readFloat();
            float mainOutputMultiplier = buffer.readFloat();
            float sideOutputMultiplier = buffer.readFloat();
            float mainToSideRatio = buffer.readFloat();
            float returnChance = buffer.readFloat();

            return new AncientForgeRecipe(recipeId, materials, catalyst, energySource,
                    mainOutputs, sideOutputs, returnMaterials, processTime, energyValue,
                    timeReduction, mainOutputMultiplier, sideOutputMultiplier,
                    mainToSideRatio, returnChance);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AncientForgeRecipe recipe) {
            for (Ingredient ingredient : recipe.materials) {
                ingredient.toNetwork(buffer);
            }

            recipe.catalyst.toNetwork(buffer);
            recipe.energySource.toNetwork(buffer);

            buffer.writeVarInt(recipe.mainOutputs.size());
            for (ItemStack stack : recipe.mainOutputs) {
                buffer.writeItem(stack);
            }

            buffer.writeVarInt(recipe.sideOutputs.size());
            for (ItemStack stack : recipe.sideOutputs) {
                buffer.writeItem(stack);
            }

            buffer.writeVarInt(recipe.returnMaterials.size());
            for (ItemStack stack : recipe.returnMaterials) {
                buffer.writeItem(stack);
            }

            buffer.writeVarInt(recipe.processTime);
            buffer.writeVarInt(recipe.energyValue);
            buffer.writeFloat(recipe.timeReduction);
            buffer.writeFloat(recipe.mainOutputMultiplier);
            buffer.writeFloat(recipe.sideOutputMultiplier);
            buffer.writeFloat(recipe.mainToSideRatio);
            buffer.writeFloat(recipe.returnChance);
        }
    }
}