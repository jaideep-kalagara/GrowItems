package net.supergamer.growitems.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.supergamer.growitems.block.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import static net.supergamer.growitems.block.custom.ItemGrowerBlockGrowthTimings.ITEM_TO_TICKS;


public class ItemGrowerBlock extends BlockWithEntity {
    public static final MapCodec<ItemGrowerBlock> CODEC = createCodec(ItemGrowerBlock::new);

    public ItemGrowerBlock(Settings settings) {
        super(settings);

        computeAllItemsRecipes();
    }

    private void computeAllItemsRecipes() {

    }


    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }


    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        // if the block is not an item grower block entity
        if (!(world.getBlockEntity(pos) instanceof ItemGrowerBlockEntity blockEntity)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }

        if (!player.getStackInHand(hand).isEmpty()) { // if the player is holding an item
            if (blockEntity.getStack(0).isEmpty()) { // if the block is empty
                if (ITEM_TO_TICKS.get(player.getStackInHand(hand).getItem()) != null) {
                    blockEntity.setMaxTimer(ITEM_TO_TICKS.get(player.getStackInHand(hand).getItem()));
                    blockEntity.setTimer(blockEntity.getMaxTimer());
                } else {
                    player.sendMessage(Text.translatable("text.growitems.item_grower_invalid_item"), true);
                    return ActionResult.FAIL;
                }

                blockEntity.markDirty();
                blockEntity.setStack(0, player.getStackInHand(hand).copyWithCount(1));
                player.getStackInHand(hand).decrement(1);
            } else { // if the block is not empty
                player.sendMessage(Text.translatable("text.growitems.item_grower_full"), true);
            }
        } else if (player.getStackInHand(hand).isEmpty()) {
            // if the player is sneaking, remove the item from the block
            if (!blockEntity.getStack(0).isEmpty() && player.isSneaking()) {
                blockEntity.markDirty();
                player.giveItemStack(blockEntity.getStack(0));
                blockEntity.removeStack(0);
            }
            // if the player is not sneaking, show the time left on the item to grow
            if (!blockEntity.getStack(0).isEmpty() && !player.isSneaking()) {
                // format time nicely
                player.sendMessage(Text.translatable("text.growitems.item_grower_time_left", blockEntity.getTimeFormatted()), true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.ITEM_GROWER_BLOCK_ENTITY, ItemGrowerBlockEntity::tick);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ItemGrowerBlockEntity(pos, state);
    }
}
