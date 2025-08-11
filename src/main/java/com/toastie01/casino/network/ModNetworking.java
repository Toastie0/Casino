package com.toastie01.casino.network;

import com.toastie01.casino.PCReference;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

/**
 * Central networking registration for the Casino mod
 */
public class ModNetworking {
    
    public static final Identifier CARD_INTERACT_PACKET = new Identifier(PCReference.MOD_ID, "card_interact");
    public static final Identifier LONG_RANGE_DECK_INTERACT_PACKET = new Identifier(PCReference.MOD_ID, "long_range_deck_interact");
    
    public static void registerClientPackets() {
        PCReference.LOGGER.info("Registering client network packets...");
        // Client packet handlers registered here if needed
    }
    
    public static void registerServerPackets() {
        PCReference.LOGGER.info("Registering server network packets...");
        ServerPlayNetworking.registerGlobalReceiver(CARD_INTERACT_PACKET, CardInteractPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(LONG_RANGE_DECK_INTERACT_PACKET, LongRangeDeckInteractPacket::handle);
    }
    
    /**
     * Send a card interaction packet to the server
     */
    public static void sendCardInteractToServer(String command) {
        if (ClientPlayNetworking.canSend(CARD_INTERACT_PACKET)) {
            ClientPlayNetworking.send(CARD_INTERACT_PACKET, CardInteractPacket.create(command));
        }
    }
    
    /**
     * Send a long-range deck interaction packet to the server
     */
    public static void sendLongRangeDeckInteractToServer(int entityId) {
        if (ClientPlayNetworking.canSend(LONG_RANGE_DECK_INTERACT_PACKET)) {
            ClientPlayNetworking.send(LONG_RANGE_DECK_INTERACT_PACKET, LongRangeDeckInteractPacket.create(entityId));
        }
    }
}
