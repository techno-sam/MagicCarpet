package com.slimeist.magic_carpet.client.render.item;

import com.slimeist.magic_carpet.MagicCarpetMod;
import com.slimeist.magic_carpet.client.render.entity.MagicCarpetEntityRenderer;
import com.slimeist.magic_carpet.common.entity.MagicCarpetEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class MagicCarpetItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private final MagicCarpetEntity entity = MagicCarpetMod.MAGIC_CARPET.create(null);

    @Override
    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        assert entity != null;
        entity.setColors(stack);
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        MagicCarpetEntityRenderer.render(entity, 0f, MinecraftClient.getInstance().getTickDelta(), player != null ? player.age: 0, matrices, vertexConsumers, light, true);
    }
}
