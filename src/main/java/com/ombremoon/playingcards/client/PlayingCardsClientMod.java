package com.ombremoon.playingcards.client;

import com.ombremoon.playingcards.client.event.CardInteractEventHandler;
import com.ombremoon.playingcards.client.model.EntityDiceModel;
import com.ombremoon.playingcards.client.render.EntityCardRenderer;
import com.ombremoon.playingcards.client.render.EntityCardDeckRenderer;
import com.ombremoon.playingcards.client.render.EntityDice3DRenderer;
import com.ombremoon.playingcards.client.render.EntityPokerChipRenderer;
import com.ombremoon.playingcards.client.render.EntitySeatRenderer;
import com.ombremoon.playingcards.init.ModEntityTypes;
import com.ombremoon.playingcards.network.ModNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Client-side initialization for the Casino mod.
 * Handles client-specific features like rendering, models, and event handlers.
 */
public class PlayingCardsClientMod implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        // Initialize client-side networking
        ModNetworking.registerClientPackets();
        
        // Initialize model overrides for dynamic card textures
        ModelOverrides.initialize();
        
        // Initialize event handlers
        CardInteractEventHandler.initialize();
        
        // Register model layers
        registerModelLayers();
        
        // Register entity renderers
        registerEntityRenderers();
    }
    
    /**
     * Registers all entity model layers.
     */
    private void registerModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(EntityDiceModel.LAYER, EntityDiceModel::getTexturedModelData);
    }
    
    /**
     * Registers all entity renderers for casino entities.
     */
    private void registerEntityRenderers() {
        EntityRendererRegistry.register(ModEntityTypes.CARD, EntityCardRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.CARD_DECK, EntityCardDeckRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.POKER_CHIP, EntityPokerChipRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.ENTITY_SEAT, EntitySeatRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.DICE, EntityDice3DRenderer::new);
    }
}
