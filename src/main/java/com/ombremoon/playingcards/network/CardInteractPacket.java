package com.ombremoon.playingcards.network;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.item.ItemCardCovered;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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
                }
            } catch (Exception e) {
                PCReference.LOGGER.error("Error handling card interact packet: {}", e.getMessage());
            }
        });
    }
    
    private static void handleCardFlip(ServerPlayerEntity player) {
        ItemStack heldStack = player.getMainHandStack();
        
        if (heldStack.getItem() instanceof ItemCardCovered cardCovered) {
            cardCovered.flipCard(heldStack, player, Hand.MAIN_HAND);
            PCReference.LOGGER.debug("Card flipped for player: {}", player.getName().getString());
        }
    }
}
