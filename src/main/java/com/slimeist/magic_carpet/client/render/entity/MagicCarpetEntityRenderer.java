package com.slimeist.magic_carpet.client.render.entity;

import com.slimeist.magic_carpet.MagicCarpetMod;
import com.slimeist.magic_carpet.client.util.CarpetUtil;
import com.slimeist.magic_carpet.common.entity.MagicCarpetEntity;
import com.slimeist.magic_carpet.common.enums.CarpetLayer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class MagicCarpetEntityRenderer extends EntityRenderer<MagicCarpetEntity> {

    private static final float TOP_MIN_U = 0f, TOP_MIN_V = 0f, TOP_MAX_U = 1f, TOP_MAX_V = 1f;
    private static final float BOTTOM_MIN_U = 0f, BOTTOM_MIN_V = 0f, BOTTOM_MAX_U = 1f, BOTTOM_MAX_V = 1f;
    private static final int FACE_COUNT = 16, VERTEX_COUNT = FACE_COUNT + 1;
    private static final float WIDTH = 2f, LENGTH = 3f;
    private static final float X1 = WIDTH / 2f, X0 = -WIDTH / 2f;
    private static final float[] VERT_Y = new float[VERTEX_COUNT];
    private static final float[] VERT_Z = new float[VERTEX_COUNT];

    //private MagicCarpetEntityModel model;
    public MagicCarpetEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.8f;
        //this.model = new MagicCarpetEntityModel(ctx.getPart(MagicCarpetModClient.MODEL_MAGIC_CARPET_LAYER));
    }

    @Override
    public boolean shouldRender(MagicCarpetEntity entity, Frustum frustum, double x, double y, double z) {
        return entity.world == null || super.shouldRender(entity, frustum, x, y, z);
    }

    @Override
    public void render(MagicCarpetEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        render(entity, yaw, tickDelta, entity.age, matrices, vertexConsumers, light, false);
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    public static void render(MagicCarpetEntity entity, float yaw, float tickDelta, int age, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, boolean isItem) {
        matrices.push();

        //Orientation
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f - yaw));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-entity.getPitch()));
        matrices.translate(0, -0.85, 0);

        //Render time!
        float ageInTicks = age + tickDelta;
        for (int i = 0; i < VERTEX_COUNT; i++) {
            VERT_Z[i] = (float) i / FACE_COUNT * LENGTH - LENGTH / 2;
            VERT_Y[i] = MathHelper.sin(ageInTicks * 0.1f + i * 0.25f) * 0.065f + 0.95f;// + (VERT_Z[i] * MathHelper.sin(entity.getPitch() * ((float)Math.PI / 180)));
        }

        drawCarpet(matrices, getConsumer(vertexConsumers, CarpetLayer.CENTER, entity), light);
        drawCarpet(matrices, getConsumer(vertexConsumers, CarpetLayer.BORDER, entity), light);
        drawCarpet(matrices, getConsumer(vertexConsumers, CarpetLayer.DECORATION, entity), light);

        //Cleanup
        matrices.pop();
    }

    private static VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers, CarpetLayer layer, MagicCarpetEntity entity) {
        return vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(CarpetUtil.getTexture(layer, entity.getLayerColor(layer))));
    }

    private static void drawCarpet(MatrixStack matrixStack, VertexConsumer buf, int packedLight) {
        MatrixStack.Entry last = matrixStack.peek();
        Matrix4f matrix4f = last.getPositionMatrix();
        Matrix3f matrixNormal = last.getNormalMatrix();

        for (int i = 0; i < FACE_COUNT; i++) {
            float t0 = (float) i / FACE_COUNT;
            float t1 = (float) (i + 1) / FACE_COUNT;
            float y0 = VERT_Y[i], y1 = VERT_Y[i + 1];
            float z0 = VERT_Z[i], z1 = VERT_Z[i + 1];
            // up top
            float v00 = TOP_MIN_V + (TOP_MAX_V - TOP_MIN_V) * t0;
            float v01 = TOP_MIN_V + (TOP_MAX_V - TOP_MIN_V) * t1;
            buf.vertex(matrix4f, X0, y0, z0).color(1f, 1f, 1f, 1f).texture(TOP_MIN_U, v00).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(matrixNormal, 0f, 1f, 0f).next();
            buf.vertex(matrix4f, X0, y1, z1).color(1f, 1f, 1f, 1f).texture(TOP_MIN_U, v01).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(matrixNormal, 0f, 1f, 0f).next();
            buf.vertex(matrix4f, X1, y1, z1).color(1f, 1f, 1f, 1f).texture(TOP_MAX_U, v01).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(matrixNormal, 0f, 1f, 0f).next();
            buf.vertex(matrix4f, X1, y0, z0).color(1f, 1f, 1f, 1f).texture(TOP_MAX_U, v00).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(matrixNormal, 0f, 1f, 0f).next();
            // up bottom
            /*float v10 = BOTTOM_MIN_V + (BOTTOM_MAX_V - BOTTOM_MIN_V) * t0;
            float v11 = BOTTOM_MIN_V + (BOTTOM_MAX_V - BOTTOM_MIN_V) * t1;
            float sep = 0.0001f;//prevent z-fighting
            buf.vertex(matrix4f, X0, y1-sep, z1).color(1f, 1f, 1f, 1f).texture(BOTTOM_MIN_U, v11).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(matrixNormal, 0f, -1f, 0f).next();
            buf.vertex(matrix4f, X0, y0-sep, z0).color(1f, 1f, 1f, 1f).texture(BOTTOM_MIN_U, v10).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(matrixNormal, 0f, -1f, 0f).next();
            buf.vertex(matrix4f, X1, y0-sep, z0).color(1f, 1f, 1f, 1f).texture(BOTTOM_MAX_U, v10).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(matrixNormal, 0f, -1f, 0f).next();
            buf.vertex(matrix4f, X1, y1-sep, z1).color(1f, 1f, 1f, 1f).texture(BOTTOM_MAX_U, v11).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(matrixNormal, 0f, -1f, 0f).next();*/
        }
    }

    @Override
    public Identifier getTexture(MagicCarpetEntity entity) {
        return MagicCarpetMod.id("textures/entity/magic_carpet/magic_carpet.png");
    }
}
