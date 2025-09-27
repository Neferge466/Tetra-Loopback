package com.tetra_loopback.block.entity;

import com.tetra_loopback.inventory.AncientForgeMenu;
import com.tetra_loopback.recipe.AncientForgeRecipe;
import com.tetra_loopback.recipe.AncientForgeRecipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
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

    // 槽位定义
    public static final int MATERIAL_SLOTS = 9;      //3x3 原料槽
    public static final int CATALYST_SLOT = 9;       //催化剂槽
    public static final int ENERGY_SLOT = 10;        //能量源槽
    public static final int MAIN_OUTPUT_SLOTS = 1;   //主产物槽
    public static final int SIDE_OUTPUT_SLOTS = 1;   //副产物槽
    public static final int RETURN_SLOTS = 1;        //返还原料槽

    public static final int TOTAL_SLOTS = MATERIAL_SLOTS + 1 + 1 + MAIN_OUTPUT_SLOTS + SIDE_OUTPUT_SLOTS + RETURN_SLOTS;

    // 状态变量
    private int burnTime;
    private int burnTimeTotal;
    private int craftTime;
    private int craftTimeTotal;
    private AncientForgeRecipe currentRecipe;
    private boolean needsRecipeUpdate = true;
    private boolean isBurning = false; //标记是否处于燃烧时间段
    private boolean hasConsumedIngredients = false; //标记是否已经消耗了材料
    private boolean hasValidRecipe = false; //标记当前是否有有效的完整配方
    private boolean isCraftingInProgress = false; //标记合成是否正在进行中
    private boolean wasRecipeValidBefore = false; //标记之前配方是否有效

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            //当材料槽位变化时，检查是否需要重新开始合成
            if (slot < MATERIAL_SLOTS && isBurning && !isCraftingInProgress && !hasValidRecipe) {
                //检查配方是否变得有效
                if (hasValidRecipe()) {
                    hasValidRecipe = true;
                    craftTime = 0; //重置合成进度
                    hasConsumedIngredients = false; //重置材料消耗标志
                }
            }

            //当输入槽位变化时，标记需要重新检查配方
            //如果已经开始燃烧时间段，则不再更新配方
            if (slot < MATERIAL_SLOTS + 2 && !isBurning) {
                needsRecipeUpdate = true;
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    //容器数据，同步到客户端
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

    // 添加 stillValid 方法
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

        //只在需要时更新配方，并且只有在非燃烧状态时才更新
        if (entity.needsRecipeUpdate && !entity.isBurning) {
            entity.updateCurrentRecipe();
            entity.needsRecipeUpdate = false;
        }

        boolean hasRecipe = entity.currentRecipe != null;
        boolean changed = false;

        //燃烧时间段逻辑
        if (entity.isBurning) {
            entity.burnTime--;
            changed = true;

            //检查燃烧时间是否耗尽
            if (entity.burnTime <= 0) {
                entity.isBurning = false;
                entity.craftTime = 0;
                entity.hasConsumedIngredients = false; //重置材料消耗标志
                entity.hasValidRecipe = false; //重置有效配方标志
                entity.isCraftingInProgress = false; //重置合成进行中标志
                entity.wasRecipeValidBefore = false; //重置之前配方有效性标志
                changed = true;
            }
        }

        //尝试开始燃烧时间段
        if (!entity.isBurning && hasRecipe && entity.canCraft()) {
            ItemStack energyItem = entity.itemHandler.getStackInSlot(ENERGY_SLOT);
            int burnTime = entity.getBurnTime(energyItem);

            if (burnTime > 0) {
                //开始燃烧时间段
                entity.isBurning = true;
                entity.burnTime = burnTime;
                entity.burnTimeTotal = burnTime;

                //设置实际合成时间（考虑催化剂缩减）
                entity.craftTimeTotal = entity.getActualCraftTime();
                entity.craftTime = 0; //重置合成进度
                entity.hasConsumedIngredients = false; //重置材料消耗标志
                entity.hasValidRecipe = true; //标记有有效配方
                entity.isCraftingInProgress = false; //重置合成进行中标志
                entity.wasRecipeValidBefore = true; //标记之前配方有效

                //立即消耗燃料
                energyItem.shrink(1);
                changed = true;
            }
        }

        //合成逻辑，只有在燃烧时间段内且有有效配方时才进行
        if (entity.isBurning && entity.hasValidRecipe) {
            //检查配方是否仍然有效
            if (!entity.hasValidRecipe()) {
                //配方不再有效，停止合成但保持燃烧时间
                entity.craftTime = 0;
                entity.hasConsumedIngredients = false;
                entity.hasValidRecipe = false;
                entity.isCraftingInProgress = false;
                changed = true;
            } else {
                //只有在开始合成时消耗一次材料
                if (!entity.hasConsumedIngredients && entity.craftTime == 0) {
                    //消耗原料
                    entity.consumeIngredients();
                    entity.hasConsumedIngredients = true;
                    entity.isCraftingInProgress = true; //标记合成正在进行
                    changed = true;
                }

                //如果已经开始合成，则继续合成进度，即使材料被拿走
                if (entity.isCraftingInProgress) {
                    entity.craftTime++;

                    //计算实际需要的合成时间（应用催化剂缩减）
                    int actualCraftTime = entity.craftTimeTotal;

                    if (entity.craftTime >= actualCraftTime) {
                        //产出
                        entity.craftItem();

                        //重置合成进度
                        entity.craftTime = 0;
                        entity.hasConsumedIngredients = false; //重置材料消耗标志
                        entity.isCraftingInProgress = false; //重置合成进行中标志

                        //检查是否还有足够的材料进行下一次合成
                        if (!entity.hasEnoughIngredients()) {
                            entity.hasValidRecipe = false;
                        }

                        changed = true;
                    }
                }
            }
        } else if (entity.craftTime > 0) {
            //如果条件不满足但仍有进度，重置进度
            entity.craftTime = 0;
            entity.hasConsumedIngredients = false;
            entity.hasValidRecipe = false;
            entity.isCraftingInProgress = false;
            changed = true;
        }

        if (changed) {
            entity.setChanged();
        }
    }

    //检查当前配方是否仍然有效
    private boolean hasValidRecipe() {
        if (currentRecipe == null) return false;

        //如果合成正在进行中，配方仍然有效
        if (isCraftingInProgress) {
            return true;
        }

        //检查所有材料是否在正确的槽位
        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            Ingredient required = currentRecipe.getMaterials().get(i);

            //如果配方要求该槽位有物品，但物品不匹配，则返回false
            if (!required.test(stack)) {
                return false;
            }
        }

        return true;
    }

    //更新当前配方
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

    //计算实际合成时间（应用催化剂缩减）
    private int getActualCraftTime() {
        if (currentRecipe == null) return 0;

        //检查是否有催化剂
        ItemStack catalyst = itemHandler.getStackInSlot(CATALYST_SLOT);
        boolean hasCatalyst = !catalyst.isEmpty() && currentRecipe.getCatalyst().test(catalyst);

        //应用时间缩减
        float timeMultiplier = hasCatalyst ? (1.0f - currentRecipe.getTimeReduction()) : 1.0f;
        return (int) (currentRecipe.getProcessTime() * timeMultiplier);
    }

    //检查是否可以合成
    private boolean canCraft() {
        if (currentRecipe == null) return false;

        //检查原料是否足够且在正确的槽位
        if (!hasEnoughIngredients()) {
            return false;
        }

        //检查输出槽是否有足够空间
        return hasEnoughSpace();
    }

    //检查原料是否足够且在正确的槽位
    private boolean hasEnoughIngredients() {
        if (currentRecipe == null) return false;

        //检查每个原料槽
        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            Ingredient required = currentRecipe.getMaterials().get(i);
            //如果配方要求该槽位有物品，但物品不匹配或数量不足，则返回false
            if (!required.test(stack) || stack.getCount() < 1) {
                return false;
            }
        }

        return true;
    }

    //检查输出空间
    private boolean hasEnoughSpace() {
        if (currentRecipe == null) return false;

        //检查主产物空间
        if (currentRecipe.getMainOutputs().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2; // 主产物槽索引
            ItemStack output = currentRecipe.getMainOutputs().get(0).copy();

            //应用增产倍率
            if (currentRecipe.getMainOutputMultiplier() > 1.0f) {
                int newCount = (int) Math.ceil(output.getCount() * currentRecipe.getMainOutputMultiplier());
                output.setCount(Math.min(newCount, output.getMaxStackSize()));
            }

            ItemStack remaining = itemHandler.insertItem(slotIndex, output, true);
            if (!remaining.isEmpty()) {
                return false;
            }
        }

        //检查副产物空间
        if (currentRecipe.getSideOutputs().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS; // 副产物槽索引
            ItemStack output = currentRecipe.getSideOutputs().get(0).copy();

            //应用副产物倍率和比例
            float finalMultiplier = currentRecipe.getSideOutputMultiplier() / currentRecipe.getMainToSideRatio();
            int newCount = (int) Math.ceil(output.getCount() * finalMultiplier);
            output.setCount(Math.min(newCount, output.getMaxStackSize()));

            ItemStack remaining = itemHandler.insertItem(slotIndex, output, true);
            if (!remaining.isEmpty()) {
                return false;
            }
        }

        //检查返还材料空间（如果有几率返还）
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

    //执行合成
    private void craftItem() {
        if (currentRecipe == null || !hasEnoughSpace()) return;

        //放置产物（应用增产倍率）
        placeOutputs();

        //处理返还材料（根据几率）
        placeReturnMaterials();

        //标记需要重新检查配方（用于下一次燃烧时间段）
        needsRecipeUpdate = true;
    }

    //消耗原料
    private void consumeIngredients() {
        //消耗原料，只消耗正确槽位的材料
        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            Ingredient required = currentRecipe.getMaterials().get(i);

            //只有在材料匹配且数量足够时才消耗
            if (!stack.isEmpty() && required.test(stack) && stack.getCount() >= 1) {
                stack.shrink(1);
            }
        }
    }

    //放置产物（应用增产倍率）
    private void placeOutputs() {
        //主产物
        if (currentRecipe.getMainOutputs().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2; // 主产物槽索引
            ItemStack output = currentRecipe.getMainOutputs().get(0).copy();

            if (currentRecipe.getMainOutputMultiplier() > 1.0f) {
                int newCount = (int) Math.ceil(output.getCount() * currentRecipe.getMainOutputMultiplier());
                output.setCount(Math.min(newCount, output.getMaxStackSize()));
            }

            //尝试插入，如果失败则保留剩余物品
            ItemStack remaining = itemHandler.insertItem(slotIndex, output, false);
            if (!remaining.isEmpty()) {
                //记录日志或处理错误情况
                System.out.println("警告：无法完全插入物品到主产物槽位 " + slotIndex);
            }
        }

        //副产物
        if (currentRecipe.getSideOutputs().size() > 0) {
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS; // 副产物槽索引
            ItemStack output = currentRecipe.getSideOutputs().get(0).copy();

            float finalMultiplier = currentRecipe.getSideOutputMultiplier() / currentRecipe.getMainToSideRatio();
            int newCount = (int) Math.ceil(output.getCount() * finalMultiplier);
            output.setCount(Math.min(newCount, output.getMaxStackSize()));

            ItemStack remaining = itemHandler.insertItem(slotIndex, output, false);
            if (!remaining.isEmpty()) {
                System.out.println("警告：无法完全插入副产物到槽位 " + slotIndex);
            }
        }
    }

    //处理返还材料（根据几率）
    private void placeReturnMaterials() {
        //根据几率返还材料
        if (currentRecipe.getReturnChance() > 0 && currentRecipe.getReturnMaterials().size() > 0 &&
                level.random.nextFloat() <= currentRecipe.getReturnChance()) {
            int slotIndex = MATERIAL_SLOTS + 2 + MAIN_OUTPUT_SLOTS + SIDE_OUTPUT_SLOTS; // 返还材料槽索引
            ItemStack returnMat = currentRecipe.getReturnMaterials().get(0).copy();
            itemHandler.insertItem(slotIndex, returnMat, false);
        }
    }

    //获取燃烧时间
    private int getBurnTime(ItemStack fuel) {
        if (currentRecipe == null || fuel.isEmpty() || !currentRecipe.getEnergySource().test(fuel)) {
            return 0;
        }

        //使用配方中定义的能量值
        return currentRecipe.getEnergyValue();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}