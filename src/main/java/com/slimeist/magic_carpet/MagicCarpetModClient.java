package com.slimeist.magic_carpet;

import com.slimeist.magic_carpet.client.render.entity.MagicCarpetEntityRenderer;
import com.slimeist.magic_carpet.client.render.entity.model.MagicCarpetEntityModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class MagicCarpetModClient implements ClientModInitializer {
    public static final EntityModelLayer MODEL_MAGIC_CARPET_LAYER = new EntityModelLayer(MagicCarpetMod.id("magic_carpet"), "main");
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(MagicCarpetMod.MAGIC_CARPET, MagicCarpetEntityRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(MODEL_MAGIC_CARPET_LAYER, MagicCarpetEntityModel::getTexturedModelData);
    }
}
