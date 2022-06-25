package com.slimeist.magic_carpet;

import com.slimeist.magic_carpet.client.render.entity.MagicCarpetEntityRenderer;
import com.slimeist.magic_carpet.client.render.entity.model.MagicCarpetEntityModel;
import com.slimeist.magic_carpet.client.render.item.MagicCarpetItemRenderer;
import com.slimeist.magic_carpet.common.entity.MagicCarpetEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.input.Input;
import net.minecraft.client.render.entity.model.EntityModelLayer;

import static com.slimeist.magic_carpet.MagicCarpetMod.MAGIC_CARPET_ITEM;

public class MagicCarpetModClient implements ClientModInitializer {
    public static final EntityModelLayer MODEL_MAGIC_CARPET_LAYER = new EntityModelLayer(MagicCarpetMod.id("magic_carpet"), "main");
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(MagicCarpetMod.MAGIC_CARPET, MagicCarpetEntityRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(MODEL_MAGIC_CARPET_LAYER, MagicCarpetEntityModel::getTexturedModelData);

        ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
            if (mc.player != null) {
                Input input = mc.player.input;
                if (mc.player.getVehicle() instanceof MagicCarpetEntity magicCarpet) {
                    magicCarpet.setInputs(input.pressingLeft, input.pressingRight, input.pressingForward, input.pressingBack, mc.player.isSprinting());
                }
            }
        });

        BuiltinItemRendererRegistry.INSTANCE.register(MAGIC_CARPET_ITEM, new MagicCarpetItemRenderer());
    }
}
