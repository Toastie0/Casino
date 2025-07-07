package com.ombremoon.playingcards.client.render;

import com.ombremoon.playingcards.entity.EntityCardDeck;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.util.CardHelper;
import com.ombremoon.playingcards.util.ItemHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class EntityCardDeckRenderer extends EntityRenderer<EntityCardDeck> {

    public EntityCardDeckRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EntityCardDeck entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        ItemStack cardStack = new ItemStack(ModItems.CARD_COVERED);
        ItemHelper.getNBT(cardStack).putByte("SkinID", entity.getSkinID());

        matrices.push();
        // Remove the problematic rotation and use a simpler approach
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRotation()));
        matrices.scale(1.5F, 1.5F, 1.5F);

        // Render multiple cards to form a deck - ensure minimum visibility
        int cardsToRender = Math.max(3, entity.getStackAmount());
        for (int i = 0; i < cardsToRender; i++) {
            CardHelper.renderItem(cardStack, entity.getWorld(), 0, i * 0.003D, 0, matrices, vertexConsumers, light);
        }

        matrices.pop();
    }

    @Override
    public Identifier getTexture(EntityCardDeck entity) {
        return null;
    }
}
