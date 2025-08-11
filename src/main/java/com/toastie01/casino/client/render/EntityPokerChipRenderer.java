package com.toastie01.casino.client.render;

import com.toastie01.casino.entity.EntityPokerChip;
import com.toastie01.casino.item.ItemPokerChip;
import com.toastie01.casino.util.CardHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.Random;

public class EntityPokerChipRenderer extends EntityRenderer<EntityPokerChip> {

    public EntityPokerChipRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EntityPokerChip entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        matrices.push();
        matrices.translate(0, 0.01D, 0.07D);
        matrices.scale(0.5F, 0.5F, 0.5F);

        for (byte i = 0; i < entity.getStackAmount(); i++) {
            matrices.push();

            Random randomX = new Random(i * 200000);
            Random randomY = new Random(i * 100000);

            matrices.translate(randomX.nextDouble() * 0.05D - 0.025D, 0, randomY.nextDouble() * 0.05D - 0.025D);
            matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(90));

            CardHelper.renderItem(new ItemStack(ItemPokerChip.getPokerChip(entity.getIDAt(i))), entity.getWorld(), 0, 0, i * 0.032D, matrices, vertexConsumers, light);

            matrices.pop();
        }

        matrices.pop();
    }

    @Override
    public Identifier getTexture(EntityPokerChip entity) {
        return null;
    }
}
