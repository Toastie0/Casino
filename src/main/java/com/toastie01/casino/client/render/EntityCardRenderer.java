package com.toastie01.casino.client.render;

import com.toastie01.casino.entity.EntityCard;
import com.toastie01.casino.init.ModItems;
import com.toastie01.casino.util.CardHelper;
import com.toastie01.casino.util.ItemHelper;
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
        matrices.scale(1.5F, 1.5F, 1.5F); // Same scale for both face-up and face-down

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
