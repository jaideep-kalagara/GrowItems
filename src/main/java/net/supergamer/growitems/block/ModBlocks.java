package net.supergamer.growitems.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.supergamer.growitems.GrowItems;
import net.supergamer.growitems.block.custom.ItemGrowerBlock;

import java.util.function.Function;

public class ModBlocks {
    public static final Block ITEM_GROWER = registerBlock("item_grower", ItemGrowerBlock::new, AbstractBlock.Settings.create().requiresTool().strength(2.0f).nonOpaque().pistonBehavior(PistonBehavior.BLOCK), true, new Item.Settings().maxCount(1));
    ;

    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings baseBlockSettings, boolean shouldRegisterItem, @org.jetbrains.annotations.Nullable Item.Settings baseItemSettings // can be null
    ) {
        var id = Identifier.of(GrowItems.MOD_ID, name);
        var blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);


        AbstractBlock.Settings finalBlockSettings = baseBlockSettings.registryKey(blockKey);
        Block block = blockFactory.apply(finalBlockSettings);


        Block registeredBlock = Registry.register(Registries.BLOCK, blockKey, block);


        if (shouldRegisterItem) {
            var itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
            Item.Settings finalItemSettings = (baseItemSettings != null ? baseItemSettings : new Item.Settings()).registryKey(itemKey); // <- add on to what's already there

            Registry.register(Registries.ITEM, itemKey, new BlockItem(registeredBlock, finalItemSettings));
        }

        return registeredBlock;
    }


    public static void registerModBlocks() {
        GrowItems.LOGGER.info(GrowItems.MOD_ID + " mod blocks registered");
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.addAfter(Items.ENCHANTING_TABLE, ITEM_GROWER.asItem());
        });
    }
}
