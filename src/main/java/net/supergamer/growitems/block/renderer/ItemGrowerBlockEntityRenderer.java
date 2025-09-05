package net.supergamer.growitems.block.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.supergamer.growitems.block.custom.ItemGrowerBlockEntity;

public class ItemGrowerBlockEntityRenderer implements BlockEntityRenderer<ItemGrowerBlockEntity> {

    public ItemGrowerBlockEntityRenderer(BlockEntityRendererFactory.Context ignored) {

    }

    @Override
    public void render(ItemGrowerBlockEntity entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        ItemStack stack = entity.getStack(0);

        matrices.push();
        matrices.translate(0.5, 0.8, 0.5);
        matrices.scale(0.5f, 0.5f, 0.5f);
        // convert degrees to radians
        float angle = (float) Math.toRadians(90);
        matrices.multiply(RotationAxis.NEGATIVE_X.rotation(angle));

        assert entity.getWorld() != null;
        itemRenderer.renderItem(stack, ItemDisplayContext.GUI, getLightLevel(entity.getWorld(), entity.getPos()),
                OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 1);
        matrices.pop();
    }

    private int getLightLevel(World world, BlockPos pos) {
        int bLight = world.getLightLevel(LightType.BLOCK, pos);
        int sLight = world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(bLight, sLight);
    }
}
