package net.mcreator.concoction.block.entity;

import com.mojang.datafixers.kinds.IdF;
import net.mcreator.concoction.block.CookingCauldron;
import net.mcreator.concoction.init.ConcoctionModBlockEntities;
import net.mcreator.concoction.init.ConcoctionModRecipes;
import net.mcreator.concoction.recipe.cauldron.CauldronBrewingRecipe;
import net.mcreator.concoction.recipe.cauldron.CauldronBrewingRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import static net.minecraft.world.level.block.entity.HopperBlockEntity.suckInItems;

public class CookingCauldronEntity extends RandomizableContainerBlockEntity {
    // This can be any value of any type you want, so long as you can somehow serialize it to NBT.
    // We will use an int for the sake of example.
    // Container methods and fields
    private final int ContainerSize = 6;
    private boolean isCooking = false;
    private RecipeHolder<CauldronBrewingRecipe> recipe = null;
    private ItemStack output = ItemStack.EMPTY;

    private int progress = 0;
    private int maxProgress = 72;
    private final int DEFAULT_MAX_PROGRESS = 72;
    private NonNullList<ItemStack> items = NonNullList.withSize(
            this.ContainerSize,
            ItemStack.EMPTY
    );


    public CookingCauldronEntity(BlockPos pos, BlockState state) {
        super(ConcoctionModBlockEntities.COOKING_CAULDRON.get(), pos, state);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getInt("cooking.progress");
        this.maxProgress = tag.getInt("cooking.max_progress");
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }
    }

    // Save values into the passed CompoundTag here.
    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("cooking.progress", this.progress);
        tag.putInt("cooking.max_progress", this.maxProgress);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }
    }



    // craft logic
    // The signature of this method matches the signature of the BlockEntityTicker functional interface.
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void tick(Level level, BlockPos pPos, BlockState pState) {
        if (!level.isClientSide) {
            Block blockBelow = level.getBlockState(pPos.below()).getBlock();
            if (level.getFluidState(pPos.below()).is(Fluids.LAVA.getSource()) || blockBelow instanceof FireBlock ||
                    blockBelow instanceof MagmaBlock || blockBelow instanceof CampfireBlock) {
                if (!pState.getValue(CookingCauldron.LIT)) {
                    level.setBlockAndUpdate(pPos, pState.setValue(CookingCauldron.LIT, true));
                    setChanged(level, pPos, pState);
                }

            } else {
                if (pState.getValue(CookingCauldron.LIT)) {
                    level.setBlockAndUpdate(pPos, pState.setValue(CookingCauldron.LIT, false));
                    setChanged(level, pPos, pState);
                }
            }
            if (pState.getValue(CookingCauldron.LIT) && (this.isCooking || (hasRecipe() && isOutputSlotEmptyOrReceivable()))) {
                if (!this.isCooking) this.isCooking = true;

                increaseCraftingProgress();
                setChanged(level, pPos, pState);

                if (hasCraftingFinished()) {
                    craftItem();
                    resetProgress();
                }

            } else {
                resetProgress();
            }
        }
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = DEFAULT_MAX_PROGRESS;
        this.isCooking = false;
        this.recipe = null;
        this.output = ItemStack.EMPTY;
    }

    private void craftItem() {
            this.clearContent();
            this.setItem(0, this.output);
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    private boolean isOutputSlotEmptyOrReceivable() {
        return true;
//        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
//                this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() < this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    private boolean hasRecipe() {
        Optional<RecipeHolder<CauldronBrewingRecipe>> recipe = getCurrentRecipe();
        if(recipe.isEmpty()) {
            return false;
        }

        this.recipe = recipe.get();
        this.output = this.recipe.value().getResultItem(null);
        return canInsertAmountIntoOutputSlot(this.output.getCount()) && canInsertItemIntoOutputSlot(this.output);
    }

    private Optional<RecipeHolder<CauldronBrewingRecipe>> getCurrentRecipe() {
        return this.level.getRecipeManager()
                .getRecipeFor(ConcoctionModRecipes.CAULDRON_BREWING_RECIPE_TYPE.get(),
                        new CauldronBrewingRecipeInput(this.getBlockState(), this.getItems(), this.isCooking), level);
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return true;
//        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
//                itemHandler.getStackInSlot(OUTPUT_SLOT).getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return true;
//        int maxCount = itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ? 64 : itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
//        int currentCount = itemHandler.getStackInSlot(OUTPUT_SLOT).getCount();
//
//        return maxCount >= currentCount + count;
    }

    @Override
    public int getContainerSize() {
        return this.ContainerSize;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        stack.limitSize(this.getMaxStackSize(stack));
        this.items.set(slot, stack);
        this.setChanged();
    }

    @Override
    protected void setItems(NonNullList<ItemStack> Items) {
        this.items = Items;
    }

    //Item add to container methods
    @SuppressWarnings("UnusedReturnValue")
    public boolean addItemOnClick(ItemStack addedItem, int count, boolean isCreative) {
//        if (this.isCooking) return false;
        boolean flag = false;
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack itemstack = this.getItem(i);
            if (itemstack.isEmpty()) {
                this.setItem(i, isCreative ? addedItem.copyWithCount(1) : addedItem.split(count));
                flag = true;
                break;
//            } else if ( itemstack.getItem().equals(addedItem.getItem()) ) {
//                int to_add = Math.min(count, itemstack.getMaxStackSize()-itemstack.getCount());
//                if (isCreative || addedItem.getCount() - addedItem.split(to_add).getCount() != 0) {
//                    this.items.get(i).grow(to_add);
//                    this.setChanged();
//                    flag = true;
//                    break;
//                }
            }
        }
        if (flag) this.resetProgress();
        return flag;
    }

    public ItemStack takeItemOnClick(boolean takeAll) {
//        if (this.isCooking) return ItemStack.EMPTY;
        ItemStack returnStack = ItemStack.EMPTY;
        for (int i = this.items.size()-1; i >= 0; i--) {
            ItemStack itemstack = this.items.get(i);
            if (!itemstack.isEmpty()) {
                if (takeAll) {
                    returnStack = itemstack.copy();
                    this.setItem(i, ItemStack.EMPTY);
                } else {
                    returnStack = itemstack.split(1);
                }
                this.setChanged();
                this.resetProgress();
                return returnStack;
            }
        }
        return returnStack;
    }



    // Whether the container is considered "still valid" for the given player. For example, chests and
    // similar blocks check if the player is still within a given distance of the block here.
    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // Clear the internal storage, setting all slots to empty again.
    @Override
    public void clearContent() {
        items.clear();
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        // This will send the block entity data to the client every time the block entity is marked as changed.
        // This is useful for syncing data between the server and client.
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    // Return our packet here. This method returning a non-null result tells the game to use this packet for syncing.
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // The packet uses the CompoundTag returned by #getUpdateTag. An alternative overload of #create exists
        // that allows you to specify a custom update tag, including the ability to omit data the client might not need.
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Optionally: Run some custom logic when the packet is received.
    // The super/default implementation forwards to #loadAdditional.
    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        // Do whatever you need to do here.
    }

    // Handle a received update tag here. The default implementation calls #loadAdditional here,
    // so you do not need to override this method if you don't plan to do anything beyond that.
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.cooking_cauldron");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return null;
    }

    @Override
    protected AbstractContainerMenu createMenu(int p_58627_, Inventory p_58628_) {
        return null;
    }

    // Hopper methods
//    @Override
//    public double getLevelX() {
//        return (double)this.worldPosition.getX() + 0.5;
//    }
//
//    @Override
//    public double getLevelY() {
//        return (double)this.worldPosition.getY() + 0.5;
//    }
//
//    @Override
//    public double getLevelZ() {
//        return (double)this.worldPosition.getZ() + 0.5;
//    }
//
//    @Override
//    public boolean isGridAligned() {
//        return true;
//    }
}