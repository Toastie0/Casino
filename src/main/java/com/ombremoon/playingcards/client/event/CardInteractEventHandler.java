package com.ombremoon.playingcards.client.event;

import com.ombremoon.playingcards.item.ItemCardCovered;
import com.ombremoon.playingcards.network.ModNetworking;
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
                
                if (heldStack.getItem() instanceof ItemCardCovered) {
                    // Send flip command to server
                    ModNetworking.sendCardInteractToServer("flipinv");
                }
            }
        } else if (!currentlyPressed) {
            leftClickPressed = false;
        }
    }
}
