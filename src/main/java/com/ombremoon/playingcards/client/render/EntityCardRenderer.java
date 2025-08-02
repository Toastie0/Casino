package com.ombremoon.playingcards.client.render;

import com.ombremoon.playingcards.entity.EntityCard;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.util.CardHelper;
import com.ombremoon.playingcards.util.ItemHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class EntityCardRenderer extends EntityRenderer<EntityCard> {

    public EntityCardRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EntityCard entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        ItemStack card = new ItemStack(ModItems.CARD);
        card.setDamage(entity.getTopStackID());

        if (entity.isCovered()) {
            card = new ItemStack(ModItems.CARD_COVERED);
            NbtCompound nbt = ItemHelper.getNBT(card);
            nbt.putByte("SkinID", entity.getSkinID());
            nbt.putInt("CustomModelData", entity.getSkinID()); // Fix texture rendering
        } else {
            // Face-up card rendering
            NbtCompound nbt = ItemHelper.getNBT(card);
            nbt.putByte("SkinID", entity.getSkinID());
            nbt.putInt("CustomModelData", 100 + entity.getTopStackID()); // Face-up texture
        }

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-entity.getRotation() + 180));
        
        // Face-up cards should lie flat on the ground and be sized to match face-down cards
        if (!entity.isCovered()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90)); // Rotate to lie flat
            matrices.scale(1.0F, 1.0F, 1.0F); // Smaller scale to match face-down card visual size
        } else {
            matrices.scale(1.5F, 1.5F, 1.5F); // Face-down card size
        }

        for (byte i = 0; i < Math.max(1, entity.getStackAmount()); i++) {
            CardHelper.renderItem(card, entity.getWorld(), 0, i * 0.003D, 0, matrices, vertexConsumers, light);
        }

        matrices.pop();
    }

    @Override
    public Identifier getTexture(EntityCard entity) {
        return null;
    }
}
