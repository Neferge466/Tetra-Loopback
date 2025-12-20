package com.tetra_loopback.integration.jei;

import com.tetra_loopback.block.ancientforge.entity.AncientForgeBlockEntity;
import com.tetra_loopback.block.ancientforge.inventory.AncientForgeMenu;
import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipe;
import com.tetra_loopback.network.TLbNetwork;
import com.tetra_loopback.network.packet.TransferRecipePacket;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AncientForgeRecipeTransferHandler implements IRecipeTransferHandler<AncientForgeMenu, AncientForgeRecipeDisplay> {

    private final IRecipeTransferHandlerHelper transferHelper;

    public AncientForgeRecipeTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public @NotNull Class<AncientForgeMenu> getContainerClass() {
        return AncientForgeMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<AncientForgeMenu>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public @NotNull RecipeType<AncientForgeRecipeDisplay> getRecipeType() {
        return AncientForgeRecipeCategory.RECIPE_TYPE;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@NotNull AncientForgeMenu menu,
                                               @NotNull AncientForgeRecipeDisplay recipe,
                                               @NotNull IRecipeSlotsView recipeSlotsView,
                                               @NotNull Player player,
                                               boolean maxTransfer,
                                               boolean doTransfer) {

        //检查是否可以传输
        TransferCheckResult checkResult = checkRecipeTransfer(menu, recipe, player);

        if (!checkResult.canTransfer()) {
            return transferHelper.createUserErrorWithTooltip(checkResult.errorMessage());
        }

        //如果只检查，返回null表示可以传输
        if (!doTransfer) {
            return null;
        }

        //实际传输，发送数据包到服务器
        try {
            //准备传输数据
            Map<Integer, Integer> slotMap = new HashMap<>();

            //材料槽位映射
            for (int i = 0; i < AncientForgeBlockEntity.MATERIAL_SLOTS; i++) {
                slotMap.put(i, i); // GUI槽位索引--容器槽位索引
            }

            slotMap.put(AncientForgeBlockEntity.MATERIAL_SLOTS, AncientForgeBlockEntity.CATALYST_SLOT);
            slotMap.put(AncientForgeBlockEntity.MATERIAL_SLOTS + 1, AncientForgeBlockEntity.ENERGY_SLOT);

            //获取配方ID
            String recipeId = recipe.getRecipe().getId().toString();
            //发送数据包到服务器
            if (TLbNetwork.CHANNEL != null) {
                TLbNetwork.CHANNEL.sendToServer(new TransferRecipePacket(
                        menu.containerId,
                        recipeId,
                        slotMap,
                        maxTransfer
                ));
            }

            return null;
        } catch (Exception e) {
            return transferHelper.createUserErrorWithTooltip(
                    net.minecraft.network.chat.Component.translatable("jei.tetra_loopback.transfer.error.failed")
            );
        }
    }

    private TransferCheckResult checkRecipeTransfer(AncientForgeMenu menu, AncientForgeRecipeDisplay recipe, Player player) {
        AncientForgeRecipe forgeRecipe = recipe.getRecipe();

        //检查材料槽位
        for (int i = 0; i < AncientForgeBlockEntity.MATERIAL_SLOTS; i++) {
            if (i < forgeRecipe.getMaterials().size()) {
                Ingredient ingredient = forgeRecipe.getMaterials().get(i);
                if (!ingredient.isEmpty()) {
                    // 检查玩家物品栏中是否有匹配的材料
                    if (!hasMatchingItemInInventory(player, ingredient)) {
                        return TransferCheckResult.error(
                                net.minecraft.network.chat.Component.translatable("jei.tetra_loopback.transfer.error.missing_ingredient")
                        );
                    }
                }
            }
        }

        //检查催化剂（必需）
        if (forgeRecipe.getCatalystData().isRequired() &&
                !forgeRecipe.getCatalystData().getIngredient().isEmpty()) {
            if (!hasMatchingItemInInventory(player, forgeRecipe.getCatalystData().getIngredient())) {
                return TransferCheckResult.error(
                        net.minecraft.network.chat.Component.translatable("jei.tetra_loopback.transfer.error.missing_catalyst")
                );
            }
        }

        //检查能量源（非接受任何燃料）
        if (!forgeRecipe.getEnergySourceData().acceptAnyFuel() &&
                !forgeRecipe.getEnergySourceData().getIngredient().isEmpty()) {
            if (!hasMatchingItemInInventory(player, forgeRecipe.getEnergySourceData().getIngredient())) {
                return TransferCheckResult.error(
                        net.minecraft.network.chat.Component.translatable("jei.tetra_loopback.transfer.error.missing_energy")
                );
            }
        }

        //检查输出槽位是否有空间
        if (!hasSpaceForOutputs(menu, recipe)) {
            return TransferCheckResult.error(
                    net.minecraft.network.chat.Component.translatable("jei.tetra_loopback.transfer.error.no_space")
            );
        }

        return TransferCheckResult.success();
    }

    private boolean hasMatchingItemInInventory(Player player, Ingredient ingredient) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpaceForOutputs(AncientForgeMenu menu, AncientForgeRecipeDisplay recipe) {
        AncientForgeRecipe forgeRecipe = recipe.getRecipe();

        //检查主产物槽位
        if (!forgeRecipe.getMainOutputs().isEmpty()) {
            ItemStack output = forgeRecipe.getMainOutputs().get(0).copy();
            if (forgeRecipe.getMainOutputMultiplier() > 1.0f) {
                int newCount = (int) Math.ceil(output.getCount() * forgeRecipe.getMainOutputMultiplier());
                output.setCount(Math.min(newCount, output.getMaxStackSize()));
            }

            //检查主输出槽位
            int mainOutputSlot = AncientForgeBlockEntity.MATERIAL_SLOTS + 2;
            ItemStack currentStack = menu.getSlot(mainOutputSlot).getItem();
            if (!canMergeStacks(currentStack, output)) {
                return false;
            }
        }

        //检查副产物槽位
        if (!forgeRecipe.getSideOutputs().isEmpty()) {
            ItemStack output = forgeRecipe.getSideOutputs().get(0).copy();
            float finalMultiplier = forgeRecipe.getSideOutputMultiplier() / forgeRecipe.getMainToSideRatio();
            int newCount = (int) Math.ceil(output.getCount() * finalMultiplier);
            output.setCount(Math.min(newCount, output.getMaxStackSize()));

            //检查副输出槽位
            int sideOutputSlot = AncientForgeBlockEntity.MATERIAL_SLOTS + 3;
            ItemStack currentStack = menu.getSlot(sideOutputSlot).getItem();
            if (!canMergeStacks(currentStack, output)) {
                return false;
            }
        }

        //检查返还材料槽位
        if (!forgeRecipe.getReturnMaterials().isEmpty() && forgeRecipe.getReturnChance() > 0) {
            ItemStack returnMat = forgeRecipe.getReturnMaterials().get(0).copy();
            int returnSlot = AncientForgeBlockEntity.MATERIAL_SLOTS + 4;
            ItemStack currentStack = menu.getSlot(returnSlot).getItem();
            if (!canMergeStacks(currentStack, returnMat)) {
                return false;
            }
        }

        return true;
    }

    private boolean canMergeStacks(ItemStack currentStack, ItemStack newStack) {
        if (currentStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameTags(currentStack, newStack)) {
            return false;
        }
        return currentStack.getCount() + newStack.getCount() <= currentStack.getMaxStackSize();
    }

    private record TransferCheckResult(boolean canTransfer, @Nullable net.minecraft.network.chat.Component errorMessage) {
        public static TransferCheckResult success() {
            return new TransferCheckResult(true, null);
        }

        public static TransferCheckResult error(net.minecraft.network.chat.Component errorMessage) {
            return new TransferCheckResult(false, errorMessage);
        }
    }
}