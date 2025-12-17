package com.tetra_loopback.block.ancientforge.recipe;

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

import static com.tetra_loopback.block.ancientforge.entity.AncientForgeBlockEntity.MATERIAL_SLOTS;

public class AncientForgeRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final NonNullList<Ingredient> materials;
    private final CatalystData catalystData;
    private final EnergySourceData energySourceData;
    private final NonNullList<ItemStack> mainOutputs;
    private final NonNullList<ItemStack> sideOutputs;
    private final NonNullList<ItemStack> returnMaterials;
    private final int processTime;

    //产出倍率参数
    private final float mainOutputMultiplier;
    private final float sideOutputMultiplier;
    private final float mainToSideRatio;
    private final float returnChance;

    //催化剂数据类
    public static class CatalystData {
        private final Ingredient ingredient;
        private final boolean optional;      //催化剂是否可选
        private final boolean required;      //是否必需才能启动
        private final float timeReduction;   //时间减少比例

        public CatalystData(Ingredient ingredient, boolean optional, boolean required, float timeReduction) {
            this.ingredient = ingredient;
            this.optional = optional;
            this.required = required;
            this.timeReduction = Math.max(0, Math.min(1, timeReduction));  //限制在0-1之间
        }

        public boolean isValidCatalyst(ItemStack stack) {
            return !stack.isEmpty() && ingredient.test(stack);
        }

        public Ingredient getIngredient() { return ingredient; }
        public boolean isOptional() { return optional; }
        public boolean isRequired() { return required; }
        public float getTimeReduction() { return timeReduction; }
    }

    //能量源数据类
    public static class EnergySourceData {
        private final Ingredient ingredient;
        private final int energyValue;
        private final boolean acceptAnyFuel;      //是否接受任何燃料
        private final float fuelMultiplier;       //通用燃料倍率

        public EnergySourceData(Ingredient ingredient, int energyValue,
                                boolean acceptAnyFuel, float fuelMultiplier) {
            this.ingredient = ingredient;
            this.energyValue = Math.max(0, energyValue);
            this.acceptAnyFuel = acceptAnyFuel;
            this.fuelMultiplier = Math.max(0, fuelMultiplier);
        }

        public boolean isValidFuel(ItemStack stack) {
            return !stack.isEmpty() && ingredient.test(stack);
        }

        public Ingredient getIngredient() { return ingredient; }
        public int getEnergyValue() { return energyValue; }
        public boolean acceptAnyFuel() { return acceptAnyFuel; }
        public float getFuelMultiplier() { return fuelMultiplier; }
    }

    public AncientForgeRecipe(ResourceLocation id, NonNullList<Ingredient> materials,
                              CatalystData catalystData, EnergySourceData energySourceData,
                              NonNullList<ItemStack> mainOutputs, NonNullList<ItemStack> sideOutputs,
                              NonNullList<ItemStack> returnMaterials, int processTime,
                              float mainOutputMultiplier, float sideOutputMultiplier,
                              float mainToSideRatio, float returnChance) {
        this.id = id;
        this.materials = materials;
        this.catalystData = catalystData;
        this.energySourceData = energySourceData;
        this.mainOutputs = mainOutputs;
        this.sideOutputs = sideOutputs;
        this.returnMaterials = returnMaterials;
        this.processTime = processTime;
        this.mainOutputMultiplier = mainOutputMultiplier;
        this.sideOutputMultiplier = sideOutputMultiplier;
        this.mainToSideRatio = mainToSideRatio;
        this.returnChance = returnChance;
    }

    //matches
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

        //如果催化剂是必需的，必须存在且匹配
        if (catalystData.isRequired() && !catalystData.isValidCatalyst(catalystStack)) {
            return false;
        }

        //如果催化剂不是可选的且放入了物品，必须匹配
        if (!catalystData.isOptional() && !catalystStack.isEmpty() &&
                !catalystData.isValidCatalyst(catalystStack)) {
            return false;
        }

        //检查能量源
        ItemStack energyStack = container.getItem(MATERIAL_SLOTS + 1);
        if (energyStack.isEmpty()) {
            return false;
        }

        //如果能量源是指定的，必须匹配
        if (!energySourceData.acceptAnyFuel() && !energySourceData.isValidFuel(energyStack)) {
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

    //Getters
    public int getProcessTime() { return processTime; }
    public NonNullList<Ingredient> getMaterials() { return materials; }
    public CatalystData getCatalystData() { return catalystData; }
    public EnergySourceData getEnergySourceData() { return energySourceData; }
    public NonNullList<ItemStack> getMainOutputs() { return mainOutputs; }
    public NonNullList<ItemStack> getSideOutputs() { return sideOutputs; }
    public NonNullList<ItemStack> getReturnMaterials() { return returnMaterials; }
    public float getMainOutputMultiplier() { return mainOutputMultiplier; }
    public float getSideOutputMultiplier() { return sideOutputMultiplier; }
    public float getMainToSideRatio() { return mainToSideRatio; }
    public float getReturnChance() { return returnChance; }

    public static class Serializer implements RecipeSerializer<AncientForgeRecipe> {

        @Override
        public AncientForgeRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            JsonArray materialsArray = GsonHelper.getAsJsonArray(json, "materials");
            NonNullList<Ingredient> materials = NonNullList.withSize(9, Ingredient.EMPTY);

            for (int i = 0; i < materialsArray.size(); i++) {
                materials.set(i, Ingredient.fromJson(materialsArray.get(i)));
            }

            //解析催化剂数据
            JsonObject catalystObj = GsonHelper.getAsJsonObject(json, "catalyst");
            Ingredient catalystIngredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(catalystObj, "ingredient"));
            boolean catalystOptional = GsonHelper.getAsBoolean(catalystObj, "optional", true);
            boolean catalystRequired = GsonHelper.getAsBoolean(catalystObj, "required", false);
            float timeReduction = GsonHelper.getAsFloat(catalystObj, "time_reduction", 0.0f);

            CatalystData catalystData = new CatalystData(catalystIngredient, catalystOptional,
                    catalystRequired, timeReduction);

            //解析能量源数据
            JsonObject energyObj = GsonHelper.getAsJsonObject(json, "energy_source");
            Ingredient energyIngredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(energyObj, "ingredient"));
            int energyValue = GsonHelper.getAsInt(energyObj, "energy_value", 200);
            boolean acceptAnyFuel = GsonHelper.getAsBoolean(energyObj, "accept_any_fuel", false);
            float fuelMultiplier = GsonHelper.getAsFloat(energyObj, "fuel_multiplier", 1.0f);

            EnergySourceData energySourceData = new EnergySourceData(energyIngredient, energyValue,
                    acceptAnyFuel, fuelMultiplier);

            //解析输出
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

            //解析其他参数
            int processTime = GsonHelper.getAsInt(json, "process_time", 200);
            float mainOutputMultiplier = GsonHelper.getAsFloat(json, "main_output_multiplier", 1.0f);
            float sideOutputMultiplier = GsonHelper.getAsFloat(json, "side_output_multiplier", 1.0f);
            float mainToSideRatio = GsonHelper.getAsFloat(json, "main_to_side_ratio", 2.0f);
            float returnChance = GsonHelper.getAsFloat(json, "return_chance", 0.0f);

            return new AncientForgeRecipe(recipeId, materials, catalystData, energySourceData,
                    mainOutputs, sideOutputs, returnMaterials, processTime,
                    mainOutputMultiplier, sideOutputMultiplier, mainToSideRatio, returnChance);
        }

        @Override
        public AncientForgeRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> materials = NonNullList.withSize(9, Ingredient.EMPTY);
            for (int i = 0; i < 9; i++) {
                materials.set(i, Ingredient.fromNetwork(buffer));
            }

            //读取催化剂数据
            Ingredient catalystIngredient = Ingredient.fromNetwork(buffer);
            boolean catalystOptional = buffer.readBoolean();
            boolean catalystRequired = buffer.readBoolean();
            float timeReduction = buffer.readFloat();
            CatalystData catalystData = new CatalystData(catalystIngredient, catalystOptional,
                    catalystRequired, timeReduction);

            //读取能量源数据
            Ingredient energyIngredient = Ingredient.fromNetwork(buffer);
            int energyValue = buffer.readVarInt();
            boolean acceptAnyFuel = buffer.readBoolean();
            float fuelMultiplier = buffer.readFloat();
            EnergySourceData energySourceData = new EnergySourceData(energyIngredient, energyValue,
                    acceptAnyFuel, fuelMultiplier);

            //读取输出
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

            //读取其他参数
            int processTime = buffer.readVarInt();
            float mainOutputMultiplier = buffer.readFloat();
            float sideOutputMultiplier = buffer.readFloat();
            float mainToSideRatio = buffer.readFloat();
            float returnChance = buffer.readFloat();

            return new AncientForgeRecipe(recipeId, materials, catalystData, energySourceData,
                    mainOutputs, sideOutputs, returnMaterials, processTime,
                    mainOutputMultiplier, sideOutputMultiplier, mainToSideRatio, returnChance);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AncientForgeRecipe recipe) {
            for (Ingredient ingredient : recipe.materials) {
                ingredient.toNetwork(buffer);
            }

            //写入催化剂数据
            recipe.catalystData.getIngredient().toNetwork(buffer);
            buffer.writeBoolean(recipe.catalystData.isOptional());
            buffer.writeBoolean(recipe.catalystData.isRequired());
            buffer.writeFloat(recipe.catalystData.getTimeReduction());

            //写入能量源数据
            recipe.energySourceData.getIngredient().toNetwork(buffer);
            buffer.writeVarInt(recipe.energySourceData.getEnergyValue());
            buffer.writeBoolean(recipe.energySourceData.acceptAnyFuel());
            buffer.writeFloat(recipe.energySourceData.getFuelMultiplier());

            //写入输出
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

            //写入其他参数
            buffer.writeVarInt(recipe.processTime);
            buffer.writeFloat(recipe.mainOutputMultiplier);
            buffer.writeFloat(recipe.sideOutputMultiplier);
            buffer.writeFloat(recipe.mainToSideRatio);
            buffer.writeFloat(recipe.returnChance);
        }
    }
}