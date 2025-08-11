package com.toastie01.casino.util;

import com.toastie01.casino.PCReference;
import com.toastie01.casino.economy.EconomyManager;
import com.toastie01.casino.init.ModItems;
import com.toastie01.casino.item.ItemPokerChip;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Utility class for selling casino items (chips and decks) in the casino mod.
 * Provides unified selling logic for both GUI and shift-click interactions.
 */
public class SellUtils {
    
    /**
     * Result of a selling operation.
     */
    public static class SellResult {
        public final double totalValue;
        public final int chipsSold;
        public final int decksSold;
        public final boolean success;
        
        public SellResult(double totalValue, int chipsSold, int decksSold) {
            this.totalValue = totalValue;
            this.chipsSold = chipsSold;
            this.decksSold = decksSold;
            this.success = chipsSold > 0 || decksSold > 0;
        }
    }
    
    /**
     * Sells all casino items in the player's inventory.
     */
    public static SellResult sellAllCasinoItems(ServerPlayerEntity player) {
        double totalValue = 0.0;
        int chipsSold = 0;
        int decksSold = 0;
        
        // Iterate through player's inventory
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            
            if (!stack.isEmpty()) {
                // Check if it's a casino item
                if (stack.getItem() instanceof ItemPokerChip pokerChip) {
                    // It's a poker chip - get value from the chip itself
                    double value = pokerChip.getValue() * stack.getCount();
                    totalValue += value;
                    chipsSold += stack.getCount();
                    
                    // Remove the item
                    player.getInventory().setStack(i, ItemStack.EMPTY);
                    
                } else if (stack.getItem() == ModItems.CARD_DECK) {
                    // It's a card deck - use centralized price
                    double value = PCReference.CARD_DECK_SELL_PRICE * stack.getCount();
                    totalValue += value;
                    decksSold += stack.getCount();
                    
                    // Remove the item
                    player.getInventory().setStack(i, ItemStack.EMPTY);
                }
            }
        }
        
        // Add money to player's account
        if (totalValue > 0) {
            try {
                boolean success = EconomyManager.deposit(player, totalValue);
                if (success) {
                    // Send success message with natural formatting
                    String message = formatSellMessage(chipsSold, decksSold, totalValue);
                    player.sendMessage(Text.literal(message).formatted(Formatting.GREEN), false);
                } else {
                    // Failed to deposit - put items back (basic recovery)
                    PCReference.LOGGER.error("Failed to deposit {} to player {}", totalValue, player.getName().getString());
                    player.sendMessage(Text.literal("Transaction failed! Please try again.")
                        .formatted(Formatting.RED), false);
                    return new SellResult(0, 0, 0);
                }
            } catch (Exception e) {
                PCReference.LOGGER.error("Error during sell transaction for player {}: {}", 
                    player.getName().getString(), e.getMessage());
                player.sendMessage(Text.literal("Transaction error! Please try again.")
                    .formatted(Formatting.RED), false);
                return new SellResult(0, 0, 0);
            }
        } else {
            // Send no items message
            player.sendMessage(Text.literal("No casino items found to sell!")
                .formatted(Formatting.RED), false);
        }
        
        return new SellResult(totalValue, chipsSold, decksSold);
    }
    
    /**
     * Sells a single ItemStack and returns the value.
     */
    public static SellResult sellIndividualItem(ServerPlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) {
            return new SellResult(0, 0, 0);
        }
        
        double value = 0;
        int chipsSold = 0;
        int decksSold = 0;
        
        if (stack.getItem() instanceof ItemPokerChip pokerChip) {
            value = pokerChip.getValue() * stack.getCount();
            chipsSold = stack.getCount();
        } else if (stack.getItem() == ModItems.CARD_DECK) {
            value = PCReference.CARD_DECK_SELL_PRICE * stack.getCount();
            decksSold = stack.getCount();
        }
        
        if (value > 0) {
            EconomyManager.deposit(player, value);
            return new SellResult(value, chipsSold, decksSold);
        }
        
        return new SellResult(0, 0, 0);
    }
    
    /**
     * Gets the sell price for a single item (used for display purposes).
     */
    public static double getItemSellPrice(ItemStack stack) {
        if (stack.getItem() instanceof ItemPokerChip pokerChip) {
            return pokerChip.getValue();
        } else if (stack.getItem() == ModItems.CARD_DECK) {
            return PCReference.CARD_DECK_SELL_PRICE;
        }
        return 0.0;
    }
    
    /**
     * Formats a natural-sounding sell message that only mentions items actually sold.
     * Examples:
     * - "Sold 5 chips for $25"
     * - "Sold 2 decks for $100" 
     * - "Sold 3 chips and 1 deck for $78"
     * - "Sold 2 chips and 3 decks for $156"
     */
    private static String formatSellMessage(int chipsSold, int decksSold, double totalValue) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        
        if (chipsSold > 0) {
            parts.add(chipsSold + (chipsSold == 1 ? " chip" : " chips"));
        }
        
        if (decksSold > 0) {
            parts.add(decksSold + (decksSold == 1 ? " deck" : " decks"));
        }
        
        String itemsText;
        if (parts.size() == 1) {
            itemsText = parts.get(0);
        } else if (parts.size() == 2) {
            itemsText = parts.get(0) + " and " + parts.get(1);
        } else {
            // Future-proof for more item types
            itemsText = String.join(", ", parts.subList(0, parts.size() - 1)) + " and " + parts.get(parts.size() - 1);
        }
        
        return String.format("Sold %s for $%.0f", itemsText, totalValue);
    }
}
