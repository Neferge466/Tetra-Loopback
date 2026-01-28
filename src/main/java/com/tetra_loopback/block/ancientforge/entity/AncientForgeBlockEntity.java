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
import net.minecraft.resources.ResourceLocation;
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
    private ResourceLocation lastRecipeId;
    private boolean needsRecipeUpdate = true;
    private boolean isBurning = false;
    private boolean hasValidRecipe = false;
    private boolean isCraftingInProgress = false;
    private boolean catalystApplied = false;

    //燃料信息
    private ItemStack lastFuelStack = ItemStack.EMPTY;
    private int lastFuelBurnTime = 0;

    private boolean needsMaterialCheck = true;

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            //材料槽位变化时更新配方
            if (slot < MATERIAL_SLOTS + 2) { //材料，催化剂，燃料
                needsRecipeUpdate = true;
                //材料变化时重新检查
                needsMaterialCheck = true;
            }

            if (isCraftingInProgress && slot < MATERIAL_SLOTS) {
                if (!checkRecipeValid(currentRecipe)) {
                    stopCrafting();
                }
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

        if (level != null && !level.isClientSide && lastRecipeId != null) {
            Optional<AncientForgeRecipe> recipe = level.getRecipeManager().getAllRecipesFor(AncientForgeRecipeType.INSTANCE)
                    .stream()
                    .filter(r -> r.getId().equals(lastRecipeId))
                    .findFirst();

            if (recipe.isPresent()) {
                currentRecipe = recipe.get();
                hasValidRecipe = checkRecipeValid(currentRecipe);
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("burnTime", burnTime);
        tag.putInt("burnTimeTotal", burnTimeTotal);
        tag.putInt("craftTime", craftTime);
        tag.putInt("craftTimeTotal", craftTimeTotal);
        tag.putBoolean("isBurning", isBurning);
        tag.putBoolean("hasValidRecipe", hasValidRecipe);
        tag.putBoolean("isCraftingInProgress", isCraftingInProgress);
        tag.putBoolean("catalystApplied", catalystApplied);
        tag.putBoolean("needsMaterialCheck", needsMaterialCheck);

        //保存燃料信息
        if (!lastFuelStack.isEmpty()) {
            CompoundTag fuelTag = new CompoundTag();
            lastFuelStack.save(fuelTag);
            tag.put("lastFuel", fuelTag);
        }
        tag.putInt("lastFuelBurnTime", lastFuelBurnTime);

        //保存当前配方
        if (currentRecipe != null) {
            tag.putString("recipeId", currentRecipe.getId().toString());
        } else if (lastRecipeId != null) {
            tag.putString("lastRecipeId", lastRecipeId.toString());
        }
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
        hasValidRecipe = tag.getBoolean("hasValidRecipe");
        isCraftingInProgress = tag.getBoolean("isCraftingInProgress");
        catalystApplied = tag.getBoolean("catalystApplied");
        needsMaterialCheck = tag.getBoolean("needsMaterialCheck");

        //恢复燃料信息
        if (tag.contains("lastFuel")) {
            lastFuelStack = ItemStack.of(tag.getCompound("lastFuel"));
        }
        lastFuelBurnTime = tag.getInt("lastFuelBurnTime");

        //恢复配方
        if (tag.contains("recipeId")) {
            lastRecipeId = new ResourceLocation(tag.getString("recipeId"));
        } else if (tag.contains("lastRecipeId")) {
            lastRecipeId = new ResourceLocation(tag.getString("lastRecipeId"));
        }

        if (isBurning && burnTime <= 0) {
            stopBurning();
        }

        if (isCraftingInProgress && !hasValidRecipe) {
            stopCrafting();
        }
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

        boolean changed = false;
        boolean wasBurning = entity.isBurning;

        if (entity.needsRecipeUpdate) {
            entity.updateCurrentRecipe();
            entity.needsRecipeUpdate = false;
            entity.needsMaterialCheck = true;
        }

        if (entity.isBurning) {
            entity.burnTime--;
            changed = true;

            if (entity.burnTime <= 0) {
                entity.stopBurning();
                changed = true;
            }
        }

        if (!entity.isBurning && entity.currentRecipe != null && entity.hasValidRecipe) {
            ItemStack fuelStack = entity.itemHandler.getStackInSlot(ENERGY_SLOT);
            int burnTime = entity.getBurnTime(fuelStack, entity.currentRecipe);

            if (burnTime > 0) {
                entity.startBurning(fuelStack, burnTime);
                fuelStack.shrink(1);
                changed = true;
            }
        }

        if (entity.isBurning && entity.hasValidRecipe && entity.currentRecipe != null) {
            if (entity.needsMaterialCheck) {
                boolean hasMaterials = entity.hasEnoughMaterials();
                if (!hasMaterials) {
                    entity.hasValidRecipe = false;
                    entity.stopCrafting();
                    entity.needsMaterialCheck = false;
                    changed = true;
                } else {
                    entity.needsMaterialCheck = false;
                }
            }

            if (!entity.isCraftingInProgress && entity.hasValidRecipe) {
                if (entity.hasEnoughSpace()) {
                    entity.craftTimeTotal = entity.getActualCraftTime();
                    entity.craftTime = 0;
                    entity.isCraftingInProgress = true;
                    ItemStack catalyst = entity.itemHandler.getStackInSlot(CATALYST_SLOT);
                    entity.catalystApplied = !catalyst.isEmpty() &&
                            entity.currentRecipe.getCatalystData().isValidCatalyst(catalyst);

                    changed = true;
                }
            } else if (entity.isCraftingInProgress) {
                entity.craftTime++;
                changed = true;

                if (entity.craftTime >= entity.craftTimeTotal) {
                    if (entity.hasEnoughMaterials() && entity.hasEnoughSpace()) {
                        entity.finishCrafting();
                        changed = true;

                        entity.needsMaterialCheck = true;

                        if (!entity.hasEnoughMaterials()) {
                            entity.hasValidRecipe = false;
                            entity.stopCrafting();
                        }
                    } else {
                        entity.hasValidRecipe = false;
                        entity.stopCrafting();
                        changed = true;
                    }
                }
            }
        } else if (entity.isCraftingInProgress) {
            entity.stopCrafting();
            changed = true;
        }

        boolean isLit = entity.isBurning && entity.burnTime > 0;
        boolean wasLit = state.getValue(AncientForgeBlock.LIT);

        if (isLit != wasLit) {
            level.setBlock(pos, state.setValue(AncientForgeBlock.LIT, isLit), Block.UPDATE_ALL);
            changed = true;
        }

        if (changed) {
            entity.setChanged();
        }
    }

    private void stopBurning() {
        this.isBurning = false;
        this.burnTime = 0;
        this.burnTimeTotal = 0;
        this.lastFuelStack = ItemStack.EMPTY;
        this.lastFuelBurnTime = 0;

        stopCrafting();
    }

    private void stopCrafting() {
        this.isCraftingInProgress = false;
        this.craftTime = 0;
        this.craftTimeTotal = 0;
        this.catalystApplied = false;
        this.needsMaterialCheck = true;
    }

    private void startBurning(ItemStack fuelStack, int burnTime) {
        this.isBurning = true;
        this.burnTime = burnTime;
        this.burnTimeTotal = burnTime;
        this.lastFuelStack = fuelStack.copy();
        this.lastFuelStack.setCount(1);
        this.lastFuelBurnTime = burnTime;
    }

    private void finishCrafting() {
        if (currentRecipe == null) return;

        //合成完成时消耗材料
        consumeIngredients();

        //放置产出
        placeOutputs();
        placeReturnMaterials();
        //重置合成状态，但保持燃烧
        stopCrafting();
        //记录最后一个成功的配方ID
        lastRecipeId = currentRecipe.getId();
    }

    private void updateCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Optional<AncientForgeRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(AncientForgeRecipeType.INSTANCE, inventory, level);

        if (recipe.isPresent()) {
            AncientForgeRecipe newRecipe = recipe.get();

            //检查新配方是否可以使用当前燃料
            if (isBurning) {
                ItemStack currentFuel = itemHandler.getStackInSlot(ENERGY_SLOT);
                int newBurnTime = getBurnTime(currentFuel, newRecipe);
                if (newBurnTime <= 0 && !isSameFuelType(newRecipe)) {
                    hasValidRecipe = false;
                    return;
                }
            }

            currentRecipe = newRecipe;
            hasValidRecipe = checkRecipeValid(newRecipe);
            craftTimeTotal = newRecipe.getProcessTime();
        } else {
            currentRecipe = null;
            hasValidRecipe = false;
            craftTime = 0;
            craftTimeTotal = 0;
        }

        needsMaterialCheck = true;
    }

    private boolean isSameFuelType(AncientForgeRecipe recipe) {
        if (currentRecipe == null || recipe == null) return false;

        //比较能量源
        AncientForgeRecipe.EnergySourceData currentEnergy = currentRecipe.getEnergySourceData();
        AncientForgeRecipe.EnergySourceData newEnergy = recipe.getEnergySourceData();

        //如果都接受任何燃料，则视为相同
        if (currentEnergy.acceptAnyFuel() && newEnergy.acceptAnyFuel()) {
            return true;
        }

        return currentEnergy.getIngredient().equals(newEnergy.getIngredient());
    }

    private boolean checkRecipeValid(AncientForgeRecipe recipe) {
        if (recipe == null) return false;

        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            Ingredient required = recipe.getMaterials().get(i);

            if (!required.test(stack) || stack.getCount() < 1) {
                return false;
            }
        }

        if (recipe.getCatalystData().isRequired()) {
            ItemStack catalyst = itemHandler.getStackInSlot(CATALYST_SLOT);
            if (!recipe.getCatalystData().isValidCatalyst(catalyst)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasEnoughMaterials() {
        if (currentRecipe == null) return false;

        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            Ingredient required = currentRecipe.getMaterials().get(i);

            if (!required.isEmpty() && !required.test(stack)) {
                return false;
            }

            if (!required.test(stack) || stack.getCount() < 1) {
                return false;
            }
        }

        return true;
    }

    private int getBurnTime(ItemStack fuel, AncientForgeRecipe recipe) {
        //燃料兼容
        return AncientForgeFuelCompat.getFuelValue(fuel, recipe);
    }

    private int getActualCraftTime() {
        if (currentRecipe == null) return 0;

        float baseTime = currentRecipe.getProcessTime();

        ItemStack catalyst = itemHandler.getStackInSlot(CATALYST_SLOT);
        boolean hasValidCatalyst = !catalyst.isEmpty() &&
                currentRecipe.getCatalystData().isValidCatalyst(catalyst);

        if (hasValidCatalyst && currentRecipe.getCatalystData().getTimeReduction() > 0) {
            float reduction = currentRecipe.getCatalystData().getTimeReduction();
            return (int) (baseTime * (1.0f - reduction));
        }

        return (int) baseTime;
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
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS;
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
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS + SIDE_OUTPUT_SLOTS;
            ItemStack returnMat = currentRecipe.getReturnMaterials().get(0).copy();

            ItemStack remaining = itemHandler.insertItem(slotIndex, returnMat, true);
            if (!remaining.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private void consumeIngredients() {
        if (currentRecipe == null) return;

        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            Ingredient required = currentRecipe.getMaterials().get(i);

            if (!stack.isEmpty() && required.test(stack) && stack.getCount() >= 1) {
                stack.shrink(1);
            }
        }
    }

    private void placeOutputs() {
        if (currentRecipe == null) return;

        //主产物
        if (currentRecipe.getMainOutputs().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2;
            ItemStack output = currentRecipe.getMainOutputs().get(0).copy();

            if (currentRecipe.getMainOutputMultiplier() > 1.0f) {
                int newCount = (int) Math.ceil(output.getCount() * currentRecipe.getMainOutputMultiplier());
                output.setCount(Math.min(newCount, output.getMaxStackSize()));
            }

            ItemStack remaining = itemHandler.insertItem(slotIndex, output, false);
            if (!remaining.isEmpty()) {
                //无法插入，掉落在地上
                if (level != null) {
                    ItemEntity itemEntity = new ItemEntity(level,
                            worldPosition.getX() + 0.5,
                            worldPosition.getY() + 0.5,
                            worldPosition.getZ() + 0.5,
                            remaining);
                    level.addFreshEntity(itemEntity);
                }
            }
        }

        //副产物
        if (currentRecipe.getSideOutputs().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS;
            ItemStack output = currentRecipe.getSideOutputs().get(0).copy();

            float finalMultiplier = currentRecipe.getSideOutputMultiplier() / currentRecipe.getMainToSideRatio();
            int newCount = (int) Math.ceil(output.getCount() * finalMultiplier);
            output.setCount(Math.min(newCount, output.getMaxStackSize()));

            ItemStack remaining = itemHandler.insertItem(slotIndex, output, false);
            if (!remaining.isEmpty()) {
                if (level != null) {
                    ItemEntity itemEntity = new ItemEntity(level,
                            worldPosition.getX() + 0.5,
                            worldPosition.getY() + 0.5,
                            worldPosition.getZ() + 0.5,
                            remaining);
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }

    private void placeReturnMaterials() {
        if (currentRecipe == null || level == null) return;

        if (currentRecipe.getReturnChance() > 0 && currentRecipe.getReturnMaterials().size() > 0 &&
                level.random.nextFloat() <= currentRecipe.getReturnChance()) {
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS + SIDE_OUTPUT_SLOTS;
            ItemStack returnMat = currentRecipe.getReturnMaterials().get(0).copy();

            ItemStack remaining = itemHandler.insertItem(slotIndex, returnMat, false);
            if (!remaining.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5,
                        remaining);
                level.addFreshEntity(itemEntity);
            }
        }
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;

        try {
            synchronized (this) {
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(level,
                                worldPosition.getX() + 0.5,
                                worldPosition.getY() + 0.5,
                                worldPosition.getZ() + 0.5,
                                stack.copy());
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);

                        itemHandler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error dropping forge contents: " + e.getMessage());
        }
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}