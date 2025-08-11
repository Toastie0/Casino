package com.toastie01.casino.client;

import com.toastie01.casino.client.event.CardInteractEventHandler;
import com.toastie01.casino.client.render.EntityCardRenderer;
import com.toastie01.casino.client.render.EntityCardDeckRenderer;
import com.toastie01.casino.client.render.EntityPokerChipRenderer;
import com.toastie01.casino.client.render.EntitySeatRenderer;
import com.toastie01.casino.init.ModEntityTypes;
import com.toastie01.casino.network.ModNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Client-side initialization for the Casino mod.
 * Handles client-specific features like rendering and event handlers.
 */
public class CasinoClientMod implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        // Initialize client-side networking
        ModNetworking.registerClientPackets();
        
        // Initialize model overrides for dynamic card textures
        ModelOverrides.initialize();
        
        // Initialize event handlers
        CardInteractEventHandler.initialize();
        LongRangeInteractionHandler.initialize();
        
        // Register entity renderers
        registerEntityRenderers();
    }
    
    /**
     * Registers all entity renderers for casino entities.
     */
    private void registerEntityRenderers() {
        EntityRendererRegistry.register(ModEntityTypes.CARD, EntityCardRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.CARD_DECK, EntityCardDeckRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.POKER_CHIP, EntityPokerChipRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.ENTITY_SEAT, EntitySeatRenderer::new);
    }
}
