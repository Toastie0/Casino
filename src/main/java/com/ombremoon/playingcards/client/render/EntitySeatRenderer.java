package com.ombremoon.playingcards.client.render;

import com.ombremoon.playingcards.entity.EntitySeat;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class EntitySeatRenderer extends EntityRenderer<EntitySeat> {

    public EntitySeatRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EntitySeat entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        // Seats don't render anything - they're invisible
    }

    @Override
    public Identifier getTexture(EntitySeat entity) {
        return null;
    }
}
