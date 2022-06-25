package com.slimeist.magic_carpet.client.render.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.slimeist.magic_carpet.MagicCarpetMod;
import com.slimeist.magic_carpet.client.util.CarpetUtil;
import com.slimeist.magic_carpet.common.enums.CarpetLayer;
import com.slimeist.magic_carpet.common.gui.CarpetLoomScreenHandler;
import com.slimeist.magic_carpet.common.item.MagicCarpetItem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class CarpetLoomScreen extends HandledScreen<CarpetLoomScreenHandler> {
    private static final Identifier TEXTURE = MagicCarpetMod.id("textures/gui/container/carpet_loom.png");
    public CarpetLoomScreen(CarpetLoomScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        handler.setInventoryChangeListener(this::onInventoryChanged);
        this.titleY -= 2;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        this.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        Slot outputSlot = handler.getOutputSlot();
        ItemStack outputStack = outputSlot.getStack();
        if (!outputStack.isEmpty()) {
            matrices.push();
            //matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
            //matrices.scale(1/16f, 1/16f, 1/16f);
            DyeColor borderColor = MagicCarpetItem.getLayerColor(outputStack, CarpetLayer.BORDER);
            DyeColor centerColor = MagicCarpetItem.getLayerColor(outputStack, CarpetLayer.CENTER);
            DyeColor decorationColor = MagicCarpetItem.getLayerColor(outputStack, CarpetLayer.DECORATION);

            matrices.translate(this.x+73, this.y+17, 0.0);
            matrices.scale(32/256f, 48/256f, 1.0f);
            /*matrices.scale(1/24f, 1/24f, 1/1.0f);
            matrices.translate(0.5, 0.5, 0.5);
            float f = 0.6666667f;
            matrices.scale(0.6666667f, -0.6666667f, -0.6666667f);*/

            RenderSystem.setShaderTexture(0, CarpetUtil.getTexture(CarpetLayer.CENTER, centerColor));
            this.drawTexture(matrices, 0, 0, 0, 0, 256, 256);

            RenderSystem.setShaderTexture(0, CarpetUtil.getTexture(CarpetLayer.BORDER, borderColor));
            this.drawTexture(matrices, 0, 0, 0, 0, 256, 256);

            RenderSystem.setShaderTexture(0, CarpetUtil.getTexture(CarpetLayer.DECORATION, decorationColor));
            this.drawTexture(matrices, 0, 0, 0, 0, 256, 256);

            //magicCarpetItemRenderer.render(outputStack, ModelTransformation.Mode.GUI, matrices, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
            //MinecraftClient.getInstance().getItemRenderer().renderInGui(outputStack, this.x+73, this.y+17);
            matrices.pop();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    private void onInventoryChanged() {}
}
