package com.ombremoon.playingcards.client.model;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.entity.EntityDice;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * 3D cube model for dice entities.
 * Creates a proper 3D cube that can have textures mapped to each face.
 */
public class EntityDiceModel extends EntityModel<EntityDice> {

    private final ModelPart dice;

    public static final EntityModelLayer LAYER = new EntityModelLayer(
        new Identifier(PCReference.MOD_ID, "dice_entity"), "main"
    );

    public EntityDiceModel(ModelPart root) {
        this.dice = root.getChild("dice");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        
        // Create a 5x5x5 pixel cube (0.3125 blocks) - same as Dice-Rebuild
        modelPartData.addChild("dice", 
            ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-2.5f, -2.5f, -2.5f, 5.0F, 5.0F, 5.0F, new Dilation(0.0F)), 
            ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );
        
        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(EntityDice entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // No bone animations needed for dice - they're static cubes
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        dice.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
