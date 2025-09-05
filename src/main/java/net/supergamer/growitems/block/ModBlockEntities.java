package net.supergamer.growitems.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.supergamer.growitems.GrowItems;
import net.supergamer.growitems.block.custom.ItemGrowerBlockEntity;

public class ModBlockEntities {
    public static final BlockEntityType<ItemGrowerBlockEntity> ITEM_GROWER_BLOCK_ENTITY = registerBlockEntity(ItemGrowerBlockEntity::new, ModBlocks.ITEM_GROWER);


    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory, Block... blocks) {
        GrowItems.LOGGER.info("Registering block entity {}", "item_grower_block_entity");
        RegistryKey<BlockEntityType<?>> registryKey = RegistryKey.of(RegistryKeys.BLOCK_ENTITY_TYPE, Identifier.of(GrowItems.MOD_ID, "item_grower_block_entity"));
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, registryKey, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static void registerModBlockEntities() {
        GrowItems.LOGGER.info(GrowItems.MOD_ID + " mod block entities registered");
    }
}
