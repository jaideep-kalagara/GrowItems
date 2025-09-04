package net.supergamer.growitems.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.supergamer.growitems.block.ModBlockEntities;
import net.supergamer.growitems.block.renderer.ItemGrowerBlockEntityRenderer;

public class GrowItemsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(ModBlockEntities.ITEM_GROWER_BLOCK_ENTITY, ItemGrowerBlockEntityRenderer::new);
    }
}
