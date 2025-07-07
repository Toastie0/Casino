package com.ombremoon.playingcards.network;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.item.ItemCardCovered;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;

public class CardInteractPacket {
    
    /**
     * Create a packet buffer for card interaction
     * @param command The interaction command
     * @return PacketByteBuf containing the command
     */
    public static PacketByteBuf create(String command) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(command, 11);
        return buf;
    }
    
    /**
     * Handle card interaction packet on server side
     * @param server The server instance
     * @param player The player who sent the packet
     * @param handler The network handler
     * @param buf The packet buffer
     * @param responseSender The response sender
     */
    public static void handle(net.minecraft.server.MinecraftServer server, net.minecraft.server.network.ServerPlayerEntity player, 
                            net.minecraft.server.network.ServerPlayNetworkHandler handler, net.minecraft.network.PacketByteBuf buf, 
                            net.fabricmc.fabric.api.networking.v1.PacketSender responseSender) {
        
        // Read the command from the packet
        String command = buf.readString(11).trim();
        
        // Execute on server thread
        server.execute(() -> {
            try {
                if (command.equalsIgnoreCase("flipinv")) {
                    handleCardFlip(player);
                }
            } catch (Exception e) {
                PCReference.LOGGER.error("Error handling card interact packet: {}", e.getMessage());
            }
        });
    }
    
    /**
     * Handle card flip interaction
     * @param player The player flipping the card
     */
    private static void handleCardFlip(net.minecraft.server.network.ServerPlayerEntity player) {
        ItemStack heldStack = player.getMainHandStack();
        Item item = heldStack.getItem();
        
        if (item instanceof ItemCardCovered cardCovered) {
            // Flip the card in the main hand
            cardCovered.flipCard(heldStack, player, Hand.MAIN_HAND);
            
            // The flipCard method already updates the player's hand, so we don't need to do anything else
            PCReference.LOGGER.debug("Card flipped for player: {}", player.getName().getString());
        }
    }
}
