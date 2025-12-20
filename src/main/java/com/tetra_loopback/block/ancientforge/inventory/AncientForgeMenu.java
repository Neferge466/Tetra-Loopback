package com.tetra_loopback.block.ancientforge.inventory;

import com.tetra_loopback.block.ancientforge.entity.AncientForgeBlockEntity;
import com.tetra_loopback.block.ancientforge.inventory.slot.CatalystSlot;
import com.tetra_loopback.block.ancientforge.inventory.slot.EnergySourceSlot;
import com.tetra_loopback.block.ancientforge.inventory.slot.OutputSlot;
import com.tetra_loopback.block.ancientforge.inventory.slot.ReturnSlot;
import com.tetra_loopback.block.ancientforge.inventory.slot.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class AncientForgeMenu extends AbstractContainerMenu {

    private final AncientForgeBlockEntity blockEntity;
    private final IItemHandler playerInventory;
    private final ContainerData data;

    public AncientForgeMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory,
                (AncientForgeBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public AncientForgeMenu(int id, Inventory playerInventory, AncientForgeBlockEntity blockEntity) {
        this(id, playerInventory, blockEntity, new SimpleContainerData(4));
    }

    public AncientForgeMenu(int id, Inventory playerInventory, AncientForgeBlockEntity blockEntity, ContainerData data) {
        super(TLbMenus.ANCIENT_FORGE_MENU.get(), id);
        this.blockEntity = blockEntity;
        this.playerInventory = new InvWrapper(playerInventory);
        this.data = data;

        addCustomSlots();
        addPlayerInventory(playerInventory);
        addDataSlots(data);
    }

    public ContainerData getData() {
        return this.data;
    }

    public int getBurnTime() {
        return this.data.get(0);
    }

    public int getBurnTimeTotal() {
        return this.data.get(1);
    }

    public int getCraftTime() {
        return this.data.get(2);
    }

    public int getCraftTimeTotal() {
        return this.data.get(3);
    }

    private void addCustomSlots() {
        IItemHandler handler = blockEntity.getItemHandler();

        //原料槽 (3x3)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new SlotItemHandler(handler, row * 3 + col, 12 + col * 17, 15 + row * 17));
            }
        }

        //催化剂槽
        addSlot(new CatalystSlot(handler, AncientForgeBlockEntity.CATALYST_SLOT, 76, 15));

        //能量源槽
        addSlot(new EnergySourceSlot(handler, AncientForgeBlockEntity.ENERGY_SLOT, 88, 80));

        //主产物槽
        int mainOutputStart = AncientForgeBlockEntity.MATERIAL_SLOTS + 2;
        addSlot(new OutputSlot(handler, mainOutputStart, 136, 15));

        //副产物槽
        int sideOutputStart = mainOutputStart + AncientForgeBlockEntity.MAIN_OUTPUT_SLOTS;
        addSlot(new OutputSlot(handler, sideOutputStart, 136, 60));

        //返还原料槽
        int returnStart = sideOutputStart + AncientForgeBlockEntity.SIDE_OUTPUT_SLOTS;
        addSlot(new ReturnSlot(handler, returnStart, 12, 72));
    }

    private void addPlayerInventory(Inventory playerInventory) {
        //主物品栏
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 12 + col * 17, 129 + row * 17));
            }
        }

        //快捷栏
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 12 + col * 17, 183));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < AncientForgeBlockEntity.TOTAL_SLOTS) {
                if (!this.moveItemStackTo(itemstack1, AncientForgeBlockEntity.TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, AncientForgeBlockEntity.TOTAL_SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    public AncientForgeBlockEntity getBlockEntity() {
        return blockEntity;
    }

    private static class SimpleContainerData implements ContainerData {
        private final int[] data;

        public SimpleContainerData(int size) {
            this.data = new int[size];
        }

        @Override
        public int get(int index) {
            return data[index];
        }

        @Override
        public void set(int index, int value) {
            data[index] = value;
        }

        @Override
        public int getCount() {
            return data.length;
        }
    }

    public void broadcastChanges() {
        this.broadcastFullState();
    }

}