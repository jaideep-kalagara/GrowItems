package net.supergamer.growitems.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.supergamer.growitems.block.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class ItemGrowerBlockEntity extends BlockEntity implements ImplementedInventory {

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private int timer = 0;
    private int maxTimer = 0;


    /* Getter and Setters */

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.markDirty();
        this.timer = timer;
    }

    public void setMaxTimer(int maxTimer) {
        this.markDirty();
        this.maxTimer = maxTimer;
    }

    public int getMaxTimer() {
        return maxTimer;
    }

    public String getTimeFormatted() {
        float seconds = (float) getTimer() / 20.0f;
        int minutes = (int) (seconds / 60);
        return minutes + "m " + (int) (seconds % 60) + "s";
    }


    public ItemGrowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_GROWER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        assert this.world != null;
        if (oldState != this.world.getBlockState(pos)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ItemGrowerBlockEntity) {
                 ItemScatterer.spawn(world, pos, this.items);
                world.updateComparators(pos, this.world.getBlockState(pos).getBlock());
            }
            super.onBlockReplaced(pos, oldState);
        }


    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, getItems());
        view.putInt("max_timer", maxTimer);
        view.putInt("timer", timer);
    }

    @Override
    protected void readData(ReadView view) {
        this.items.clear();
        super.readData(view);
        this.maxTimer = view.getInt("max_timer", 100);
        this.timer = view.getInt("timer", this.maxTimer);
        Inventories.readData(view, getItems());
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if(this.world != null) {
            this.world.updateListeners(pos, getCachedState(), getCachedState(), 3); // update the block entity
        }
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, ItemGrowerBlockEntity entity) {
        if (entity.getStack(0).isEmpty()) return; // If the item grower is empty, do nothing
        if (entity.getTimer() > 0) { // If the timer is not 0, decrement it
            entity.setTimer(entity.getTimer() - 1);
        } else if (!world.isClient) { // If the world is not the client, scatter the item
            ItemScatterer.spawn(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), entity.getStack(0).copy());
            entity.setTimer(entity.getMaxTimer()); // reset the timer
        }
    }
}
