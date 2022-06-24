package com.slimeist.magic_carpet.client.render.entity;

import com.slimeist.magic_carpet.MagicCarpetMod;
import com.slimeist.magic_carpet.MagicCarpetModClient;
import com.slimeist.magic_carpet.client.render.entity.model.MagicCarpetEntityModel;
import com.slimeist.magic_carpet.common.entity.MagicCarpetEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class MagicCarpetEntityRenderer extends EntityRenderer<MagicCarpetEntity> {
    private MagicCarpetEntityModel model;
    public MagicCarpetEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.8f;
        this.model = new MagicCarpetEntityModel(ctx.getPart(MagicCarpetModClient.MODEL_MAGIC_CARPET_LAYER));
    }

    @Override
    public void render(MagicCarpetEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        this.model.setAngles(entity, 0, 0, 0, yaw, entity.getPitch(tickDelta));
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(this.getTexture(entity)));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(MagicCarpetEntity entity) {
        return MagicCarpetMod.id("textures/entity/magic_carpet/magic_carpet.png");
    }
}
