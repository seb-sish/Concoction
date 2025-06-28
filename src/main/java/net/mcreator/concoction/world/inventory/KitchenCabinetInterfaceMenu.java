package net.mcreator.concoction.world.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

import net.mcreator.concoction.init.ConcoctionModMenus;
import net.mcreator.concoction.block.entity.OakKitchenCabinetBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class KitchenCabinetInterfaceMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    public final static HashMap<String, Object> guistate = new HashMap<>();
    public final Level world;
    public final Player entity;
    public int x, y, z;
    private ContainerLevelAccess access = ContainerLevelAccess.NULL;
    private final OakKitchenCabinetBlockEntity blockEntity;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private boolean bound = false;

    public KitchenCabinetInterfaceMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ConcoctionModMenus.KITCHEN_CABINET_INTERFACE.get(), id);
        this.entity = inv.player;
        this.world = inv.player.level();
        BlockPos pos = null;
        if (extraData != null) {
            pos = extraData.readBlockPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            access = ContainerLevelAccess.create(world, pos);
            // Get the block entity at the position
            if (world.getBlockEntity(pos) instanceof OakKitchenCabinetBlockEntity be) {
                this.blockEntity = be;
                this.bound = true;
            } else {
                this.blockEntity = null; // fallback
            }
        } else {
            this.blockEntity = null;
        }

        // Use blockEntity inventory or fallback to empty inventory if missing
        if (this.blockEntity != null) {
            // Add container (cabinet) slots (3 rows x 9 cols)
            int slotSize = 18;
            int startX = 8;
            int startY = 17; // shifted 1 pixel up from 18
            for (int row = 0; row < 3; ++row) {
                for (int col = 0; col < 9; ++col) {
                    int slotIndex = col + row * 9;
                    Slot slot = this.addSlot(new Slot(blockEntity, slotIndex, startX + col * slotSize, startY + row * slotSize));
                    customSlots.put(slotIndex, slot);
                }
            }
        } else {
            // fallback to empty slots if no block entity found
            for (int i = 0; i < 27; i++) {
                this.addSlot(new Slot(new net.minecraft.world.SimpleContainer(1), i, 0, 0));
            }
        }

        // Add player inventory slots (3 rows x 9 cols)
        int playerInvStartY = 84; // shifted 1 pixel up from 85
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int slotIndex = col + row * 9 + 9;
                this.addSlot(new Slot(inv, slotIndex, 8 + col * 18, playerInvStartY + row * 18));
            }
        }

        // Add player hotbar slots (1 row x 9 cols)
        int hotbarY = 142; // shifted 1 pixel up from 143
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, hotbarY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.bound && this.blockEntity != null) {
            return access.evaluate((world, pos) ->
                world.getBlockState(pos).getBlock() == this.blockEntity.getBlockState().getBlock(),
                true);
        }
        return true;
    }

    @Override
    public Map<Integer, Slot> get() {
        return customSlots;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            int containerSlots = blockEntity != null ? blockEntity.getContainerSize() : 0;

            if (index < containerSlots) {
                // Move from container to player inventory
                if (!this.moveItemStackTo(itemstack1, containerSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to container
                if (!this.moveItemStackTo(itemstack1, 0, containerSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }
}
