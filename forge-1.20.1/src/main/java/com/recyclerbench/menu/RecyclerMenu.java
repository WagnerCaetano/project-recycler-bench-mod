package com.recyclerbench.menu;

import com.recyclerbench.block.RecyclerBlockEntity;
import com.recyclerbench.recycling.Recycling;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RecyclerMenu extends AbstractContainerMenu {
    public static final int INPUT_X = 20;
    public static final int INPUT_Y = 35;
    public static final int OUTPUT_X = 98;
    public static final int OUTPUT_Y = 17;
    public static final int INV_X = 8;
    public static final int INV_Y = 126;
    public static final int HOTBAR_Y = 184;

    private static final int INPUT = 0;
    private static final int OUTPUT_START = 1;
    private static final int OUTPUT_END = 9;
    private static final int INV_START = 10;
    private static final int INV_END = 36;
    private static final int HOTBAR_START = 37;
    private static final int HOTBAR_END = 45;

    private final Container container;
    private final ContainerData data;
    private final RecyclerBlockEntity blockEntity;

    public RecyclerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, new SimpleContainer(RecyclerBlockEntity.SLOT_COUNT), new SimpleContainerData(2), null);
        buffer.readBlockPos();
    }

    public RecyclerMenu(int containerId, Inventory playerInventory, RecyclerBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity.getInventory(), blockEntity.getData(), blockEntity);
    }

    private RecyclerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, RecyclerBlockEntity blockEntity) {
        super(ModMenus.RECYCLER_BENCH.get(), containerId);
        this.container = container;
        this.data = data;
        this.blockEntity = blockEntity;
        checkContainerSize(container, RecyclerBlockEntity.SLOT_COUNT);
        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, INPUT, INPUT_X, INPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return Recycling.canRecycle(playerInventory.player.level(), stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                this.addSlot(new Slot(container, OUTPUT_START + column + row * 3, OUTPUT_X + column * 18, OUTPUT_Y + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, INV_X + column * 18, INV_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, INV_X + column * 18, HOTBAR_Y));
        }

        this.addDataSlots(data);
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        int max = this.data.get(1);
        return max <= 0 ? 1 : max;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.blockEntity == null) {
            return true;
        }
        BlockPos pos = this.blockEntity.getBlockPos();
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            if (index >= OUTPUT_START && index <= OUTPUT_END) {
                if (!this.moveItemStackTo(stack, INV_START, HOTBAR_END + 1, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, original);
            } else if (index == INPUT) {
                if (!this.moveItemStackTo(stack, INV_START, HOTBAR_END + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, INPUT, INPUT + 1, false)) {
                if (index >= INV_START && index <= INV_END) {
                    if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= HOTBAR_START && index <= HOTBAR_END
                        && !this.moveItemStackTo(stack, INV_START, INV_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
