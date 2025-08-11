package com.toastie01.casino.network;

import com.toastie01.casino.PCReference;
import com.toastie01.casino.entity.EntityCardDeck;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Network packet for handling long-range deck interactions
 */
public class LongRangeDeckInteractPacket {
    
    public static PacketByteBuf create(int entityId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entityId);
        return buf;
    }
    
    public static void handle(MinecraftServer server, ServerPlayerEntity player, 
                             ServerPlayNetworkHandler handler, PacketByteBuf buf, 
                             PacketSender responseSender) {
        
        int entityId = buf.readInt();
        
        server.execute(() -> {
            try {
                handleLongRangeDeckInteraction(player, entityId);
            } catch (Exception e) {
                PCReference.LOGGER.error("Error handling long-range deck interact packet: {}", e.getMessage());
            }
        });
    }
    
    private static void handleLongRangeDeckInteraction(ServerPlayerEntity player, int entityId) {
        // Find the entity by ID
        Entity entity = player.getWorld().getEntityById(entityId);
        
        if (!(entity instanceof EntityCardDeck deck)) {
            PCReference.LOGGER.warn("Long-range interaction attempted on non-deck entity: {}", entityId);
            return;
        }
        
        // Check distance - must be between 4 and 10 blocks
        double distance = player.distanceTo(deck);
        if (distance <= 4.0 || distance > 10.0) {
            return; // Silently fail if out of range (no message spam)
        }
        
        ItemStack mainHandStack = player.getMainHandStack();
        boolean holdingCard = mainHandStack.getItem() instanceof com.toastie01.casino.item.ItemCard || 
                              mainHandStack.getItem() instanceof com.toastie01.casino.item.ItemCardCovered;
        
        // Check if player's main hand is empty (required for drawing) OR holding a card (for putting back)
        if (!player.isSneaking() && !mainHandStack.isEmpty() && !holdingCard) {
            player.sendMessage(Text.literal("Empty your main hand to draw cards from distance!")
                .formatted(Formatting.YELLOW), true);
            return;
        }
        
        // Check if deck has cards (only required for drawing, not for putting cards back or collecting)
        if (!player.isSneaking() && !holdingCard && deck.getStackAmount() <= 0) {
            player.sendMessage(Text.literal("The deck is empty!")
                .formatted(Formatting.RED), true);
            return;
        }
        
        // Perform the long-range interaction based on whether player is crouching
        synchronized (deck) {
            // Trigger hand swing animation for visual feedback
            player.swingHand(net.minecraft.util.Hand.MAIN_HAND, true);
            
            if (player.isSneaking()) {
                // Crouch + Right-click: Collect all cards belonging to this deck from the entire server
                PCReference.LOGGER.info("Long-range deck collection: Player {} collecting cards from deck at distance {}", 
                    player.getName().getString(), String.format("%.1f", distance));
                
                // Call the manual collection method
                deck.manualCollectCards(player);
            } else if (holdingCard) {
                // Regular right-click while holding a card: Put card back into deck
                PCReference.LOGGER.info("Long-range card return: Player {} putting card back into deck at distance {}", 
                    player.getName().getString(), String.format("%.1f", distance));
                
                // Call the deck's interact method which handles card addition
                deck.interact(player, net.minecraft.util.Hand.MAIN_HAND);
            } else {
                // Regular right-click with empty hand: Draw a card
                if (deck.getStackAmount() > 0) {
                    PCReference.LOGGER.info("Long-range deck interaction: Player {} drawing from deck at distance {}", 
                        player.getName().getString(), String.format("%.1f", distance));
                    
                    // Call the deck's draw method directly
                    deck.interact(player, net.minecraft.util.Hand.MAIN_HAND);
                } else {
                    player.sendMessage(Text.literal("The deck is empty!")
                        .formatted(Formatting.RED), true);
                }
            }
        }
    }
}
