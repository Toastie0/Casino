package com.ombremoon.playingcards.util;

import com.ombremoon.playingcards.economy.EconomyManager;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.item.ItemPokerChip;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Utility class for selling casino items (chips, decks, dice) in the casino mod.
 * Provides unified selling logic for both GUI and shift-click interactions.
 */
public class SellUtils {
    
    // Default sell prices for non-chip items
    private static final double CARD_DECK_SELL_PRICE = 50.0;
    private static final double DICE_SELL_PRICE = 10.0;
    
    /**
     * Result of a selling operation.
     */
    public static class SellResult {
        public final double totalValue;
        public final int chipsSold;
        public final int decksSold;
        public final int diceSold;
        public final boolean success;
        
        public SellResult(double totalValue, int chipsSold, int decksSold, int diceSold) {
            this.totalValue = totalValue;
            this.chipsSold = chipsSold;
            this.decksSold = decksSold;
            this.diceSold = diceSold;
            this.success = chipsSold > 0 || decksSold > 0 || diceSold > 0;
        }
        
        public String getResultMessage() {
            if (!success) {
                return "No sellable casino items found in inventory!";
            }
            
            StringBuilder message = new StringBuilder("Sold ");
            boolean first = true;
            
            if (chipsSold > 0) {
                message.append(chipsSold).append(" chip").append(chipsSold == 1 ? "" : "s");
                first = false;
            }
            
            if (decksSold > 0) {
                if (!first) message.append(", ");
                message.append(decksSold).append(" deck").append(decksSold == 1 ? "" : "s");
                first = false;
            }
            
            if (diceSold > 0) {
                if (!first) message.append(", ");
                message.append(diceSold).append(" dice");
            }
            
            message.append(" for $").append(String.format("%.2f", totalValue));
            return message.toString();
        }
    }
    
    /**
     * Sells all casino items in the player's inventory.
     */
    public static SellResult sellAllCasinoItems(ServerPlayerEntity player) {
        double totalValue = 0.0;
        int chipsSold = 0;
        int decksSold = 0;
        int diceSold = 0;
        
        // Check entire inventory for sellable casino items
        for (int i = 0; i < player.getInventory().main.size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            
            if (stack.isEmpty()) continue;
            
            // Sell poker chips
            if (stack.getItem() instanceof ItemPokerChip pokerChip) {
                double value = pokerChip.getValue() * stack.getCount();
                totalValue += value;
                chipsSold += stack.getCount();
                player.getInventory().setStack(i, ItemStack.EMPTY);
            }
            // Sell card decks
            else if (stack.getItem() == ModItems.CARD_DECK) {
                double value = CARD_DECK_SELL_PRICE * stack.getCount();
                totalValue += value;
                decksSold += stack.getCount();
                player.getInventory().setStack(i, ItemStack.EMPTY);
            }
            // Sell dice (all types)
            else if (stack.getItem() == ModItems.FANTASY_DICE || 
                     stack.getItem() == ModItems.SIMPLE_DICE || 
                     stack.getItem() == ModItems.GUI_DICE) {
                double value = DICE_SELL_PRICE * stack.getCount();
                totalValue += value;
                diceSold += stack.getCount();
                player.getInventory().setStack(i, ItemStack.EMPTY);
            }
        }
        
        SellResult result = new SellResult(totalValue, chipsSold, decksSold, diceSold);
        
        if (result.success) {
            EconomyManager.deposit(player, totalValue);
            EconomyManager.sendEconomyMessage(player, result.getResultMessage());
        } else {
            player.sendMessage(Text.literal(result.getResultMessage()).formatted(Formatting.RED), false);
        }
        
        return result;
    }
    
    /**
     * Sells a specific item stack and returns the value.
     */
    public static double sellItemStack(ServerPlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) return 0.0;
        
        double value = 0.0;
        String itemName = "";
        
        // Calculate sell value based on item type
        if (stack.getItem() instanceof ItemPokerChip pokerChip) {
            value = pokerChip.getValue() * stack.getCount();
            itemName = "poker chip" + (stack.getCount() == 1 ? "" : "s");
        } else if (stack.getItem() == ModItems.CARD_DECK) {
            value = CARD_DECK_SELL_PRICE * stack.getCount();
            itemName = "card deck" + (stack.getCount() == 1 ? "" : "s");
        } else if (stack.getItem() == ModItems.FANTASY_DICE || 
                   stack.getItem() == ModItems.SIMPLE_DICE || 
                   stack.getItem() == ModItems.GUI_DICE) {
            value = DICE_SELL_PRICE * stack.getCount();
            itemName = "dice";
        } else {
            // Not a sellable casino item
            return 0.0;
        }
        
        // Process the sale
        EconomyManager.deposit(player, value);
        EconomyManager.sendEconomyMessage(player, 
            String.format("Sold %d %s for $%.2f", stack.getCount(), itemName, value));
        
        return value;
    }
    
    /**
     * Checks if an item is sellable in the casino.
     */
    public static boolean isCasinoItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        return stack.getItem() instanceof ItemPokerChip ||
               stack.getItem() == ModItems.CARD_DECK ||
               stack.getItem() == ModItems.FANTASY_DICE ||
               stack.getItem() == ModItems.SIMPLE_DICE ||
               stack.getItem() == ModItems.GUI_DICE;
    }
    
    /**
     * Gets the sell price for a casino item.
     */
    public static double getSellPrice(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;
        
        if (stack.getItem() instanceof ItemPokerChip pokerChip) {
            return pokerChip.getValue();
        } else if (stack.getItem() == ModItems.CARD_DECK) {
            return CARD_DECK_SELL_PRICE;
        } else if (stack.getItem() == ModItems.FANTASY_DICE ||
                   stack.getItem() == ModItems.SIMPLE_DICE ||
                   stack.getItem() == ModItems.GUI_DICE) {
            return DICE_SELL_PRICE;
        }
        
        return 0.0;
    }
}
