package com.tetra_loopback.block.ancientforge.entity;

import com.tetra_loopback.block.ancientforge.AncientForgeBlock;
import com.tetra_loopback.block.ancientforge.fuel.AncientForgeFuelCompat;
import com.tetra_loopback.block.ancientforge.inventory.AncientForgeMenu;
import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipe;
import com.tetra_loopback.block.ancientforge.recipe.AncientForgeRecipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
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
    private boolean catalystApplied = false; //标记当前合成是否应用了催化剂

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
        tag.putBoolean("catalystApplied", catalystApplied); //保存催化剂标记
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
        catalystApplied = tag.getBoolean("catalystApplied"); // 加载催化剂标记
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
                entity.resetCraftingState();
                changed = true;
            }
        }

        if (!entity.isBurning && hasRecipe && entity.canCraft()) {
            ItemStack energyItem = entity.itemHandler.getStackInSlot(ENERGY_SLOT);
            int burnTime = entity.getBurnTime(energyItem);

            if (burnTime > 0) {
                entity.startCrafting(burnTime);
                energyItem.shrink(1);
                changed = true;
            }
        }

        if (entity.isBurning && entity.hasValidRecipe) {
            if (!entity.hasValidRecipe()) {
                entity.resetCraftingState();
                changed = true;
            } else {
                entity.processCrafting(changed);
            }
        } else if (entity.craftTime > 0) {
            entity.resetCraftingState();
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


    private void resetCraftingState() {
        this.isBurning = false;
        this.craftTime = 0;
        this.hasConsumedIngredients = false;
        this.hasValidRecipe = false;
        this.isCraftingInProgress = false;
        this.wasRecipeValidBefore = false;
        this.catalystApplied = false; //重置催化剂标记
    }

    private void startCrafting(int burnTime) {
        this.isBurning = true;
        this.burnTime = burnTime;
        this.burnTimeTotal = burnTime;

        //在合成开始时计算并保存合成时间
        this.craftTimeTotal = this.getActualCraftTime();
        this.craftTime = 0;
        this.hasConsumedIngredients = false;
        this.hasValidRecipe = true;
        this.isCraftingInProgress = false;
        this.wasRecipeValidBefore = true;

        //标记是否应用了催化剂
        ItemStack catalyst = itemHandler.getStackInSlot(CATALYST_SLOT);
        if (!catalyst.isEmpty() && currentRecipe != null) {
            this.catalystApplied = currentRecipe.getCatalystData().isValidCatalyst(catalyst);
        }
    }

    private void processCrafting(boolean changed) {
        if (!this.hasConsumedIngredients && this.craftTime == 0) {
            this.consumeIngredients();
            this.hasConsumedIngredients = true;
            this.isCraftingInProgress = true;
            changed = true;
        }

        if (this.isCraftingInProgress) {
            this.craftTime++;

            //合成过程中不重新计算时间，保持开始时的craftTimeTotal
            if (this.craftTime >= this.craftTimeTotal) {
                this.craftItem();

                this.craftTime = 0;
                this.hasConsumedIngredients = false;
                this.isCraftingInProgress = false;
                this.catalystApplied = false; //重置催化剂标记

                if (!this.hasEnoughIngredients()) {
                    this.hasValidRecipe = false;
                }

                changed = true;
            }
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

        //只在合成开始时检查催化剂
        ItemStack catalyst = itemHandler.getStackInSlot(CATALYST_SLOT);
        boolean hasValidCatalyst = !catalyst.isEmpty() &&
                currentRecipe.getCatalystData().isValidCatalyst(catalyst);

        //计算基础时间
        float baseTime = currentRecipe.getProcessTime();

        //存在催化剂且配方设置时间减少，则应用
        if (hasValidCatalyst && currentRecipe.getCatalystData().getTimeReduction() > 0) {
            float reduction = currentRecipe.getCatalystData().getTimeReduction();
            return (int) (baseTime * (1.0f - reduction));
        }

        return (int) baseTime;
    }

    private boolean canCraft() {
        if (currentRecipe == null) return false;

        //检查催化剂是否必需
        if (currentRecipe.getCatalystData().isRequired()) {
            ItemStack catalyst = itemHandler.getStackInSlot(CATALYST_SLOT);
            if (!currentRecipe.getCatalystData().isValidCatalyst(catalyst)) {
                return false;
            }
        }

        //检查燃料
        ItemStack energyItem = itemHandler.getStackInSlot(ENERGY_SLOT);
        if (getBurnTime(energyItem) <= 0) {
            return false;
        }

        //检查材料
        if (!hasEnoughIngredients()) {
            return false;
        }

        //检查输出空间
        return hasEnoughSpace();
    }

    private int getBurnTime(ItemStack fuel) {
        //使用燃料兼容类
        return AncientForgeFuelCompat.getFuelValue(fuel, currentRecipe);
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

    public void dropContents() {
        if (level == null || level.isClientSide) return;

        //掉落所有物品槽中的物品
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                //创建物品实体并掉落
                ItemEntity itemEntity = new ItemEntity(level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5,
                        stack.copy());
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);

                //清空槽位
                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}