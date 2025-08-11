package com.toastie01.casino.client.event;

import com.toastie01.casino.item.ItemCard;
import com.toastie01.casino.item.ItemCardCovered;
import com.toastie01.casino.item.ItemCardDeck;
import com.toastie01.casino.network.ModNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class CardInteractEventHandler {
    
    private static boolean leftClickPressed = false;
    
    public static void initialize() {
        // Register client tick event to handle card flipping
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                handleCardFlip(client);
            }
        });
    }
    
    private static void handleCardFlip(MinecraftClient client) {
        // Check if left mouse button is pressed and we're not in a screen
        boolean currentlyPressed = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        
        if (currentlyPressed && !leftClickPressed && client.currentScreen == null) {
            leftClickPressed = true;
            
            PlayerEntity player = client.player;
            if (player != null) {
                ItemStack heldStack = player.getMainHandStack();
                
                // Handle left-click flipping for items in hand
                if (heldStack.getItem() instanceof ItemCardCovered) {
                    // Send flip command for covered cards
                    ModNetworking.sendCardInteractToServer("flipinv");
                } else if (heldStack.getItem() instanceof ItemCard) {
                    // Send flip command for regular cards  
                    ModNetworking.sendCardInteractToServer("flipcard");
                } else if (heldStack.getItem() instanceof ItemCardDeck) {
                    // Send flip command for card decks in hand
                    ModNetworking.sendCardInteractToServer("flipdeck");
                }
            }
        } else if (!currentlyPressed) {
            leftClickPressed = false;
        }
    }
}
