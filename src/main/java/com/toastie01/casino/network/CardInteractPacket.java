package com.toastie01.casino.network;

import com.toastie01.casino.PCReference;
import com.toastie01.casino.item.ItemCard;
import com.toastie01.casino.item.ItemCardCovered;
import com.toastie01.casino.item.ItemCardDeck;
import com.toastie01.casino.util.ItemHelper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

/**
 * Network packet for handling card interactions
 */
public class CardInteractPacket {
    
    public static PacketByteBuf create(String command) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(command, 11);
        return buf;
    }
    
    public static void handle(MinecraftServer server, ServerPlayerEntity player, 
                             ServerPlayNetworkHandler handler, PacketByteBuf buf, 
                             PacketSender responseSender) {
        
        String command = buf.readString(11).trim();
        
        server.execute(() -> {
            try {
                if ("flipinv".equalsIgnoreCase(command)) {
                    handleCardFlip(player);
                } else if ("flipdeck".equalsIgnoreCase(command)) {
                    handleDeckFlip(player);
                } else if ("flipcard".equalsIgnoreCase(command)) {
                    handleItemCardFlip(player);
                }
            } catch (Exception e) {
                PCReference.LOGGER.error("Error handling card interact packet: {}", e.getMessage());
            }
        });
    }
    
    private static void handleItemCardFlip(ServerPlayerEntity player) {
        ItemStack heldStack = player.getMainHandStack();
        
        if (heldStack.getItem() instanceof ItemCard itemCard) {
            itemCard.flipCard(heldStack, player, Hand.MAIN_HAND);
            PCReference.LOGGER.debug("ItemCard flipped for player: {}", player.getName().getString());
        }
    }
    
    private static void handleCardFlip(ServerPlayerEntity player) {
        ItemStack heldStack = player.getMainHandStack();
        
        if (heldStack.getItem() instanceof ItemCardCovered cardCovered) {
            cardCovered.flipCard(heldStack, player, Hand.MAIN_HAND);
            PCReference.LOGGER.debug("Card flipped for player: {}", player.getName().getString());
        }
    }
    
    private static void handleDeckFlip(ServerPlayerEntity player) {
        ItemStack heldStack = player.getMainHandStack();
        
        if (heldStack.getItem() instanceof ItemCardDeck) {
            // Flip deck face-up/face-down mode
            NbtCompound nbt = ItemHelper.getNBT(heldStack);
            byte skinId = nbt.getByte("SkinID");
            boolean currentFaceUp = nbt.getBoolean("FaceUp");
            
            // Create new deck with opposite face mode
            ItemStack newDeck = ItemCardDeck.createDeck(skinId, !currentFaceUp);
            player.setStackInHand(Hand.MAIN_HAND, newDeck);
            
            // Send feedback message to player
            String faceMode = !currentFaceUp ? "Face Up" : "Face Down";
            player.sendMessage(Text.literal("Deck flipped to " + faceMode).formatted(Formatting.GREEN), true);
            
            PCReference.LOGGER.debug("Deck flipped for player: {}", player.getName().getString());
        }
    }
}
