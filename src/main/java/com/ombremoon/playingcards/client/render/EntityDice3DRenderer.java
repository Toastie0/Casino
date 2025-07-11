package com.ombremoon.playingcards.client.render;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.client.model.EntityDiceModel;
import com.ombremoon.playingcards.entity.EntityDice;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * 3D renderer for EntityDice using proper cube models.
 * Based on Dice-Rebuild mod's approach.
 */
public class EntityDice3DRenderer extends EntityRenderer<EntityDice> {

    private final EntityDiceModel model;

    public EntityDice3DRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new EntityDiceModel(context.getPart(EntityDiceModel.LAYER));
    }

    @Override
    public void render(EntityDice entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        
        matrices.push();
        
        // Position slightly above ground
        matrices.translate(0, 0.1, 0);
        
        // Add some rotation based on current face for visual variety  
        float rotation = entity.getCurrentFace() * 60.0F;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotation * 0.7F));
        
        // Render the 3D cube model using RenderLayer.getEntityCutout
        this.model.render(matrices, vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getEntityCutout(this.getTexture(entity))), light, 0, 1.0F, 1.0F, 1.0F, 1.0F);
        
        matrices.pop();
    }

    @Override
    public Identifier getTexture(EntityDice entity) {
        int maxSides = entity.getMaxSides();
        String sidesStr = String.valueOf(maxSides);
        String ownerPrefix = entity.hasOwner() ? "fantasy_" : "simple_";
        
        return new Identifier(PCReference.MOD_ID, "textures/entity/" + ownerPrefix + "dice_" + sidesStr + ".png");
    }
}
