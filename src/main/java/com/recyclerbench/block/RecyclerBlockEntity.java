package com.recyclerbench.block;

import java.util.List;

import com.recyclerbench.RecyclerConfig;
import com.recyclerbench.recycling.Recycling;
import com.recyclerbench.sound.ModSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.recyclerbench.menu.RecyclerMenu;

public class RecyclerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_COUNT = 10;
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_START = 1;
    public static final int OUTPUT_SLOTS = 9;

    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            RecyclerBlockEntity.this.setChanged();
        }
    };
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> RecyclerConfig.processTicks();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private int progress;
    private ItemStack tracked = ItemStack.EMPTY;

    public RecyclerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RECYCLER_BENCH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RecyclerBlockEntity recycler) {
        recycler.tickServer();
    }

    private void tickServer() {
        Level level = this.level;
        if (level == null) {
            return;
        }
        ItemStack input = this.inventory.getItem(INPUT_SLOT);
        if (input.isEmpty()) {
            this.progress = 0;
            this.tracked = ItemStack.EMPTY;
            this.setLit(false);
            return;
        }
        if (!ItemStack.isSameItemSameComponents(input, this.tracked)) {
            this.progress = 0;
            this.tracked = input.copyWithCount(1);
        }

        List<ItemStack> results = Recycling.resultsFor(level, input);
        if (results.isEmpty() || !this.insertResults(results, true)) {
            this.setLit(false);
            return;
        }

        this.progress++;
        this.setLit(true);
        if (RecyclerConfig.playChainsaw() && (this.progress == 1 || this.progress % 8 == 0)) {
            level.playSound(null, this.worldPosition, ModSounds.CHAINSAW.get(), SoundSource.BLOCKS,
                    0.8F, 0.94F + level.random.nextFloat() * 0.08F);
        }
        if (this.progress % 4 == 0 && level instanceof ServerLevel serverLevel) {
            double x = this.worldPosition.getX() + 0.5D;
            double y = this.worldPosition.getY() + 1.05D;
            double z = this.worldPosition.getZ() + 0.5D;
            serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0.12D, 0.02D, 0.12D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.COMPOSTER, x, y, z, 2, 0.16D, 0.05D, 0.16D, 0.01D);
        }
        if (this.progress >= RecyclerConfig.processTicks()) {
            this.insertResults(results, false);
            this.inventory.removeItem(INPUT_SLOT, 1);
            this.progress = 0;
            ItemStack remaining = this.inventory.getItem(INPUT_SLOT);
            this.tracked = remaining.isEmpty() ? ItemStack.EMPTY : remaining.copyWithCount(1);
            this.setChanged();
        }
    }

    private boolean insertResults(List<ItemStack> results, boolean simulate) {
        ItemStack[] slots = new ItemStack[OUTPUT_SLOTS];
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            slots[i] = this.inventory.getItem(OUTPUT_START + i).copy();
        }
        for (ItemStack result : results) {
            int remaining = result.getCount();
            for (int i = 0; i < OUTPUT_SLOTS && remaining > 0; i++) {
                ItemStack slot = slots[i];
                if (slot.isEmpty()) {
                    int take = Math.min(remaining, result.getMaxStackSize());
                    slots[i] = result.copyWithCount(take);
                    remaining -= take;
                } else if (ItemStack.isSameItemSameComponents(slot, result)) {
                    int room = slot.getMaxStackSize() - slot.getCount();
                    int take = Math.min(remaining, Math.max(0, room));
                    if (take > 0) {
                        slot.grow(take);
                        remaining -= take;
                    }
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        if (!simulate) {
            for (int i = 0; i < OUTPUT_SLOTS; i++) {
                this.inventory.setItem(OUTPUT_START + i, slots[i]);
            }
        }
        return true;
    }

    private void setLit(boolean lit) {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(this.worldPosition);
        if (state.is(ModBlocks.RECYCLER_BENCH.get()) && state.getValue(RecyclerBlock.LIT) != lit) {
            level.setBlock(this.worldPosition, state.setValue(RecyclerBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    public Container getInventory() {
        return this.inventory;
    }

    public ContainerData getData() {
        return this.data;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RecyclerMenu(containerId, playerInventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.recyclerbench.recycler_bench");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.inventory.getItems(), registries);
        tag.putInt("Progress", this.progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.inventory.clearContent();
        ContainerHelper.loadAllItems(tag, this.inventory.getItems(), registries);
        this.progress = tag.getInt("Progress");
        ItemStack input = this.inventory.getItem(INPUT_SLOT);
        this.tracked = input.isEmpty() ? ItemStack.EMPTY : input.copyWithCount(1);
        if (input.isEmpty()) {
            this.progress = 0;
        }
    }
}
