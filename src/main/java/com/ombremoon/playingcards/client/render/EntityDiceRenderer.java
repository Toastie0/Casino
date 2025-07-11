package com.ombremoon.playingcards.client.render;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.entity.EntityDice;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.item.ItemFantasyDice;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * Renderer for dice entities.
 * Uses item models for rendering with appropriate rotations and scaling.
 */
public class EntityDiceRenderer extends EntityRenderer<EntityDice> {

    private final ItemRenderer itemRenderer;

    public EntityDiceRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(EntityDice entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        matrices.push();
        
        // Position the dice slightly above ground to prevent z-fighting
        matrices.translate(0, 0.1, 0);
        
        // Scale up the dice to make it more visible
        matrices.scale(0.8F, 0.8F, 0.8F);
        
        // Add some rotation based on the current face for visual variety
        float rotation = entity.getCurrentFace() * 60.0F; // 60 degrees per face
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotation * 0.7F));
        
        // Get the appropriate item stack to render
        ItemStack itemStack = getDiceItemStack(entity);
        
        // Render the item
        this.itemRenderer.renderItem(
            itemStack, 
            ModelTransformationMode.GROUND, 
            light, 
            0, 
            matrices, 
            vertexConsumers, 
            entity.getWorld(), 
            entity.getId()
        );
        
        matrices.pop();
    }

    /**
     * Get the appropriate ItemStack to render for this dice entity
     */
    private ItemStack getDiceItemStack(EntityDice entity) {
        if (entity.isSimpleDice()) {
            return new ItemStack(ModItems.SIMPLE_DICE);
        } else {
            ItemStack stack = new ItemStack(ModItems.FANTASY_DICE);
            ItemFantasyDice.setSides(stack, entity.getMaxSides());
            ItemFantasyDice.setMaterial(stack, entity.getMaterial());
            
            // Set the custom model data for the correct texture
            int materialIndex = ItemFantasyDice.getMaterialTier(entity.getMaterial());
            int sidesIndex = getSidesIndex(entity.getMaxSides());
            int customModelData = materialIndex * 6 + sidesIndex;
            stack.getOrCreateNbt().putInt("CustomModelData", customModelData);
            
            return stack;
        }
    }

    /**
     * Get the index of the dice sides in the SIDES array
     */
    private int getSidesIndex(int sides) {
        return switch (sides) {
            case 4 -> 0;
            case 6 -> 1;
            case 8 -> 2;
            case 10 -> 3;
            case 12 -> 4;
            case 20 -> 5;
            default -> 1; // Default to 6-sided
        };
    }

    @Override
    public Identifier getTexture(EntityDice entity) {
        // This method is required but not used since we're using item rendering
        return new Identifier(PCReference.MOD_ID, "textures/item/dice.png");
    }
}
