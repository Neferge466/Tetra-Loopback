package com.tetra_loopback.integration.jei;

import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AncientForgeRecipeCategory implements IRecipeCategory<AncientForgeRecipeDisplay> {

    public static final RecipeType<AncientForgeRecipeDisplay> RECIPE_TYPE =
            RecipeType.create(Tetra_loopback.MODID, "ancient_forge", AncientForgeRecipeDisplay.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    // 槽位位置调整（与原版GUI对齐）
    private static final int MATERIAL_START_X = 12;
    private static final int MATERIAL_START_Y = 15;
    private static final int MATERIAL_SPACING = 17;

    private static final int CATALYST_X = 76;
    private static final int CATALYST_Y = 15;

    private static final int ENERGY_X = 88;
    private static final int ENERGY_Y = 80;

    private static final int MAIN_OUTPUT_X = 136;
    private static final int MAIN_OUTPUT_Y = 15;

    private static final int SIDE_OUTPUT_X = 136;
    private static final int SIDE_OUTPUT_Y = 60;

    private static final int RETURN_X = 12;
    private static final int RETURN_Y = 72;

    // 动画位置（与原版GUI对齐）
    private static final int BURN_X = 91;
    private static final int BURN_Y = 55;
    private static final int BURN_WIDTH = 10;
    private static final int BURN_HEIGHT = 18;

    private static final int CRAFT_X = 84;
    private static final int CRAFT_Y = 40;
    private static final int CRAFT_WIDTH = 23;
    private static final int CRAFT_HEIGHT = 8;

    // 燃烧条帧的纹理坐标
    private static final int[][] BURN_FRAMES = {
            {176, 16},
            {192, 16},
            {208, 16},
            {224, 16},
            {240, 16},
            {176, 38}
    };

    // 进度条帧的纹理坐标
    private static final int[][] CRAFT_FRAMES = {
            {230, 80},
            {203, 80},
            {176, 80},
            {230, 69},
            {203, 69},
            {176, 69}
    };

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    private final IDrawable[] burnFrames;
    private final IDrawable[] craftFrames;
    private int currentBurnFrame = 0;
    private int currentCraftFrame = 0;
    private long lastUpdateTime = 0;
    private static final long FRAME_DURATION = 200;

    public AncientForgeRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation backgroundTexture = new ResourceLocation(
                Tetra_loopback.MODID, "textures/gui/ancient_forge.png"
        );

        this.background = guiHelper.drawableBuilder(backgroundTexture, 0, 0, WIDTH, HEIGHT)
                .setTextureSize(256, 256)
                .build();

        this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(com.tetra_loopback.TLbRegistry.ANCIENT_FORGE_ITEM.get())
        );
        this.title = Component.translatable("block.tetra_loopback.ancient_forge");

        //燃烧条动画帧
        burnFrames = new IDrawable[6];
        for (int i = 0; i < 6; i++) {
            int[] coords = BURN_FRAMES[i];
            burnFrames[i] = guiHelper.drawableBuilder(backgroundTexture,
                            coords[0], coords[1], BURN_WIDTH, BURN_HEIGHT)
                    .setTextureSize(256, 256)
                    .build();
        }

        //进度条动画帧
        craftFrames = new IDrawable[6];
        for (int i = 0; i < 6; i++) {
            int[] coords = CRAFT_FRAMES[i];
            craftFrames[i] = guiHelper.drawableBuilder(backgroundTexture,
                            coords[0], coords[1], CRAFT_WIDTH, CRAFT_HEIGHT)
                    .setTextureSize(256, 256)
                    .build();
        }
    }

    @Override
    public @NotNull RecipeType<AncientForgeRecipeDisplay> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AncientForgeRecipeDisplay recipe,
                          @NotNull IFocusGroup focuses) {

        AncientForgeRecipe forgeRecipe = recipe.getRecipe();

        //添加材料槽位（3x3）
        List<net.minecraft.world.item.crafting.Ingredient> materials = forgeRecipe.getMaterials();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                int x = MATERIAL_START_X + col * MATERIAL_SPACING;
                int y = MATERIAL_START_Y + row * MATERIAL_SPACING;

                if (index < materials.size() && !materials.get(index).isEmpty()) {
                    builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, x, y)
                            .addIngredients(materials.get(index));
                }
            }
        }

        //催化剂槽位
        if (!forgeRecipe.getCatalystData().getIngredient().isEmpty()) {
            builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, CATALYST_X, CATALYST_Y)
                    .addIngredients(forgeRecipe.getCatalystData().getIngredient())
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        if (forgeRecipe.getCatalystData().isRequired()) {
                            tooltip.add(Component.translatable("jei.tetra_loopback.catalyst.required"));
                        }
                        if (forgeRecipe.getCatalystData().getTimeReduction() > 0) {
                            float reduction = forgeRecipe.getCatalystData().getTimeReduction() * 100;
                            tooltip.add(Component.translatable("jei.tetra_loopback.catalyst.time_reduction",
                                    String.format("%.0f", reduction)));
                        }
                    });
        }

        //能量源槽位
        if (!forgeRecipe.getEnergySourceData().getIngredient().isEmpty()) {
            builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, ENERGY_X, ENERGY_Y)
                    .addIngredients(forgeRecipe.getEnergySourceData().getIngredient())
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        int energy = forgeRecipe.getEnergySourceData().getEnergyValue();
                        tooltip.add(Component.translatable("jei.tetra_loopback.energy.value", energy));

                        if (forgeRecipe.getEnergySourceData().acceptAnyFuel()) {
                            tooltip.add(Component.translatable("jei.tetra_loopback.energy.accept_any_fuel"));
                        }
                    });
        }

        //主输出
        List<ItemStack> mainOutputs = forgeRecipe.getMainOutputs();
        if (!mainOutputs.isEmpty()) {
            ItemStack output = mainOutputs.get(0).copy();

            if (forgeRecipe.getMainOutputMultiplier() > 1.0f) {
                int newCount = (int) Math.ceil(output.getCount() * forgeRecipe.getMainOutputMultiplier());
                output.setCount(Math.min(newCount, output.getMaxStackSize()));
            }

            builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, MAIN_OUTPUT_X, MAIN_OUTPUT_Y)
                    .addItemStack(output)
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        if (forgeRecipe.getMainOutputMultiplier() > 1.0f) {
                            float multiplier = forgeRecipe.getMainOutputMultiplier();
                            tooltip.add(Component.translatable("jei.tetra_loopback.output.multiplier",
                                    String.format("%.1f", multiplier)));
                        }
                    });
        }

        //副输出
        List<ItemStack> sideOutputs = forgeRecipe.getSideOutputs();
        if (!sideOutputs.isEmpty()) {
            ItemStack output = sideOutputs.get(0).copy();

            float finalMultiplier = forgeRecipe.getSideOutputMultiplier() / forgeRecipe.getMainToSideRatio();
            int newCount = (int) Math.ceil(output.getCount() * finalMultiplier);
            output.setCount(Math.min(newCount, output.getMaxStackSize()));

            builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, SIDE_OUTPUT_X, SIDE_OUTPUT_Y)
                    .addItemStack(output)
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        float chance = 1.0f / forgeRecipe.getMainToSideRatio();
                        tooltip.add(Component.translatable("jei.tetra_loopback.side_output.chance",
                                String.format("%.0f", chance * 100)));
                    });
        }

        //返还材料
        List<ItemStack> returnMaterials = forgeRecipe.getReturnMaterials();
        if (!returnMaterials.isEmpty() && forgeRecipe.getReturnChance() > 0) {
            ItemStack returnMat = returnMaterials.get(0).copy();

            builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, RETURN_X, RETURN_Y)
                    .addItemStack(returnMat)
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        float chance = forgeRecipe.getReturnChance() * 100;
                        tooltip.add(Component.translatable("jei.tetra_loopback.return.chance",
                                String.format("%.0f", chance)));
                    });
        }
    }

    @Override
    public void draw(AncientForgeRecipeDisplay recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphics gui, double mouseX, double mouseY) {

        //更新动画帧
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > FRAME_DURATION) {
            currentBurnFrame = (currentBurnFrame + 1) % 6;
            currentCraftFrame = (currentCraftFrame + 1) % 6;
            lastUpdateTime = currentTime;
        }

        //绘制燃烧条动画
        burnFrames[currentBurnFrame].draw(gui, BURN_X, BURN_Y);

        //绘制进度条动画
        craftFrames[currentCraftFrame].draw(gui, CRAFT_X, CRAFT_Y);

        //绘制配方信息（催化剂是否必需）
        if (recipe.getRecipe().getCatalystData().isRequired()) {
            Component requiredText = Component.translatable("jei.tetra_loopback.catalyst.required.short");
            int requiredWidth = Minecraft.getInstance().font.width(requiredText);
            gui.drawString(
                    Minecraft.getInstance().font,
                    requiredText,
                    CATALYST_X + 8 - (requiredWidth / 2),
                    CATALYST_Y - 12,
                    0xFFAA0000,
                    false
            );
        }
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull AncientForgeRecipeDisplay recipe,
                                                      @NotNull IRecipeSlotsView recipeSlotsView,
                                                      double mouseX, double mouseY) {

        List<Component> tooltips = new ArrayList<>();

        //检查是否悬停在燃烧条区域
        if (mouseX >= BURN_X && mouseX <= BURN_X + BURN_WIDTH &&
                mouseY >= BURN_Y && mouseY <= BURN_Y + BURN_HEIGHT) {

            int energyValue = recipe.getRecipe().getEnergySourceData().getEnergyValue();
            boolean acceptAnyFuel = recipe.getRecipe().getEnergySourceData().acceptAnyFuel();

            tooltips.add(Component.translatable("jei.tetra_loopback.burning_info"));
            tooltips.add(Component.translatable(acceptAnyFuel ?
                    "jei.tetra_loopback.fuel_type.any" : "jei.tetra_loopback.fuel_type.specific"));
            tooltips.add(Component.translatable("jei.tetra_loopback.burn_time", energyValue / 20));
        }

        //检查是否悬停在进度条区域
        if (mouseX >= CRAFT_X && mouseX <= CRAFT_X + CRAFT_WIDTH &&
                mouseY >= CRAFT_Y && mouseY <= CRAFT_Y + CRAFT_HEIGHT) {

            int processTime = recipe.getRecipe().getProcessTime();
            int seconds = processTime / 20;

            tooltips.add(Component.translatable("jei.tetra_loopback.process_info"));
            tooltips.add(Component.translatable("jei.tetra_loopback.process_time_seconds", seconds));

            //检查是否有催化剂减少时间
            if (recipe.getRecipe().getCatalystData().getTimeReduction() > 0) {
                float reduction = recipe.getRecipe().getCatalystData().getTimeReduction() * 100;
                int reducedTime = (int)(processTime * (1 - recipe.getRecipe().getCatalystData().getTimeReduction()));
                tooltips.add(Component.translatable("jei.tetra_loopback.with_catalyst", reducedTime / 20));
                tooltips.add(Component.translatable("jei.tetra_loopback.time_reduction",
                        String.format("%.0f", reduction)));
            }
        }

        return tooltips;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}