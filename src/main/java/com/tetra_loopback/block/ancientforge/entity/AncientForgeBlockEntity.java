package com.tetra_loopback.block.ancientforge.entity;

import com.tetra_loopback.block.ancientforge.AncientForgeBlock;
import com.tetra_loopback.block.ancientforge.inventory.AncientForgeMenu;
import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipe;
import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AncientForgeBlockEntity extends BlockEntity implements MenuProvider {

    public static final int MATERIAL_SLOTS = 9;
    public static final int CATALYST_SLOT = 9;
    public static final int ENERGY_SLOT = 10;
    public static final int MAIN_OUTPUT_SLOTS = 1;
    public static final int SIDE_OUTPUT_SLOTS = 1;
    public static final int RETURN_SLOTS = 1;

    public static final int TOTAL_SLOTS = MATERIAL_SLOTS + 1 + 1 + MAIN_OUTPUT_SLOTS + SIDE_OUTPUT_SLOTS + RETURN_SLOTS;

    private int burnTime;
    private int burnTimeTotal;
    private int craftTime;
    private int craftTimeTotal;
    private AncientForgeRecipe currentRecipe;
    private boolean needsRecipeUpdate = true;
    private boolean isBurning = false;
    private boolean hasConsumedIngredients = false;
    private boolean hasValidRecipe = false;
    private boolean isCraftingInProgress = false;
    private boolean wasRecipeValidBefore = false;

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            if (slot < MATERIAL_SLOTS && isBurning && !isCraftingInProgress && !hasValidRecipe) {
                if (hasValidRecipe()) {
                    hasValidRecipe = true;
                    craftTime = 0;
                    hasConsumedIngredients = false;
                }
            }
            if (slot < MATERIAL_SLOTS + 2 && !isBurning) {
                needsRecipeUpdate = true;
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> burnTimeTotal;
                case 2 -> craftTime;
                case 3 -> craftTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> burnTimeTotal = value;
                case 2 -> craftTime = value;
                case 3 -> craftTimeTotal = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public AncientForgeBlockEntity(BlockPos pos, BlockState state) {
        super(TLbBlockEntities.ANCIENT_FORGE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tetra_loopback.ancient_forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AncientForgeMenu(id, inventory, this, this.data);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("burnTime", burnTime);
        tag.putInt("burnTimeTotal", burnTimeTotal);
        tag.putInt("craftTime", craftTime);
        tag.putInt("craftTimeTotal", craftTimeTotal);
        tag.putBoolean("isBurning", isBurning);
        tag.putBoolean("hasConsumedIngredients", hasConsumedIngredients);
        tag.putBoolean("hasValidRecipe", hasValidRecipe);
        tag.putBoolean("isCraftingInProgress", isCraftingInProgress);
        tag.putBoolean("wasRecipeValidBefore", wasRecipeValidBefore);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        burnTime = tag.getInt("burnTime");
        burnTimeTotal = tag.getInt("burnTimeTotal");
        craftTime = tag.getInt("craftTime");
        craftTimeTotal = tag.getInt("craftTimeTotal");
        isBurning = tag.getBoolean("isBurning");
        hasConsumedIngredients = tag.getBoolean("hasConsumedIngredients");
        hasValidRecipe = tag.getBoolean("hasValidRecipe");
        isCraftingInProgress = tag.getBoolean("isCraftingInProgress");
        wasRecipeValidBefore = tag.getBoolean("wasRecipeValidBefore");
    }

    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D,
                (double) this.worldPosition.getY() + 0.5D,
                (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AncientForgeBlockEntity entity) {
        if (level.isClientSide) return;

        if (entity.needsRecipeUpdate && !entity.isBurning) {
            entity.updateCurrentRecipe();
            entity.needsRecipeUpdate = false;
        }

        boolean hasRecipe = entity.currentRecipe != null;
        boolean changed = false;

        if (entity.isBurning) {
            entity.burnTime--;
            changed = true;

            if (entity.burnTime <= 0) {
                entity.isBurning = false;
                entity.craftTime = 0;
                entity.hasConsumedIngredients = false;
                entity.hasValidRecipe = false;
                entity.isCraftingInProgress = false;
                entity.wasRecipeValidBefore = false;
                changed = true;
            }
        }

        if (!entity.isBurning && hasRecipe && entity.canCraft()) {
            ItemStack energyItem = entity.itemHandler.getStackInSlot(ENERGY_SLOT);
            int burnTime = entity.getBurnTime(energyItem);

            if (burnTime > 0) {
                entity.isBurning = true;
                entity.burnTime = burnTime;
                entity.burnTimeTotal = burnTime;

                entity.craftTimeTotal = entity.getActualCraftTime();
                entity.craftTime = 0;
                entity.hasConsumedIngredients = false;
                entity.hasValidRecipe = true;
                entity.isCraftingInProgress = false;
                entity.wasRecipeValidBefore = true;

                energyItem.shrink(1);
                changed = true;
            }
        }

        if (entity.isBurning && entity.hasValidRecipe) {
            if (!entity.hasValidRecipe()) {
                entity.craftTime = 0;
                entity.hasConsumedIngredients = false;
                entity.hasValidRecipe = false;
                entity.isCraftingInProgress = false;
                changed = true;
            } else {
                if (!entity.hasConsumedIngredients && entity.craftTime == 0) {
                    entity.consumeIngredients();
                    entity.hasConsumedIngredients = true;
                    entity.isCraftingInProgress = true;
                    changed = true;
                }
                if (entity.isCraftingInProgress) {
                    entity.craftTime++;

                    int actualCraftTime = entity.craftTimeTotal;

                    if (entity.craftTime >= actualCraftTime) {
                        entity.craftItem();

                        entity.craftTime = 0;
                        entity.hasConsumedIngredients = false;
                        entity.isCraftingInProgress = false;

                        if (!entity.hasEnoughIngredients()) {
                            entity.hasValidRecipe = false;
                        }

                        changed = true;
                    }
                }
            }
        } else if (entity.craftTime > 0) {
            entity.craftTime = 0;
            entity.hasConsumedIngredients = false;
            entity.hasValidRecipe = false;
            entity.isCraftingInProgress = false;
            changed = true;
        }

        boolean wasLit = state.getValue(AncientForgeBlock.LIT);
        boolean shouldBeLit = entity.isBurning;
        

        if (wasLit != shouldBeLit) {
            //更新方块状态
            BlockState newState = state.setValue(AncientForgeBlock.LIT, shouldBeLit);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
            entity.setChanged();
        }

        if (changed) {
            entity.setChanged();
        }
    }
    private boolean hasValidRecipe() {
        if (currentRecipe == null) return false;
        if (isCraftingInProgress) {
            return true;
        }
        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            Ingredient required = currentRecipe.getMaterials().get(i);

            if (!required.test(stack)) {
                return false;
            }
        }

        return true;
    }

    private void updateCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Optional<AncientForgeRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(AncientForgeRecipeType.INSTANCE, inventory, level);

        if (recipe.isPresent()) {
            currentRecipe = recipe.get();
            craftTimeTotal = currentRecipe.getProcessTime();
            hasValidRecipe = true;
        } else {
            currentRecipe = null;
            craftTime = 0;
            craftTimeTotal = 0;
            hasValidRecipe = false;
        }
    }

    private int getActualCraftTime() {
        if (currentRecipe == null) return 0;

        ItemStack catalyst = itemHandler.getStackInSlot(CATALYST_SLOT);
        boolean hasCatalyst = !catalyst.isEmpty() && currentRecipe.getCatalyst().test(catalyst);

        float timeMultiplier = hasCatalyst ? (1.0f - currentRecipe.getTimeReduction()) : 1.0f;
        return (int) (currentRecipe.getProcessTime() * timeMultiplier);
    }

    private boolean canCraft() {
        if (currentRecipe == null) return false;

        if (!hasEnoughIngredients()) {
            return false;
        }

        return hasEnoughSpace();
    }

    private boolean hasEnoughIngredients() {
        if (currentRecipe == null) return false;

        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            Ingredient required = currentRecipe.getMaterials().get(i);
            if (!required.test(stack) || stack.getCount() < 1) {
                return false;
            }
        }

        return true;
    }

    private boolean hasEnoughSpace() {
        if (currentRecipe == null) return false;

        if (currentRecipe.getMainOutputs().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2;
            ItemStack output = currentRecipe.getMainOutputs().get(0).copy();


            if (currentRecipe.getMainOutputMultiplier() > 1.0f) {
                int newCount = (int) Math.ceil(output.getCount() * currentRecipe.getMainOutputMultiplier());
                output.setCount(Math.min(newCount, output.getMaxStackSize()));
            }

            ItemStack remaining = itemHandler.insertItem(slotIndex, output, true);
            if (!remaining.isEmpty()) {
                return false;
            }
        }

        if (currentRecipe.getSideOutputs().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS; // 副产物槽索引
            ItemStack output = currentRecipe.getSideOutputs().get(0).copy();

            float finalMultiplier = currentRecipe.getSideOutputMultiplier() / currentRecipe.getMainToSideRatio();
            int newCount = (int) Math.ceil(output.getCount() * finalMultiplier);
            output.setCount(Math.min(newCount, output.getMaxStackSize()));

            ItemStack remaining = itemHandler.insertItem(slotIndex, output, true);
            if (!remaining.isEmpty()) {
                return false;
            }
        }

        if (currentRecipe.getReturnChance() > 0 && currentRecipe.getReturnMaterials().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS + SIDE_OUTPUT_SLOTS; // 返还材料槽索引
            ItemStack returnMat = currentRecipe.getReturnMaterials().get(0).copy();

            ItemStack remaining = itemHandler.insertItem(slotIndex, returnMat, true);
            if (!remaining.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private void craftItem() {
        if (currentRecipe == null || !hasEnoughSpace()) return;

        placeOutputs();

        placeReturnMaterials();

        needsRecipeUpdate = true;
    }

    private void consumeIngredients() {
        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            Ingredient required = currentRecipe.getMaterials().get(i);

            if (!stack.isEmpty() && required.test(stack) && stack.getCount() >= 1) {
                stack.shrink(1);
            }
        }
    }

    private void placeOutputs() {
        if (currentRecipe.getMainOutputs().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2;
            ItemStack output = currentRecipe.getMainOutputs().get(0).copy();

            if (currentRecipe.getMainOutputMultiplier() > 1.0f) {
                int newCount = (int) Math.ceil(output.getCount() * currentRecipe.getMainOutputMultiplier());
                output.setCount(Math.min(newCount, output.getMaxStackSize()));
            }

            ItemStack remaining = itemHandler.insertItem(slotIndex, output, false);
            if (!remaining.isEmpty()) {
                System.out.println("无法插入物品到主产物槽" + slotIndex);
            }
        }

        if (currentRecipe.getSideOutputs().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS; // 副产物槽索引
            ItemStack output = currentRecipe.getSideOutputs().get(0).copy();

            float finalMultiplier = currentRecipe.getSideOutputMultiplier() / currentRecipe.getMainToSideRatio();
            int newCount = (int) Math.ceil(output.getCount() * finalMultiplier);
            output.setCount(Math.min(newCount, output.getMaxStackSize()));

            ItemStack remaining = itemHandler.insertItem(slotIndex, output, false);
            if (!remaining.isEmpty()) {
                System.out.println("无法插入副产物到槽" + slotIndex);
            }
        }
    }

    private void placeReturnMaterials() {
        if (currentRecipe.getReturnChance() > 0 && currentRecipe.getReturnMaterials().size() > 0 &&
                level.random.nextFloat() <= currentRecipe.getReturnChance()) {
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS + SIDE_OUTPUT_SLOTS; //返还材料槽索引
            ItemStack returnMat = currentRecipe.getReturnMaterials().get(0).copy();
            itemHandler.insertItem(slotIndex, returnMat, false);
        }
    }

    private int getBurnTime(ItemStack fuel) {
        if (currentRecipe == null || fuel.isEmpty() || !currentRecipe.getEnergySource().test(fuel)) {
            return 0;
        }
        return currentRecipe.getEnergyValue();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}