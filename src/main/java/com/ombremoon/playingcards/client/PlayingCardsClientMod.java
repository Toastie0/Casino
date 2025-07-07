package com.ombremoon.playingcards.client;

import com.ombremoon.playingcards.client.event.CardInteractEventHandler;
import com.ombremoon.playingcards.client.render.EntityCardRenderer;
import com.ombremoon.playingcards.client.render.EntityCardDeckRenderer;
import com.ombremoon.playingcards.client.render.EntityDiceRenderer;
import com.ombremoon.playingcards.client.render.EntityPokerChipRenderer;
import com.ombremoon.playingcards.client.render.EntitySeatRenderer;
import com.ombremoon.playingcards.init.ModEntityTypes;
import com.ombremoon.playingcards.network.ModNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class PlayingCardsClientMod implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        // Initialize client-side networking
        ModNetworking.registerClientPackets();
        
        // Initialize event handlers
        CardInteractEventHandler.initialize();
        
        // Register entity renderers
        EntityRendererRegistry.register(ModEntityTypes.CARD, EntityCardRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.CARD_DECK, EntityCardDeckRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.POKER_CHIP, EntityPokerChipRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.ENTITY_DICE, EntityDiceRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.ENTITY_SEAT, EntitySeatRenderer::new);
    }
}
