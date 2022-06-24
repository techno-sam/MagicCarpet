package com.slimeist.magic_carpet.client.render.entity;

import com.slimeist.magic_carpet.MagicCarpetMod;
import com.slimeist.magic_carpet.MagicCarpetModClient;
import com.slimeist.magic_carpet.client.render.entity.model.MagicCarpetEntityModel;
import com.slimeist.magic_carpet.common.entity.MagicCarpetEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class MagicCarpetEntityRenderer extends EntityRenderer<MagicCarpetEntity> {
    //private MagicCarpetEntityModel model;
    public MagicCarpetEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.8f;
        //this.model = new MagicCarpetEntityModel(ctx.getPart(MagicCarpetModClient.MODEL_MAGIC_CARPET_LAYER));
    }

    @Override
    public void render(MagicCarpetEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f - yaw));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-entity.getPitch()));
        matrices.scale(1/16f, 1/16f, 1/16f);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(this.getTexture(entity)));
        renderSide(entity, matrices.peek().getPositionMatrix(), matrices.peek().getNormalMatrix(), vertexConsumer, -16, 16, 0, 1, -24, -24, 24, 24, light);
        matrices.pop();
    }

    private void renderSide(MagicCarpetEntity entity, Matrix4f model, Matrix3f normal, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, int light) {
        vertices.vertex(model, x1, y1, z1).color(255, 255, 255, 255).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
        vertices.vertex(model, x2, y1, z2).color(255, 255, 255, 255).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
        vertices.vertex(model, x2, y2, z3).color(255, 255, 255, 255).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
        vertices.vertex(model, x1, y2, z4).color(255, 255, 255, 255).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
    }

    @Override
    public Identifier getTexture(MagicCarpetEntity entity) {
        return MagicCarpetMod.id("textures/entity/magic_carpet/magic_carpet.png");
    }
}
