package com.ombremoon.playingcards.client.render;

import com.ombremoon.playingcards.entity.EntityDice;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class EntityDiceRenderer extends EntityRenderer<EntityDice> {

    public EntityDiceRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EntityDice entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        
        // Original dice renderer is commented out - dice should only show name label "6"
        // No 3D model rendering needed
    }

    @Override
    public Identifier getTexture(EntityDice entity) {
        return null;
    }
}
