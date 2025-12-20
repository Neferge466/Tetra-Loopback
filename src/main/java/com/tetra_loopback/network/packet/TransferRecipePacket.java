package com.tetra_loopback.network.packet;

import com.tetra_loopback.block.ancientforge.entity.AncientForgeBlockEntity;
import com.tetra_loopback.block.ancientforge.inventory.AncientForgeMenu;
import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipe;
import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipeType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TransferRecipePacket {

    private final int containerId;
    private final String recipeId;
    private final Map<Integer, Integer> slotMapping;
    private final boolean maxTransfer;

    public TransferRecipePacket(int containerId, String recipeId, Map<Integer, Integer> slotMapping, boolean maxTransfer) {
        this.containerId = containerId;
        this.recipeId = recipeId;
        this.slotMapping = slotMapping;
        this.maxTransfer = maxTransfer;
    }

    public static void encode(TransferRecipePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.containerId);
        buffer.writeUtf(packet.recipeId);
        buffer.writeInt(packet.slotMapping.size());
        for (Map.Entry<Integer, Integer> entry : packet.slotMapping.entrySet()) {
            buffer.writeInt(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
        buffer.writeBoolean(packet.maxTransfer);
    }

    public static TransferRecipePacket decode(FriendlyByteBuf buffer) {
        int containerId = buffer.readInt();
        String recipeId = buffer.readUtf();
        int mappingSize = buffer.readInt();
        Map<Integer, Integer> slotMapping = new HashMap<>();
        for (int i = 0; i < mappingSize; i++) {
            int key = buffer.readInt();
            int value = buffer.readInt();
            slotMapping.put(key, value);
        }
        boolean maxTransfer = buffer.readBoolean();
        return new TransferRecipePacket(containerId, recipeId, slotMapping, maxTransfer);
    }

    public static void handle(TransferRecipePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            //获取容器
            AbstractContainerMenu menu = player.containerMenu;
            if (menu.containerId != packet.containerId) return;
            if (!(menu instanceof AncientForgeMenu)) return;

            //获取配方
            ResourceLocation recipeLocation = new ResourceLocation(packet.recipeId);
            AncientForgeRecipe recipe = player.level().getRecipeManager()
                    .getAllRecipesFor(AncientForgeRecipeType.INSTANCE)
                    .stream()
                    .filter(r -> r.getId().equals(recipeLocation))
                    .findFirst()
                    .orElse(null);

            if (recipe == null) return;

            //执行物品传输
            performRecipeTransfer((AncientForgeMenu) menu, player, recipe, packet.slotMapping, packet.maxTransfer);
        });
        context.setPacketHandled(true);
    }

    private static void performRecipeTransfer(AncientForgeMenu menu, ServerPlayer player,
                                              AncientForgeRecipe recipe, Map<Integer, Integer> slotMapping,
                                              boolean maxTransfer) {
        //清空相关槽位
        clearSlotsForTransfer(menu, player);

        //放置材料
        for (int recipeSlot = 0; recipeSlot < AncientForgeBlockEntity.MATERIAL_SLOTS; recipeSlot++) {
            if (recipeSlot < recipe.getMaterials().size()) {
                Ingredient ingredient = recipe.getMaterials().get(recipeSlot);
                if (!ingredient.isEmpty()) {
                    //从玩家物品栏查找匹配的物品
                    ItemStack found = findMatchingItem(player, ingredient);
                    if (!found.isEmpty()) {
                        int containerSlot = slotMapping.getOrDefault(recipeSlot, recipeSlot);
                        menu.getSlot(containerSlot).set(found.copyWithCount(1));
                        //从玩家物品栏中移除
                        removeItemFromInventory(player, found);
                    }
                }
            }
        }

        //放置催化剂
        if (!recipe.getCatalystData().getIngredient().isEmpty()) {
            Ingredient catalyst = recipe.getCatalystData().getIngredient();
            ItemStack found = findMatchingItem(player, catalyst);
            if (!found.isEmpty()) {
                int containerSlot = slotMapping.getOrDefault(AncientForgeBlockEntity.MATERIAL_SLOTS,
                        AncientForgeBlockEntity.CATALYST_SLOT);
                menu.getSlot(containerSlot).set(found.copyWithCount(1));
                removeItemFromInventory(player, found);
            }
        }

        //放置能量源
        if (!recipe.getEnergySourceData().getIngredient().isEmpty()) {
            Ingredient energy = recipe.getEnergySourceData().getIngredient();
            ItemStack found = findMatchingItem(player, energy);
            if (!found.isEmpty()) {
                int containerSlot = slotMapping.getOrDefault(AncientForgeBlockEntity.MATERIAL_SLOTS + 1,
                        AncientForgeBlockEntity.ENERGY_SLOT);
                menu.getSlot(containerSlot).set(found.copyWithCount(1));
                removeItemFromInventory(player, found);
            }
        }

        //广播容器更新
        menu.broadcastChanges();
    }

    private static void clearSlotsForTransfer(AncientForgeMenu menu, Player player) {
        //材料槽位
        for (int i = 0; i < AncientForgeBlockEntity.MATERIAL_SLOTS; i++) {
            clearSlot(menu, player, i);
        }
        clearSlot(menu, player, AncientForgeBlockEntity.CATALYST_SLOT);
        clearSlot(menu, player, AncientForgeBlockEntity.ENERGY_SLOT);
    }

    private static void clearSlot(AncientForgeMenu menu, Player player, int slotIndex) {
        ItemStack stack = menu.getSlot(slotIndex).getItem();
        if (!stack.isEmpty()) {
            //尝试将物品放回玩家物品栏
            if (!player.getInventory().add(stack)) {
                //如果物品栏已满，掉落物品
                player.drop(stack, false);
            }
            menu.getSlot(slotIndex).set(ItemStack.EMPTY);
        }
    }

    private static ItemStack findMatchingItem(Player player, Ingredient ingredient) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void removeItemFromInventory(Player player, ItemStack stackToRemove) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(stack, stackToRemove) && !stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
                break;
            }
        }
    }
}