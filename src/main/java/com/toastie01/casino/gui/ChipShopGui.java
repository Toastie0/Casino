package com.toastie01.casino.gui;

import com.toastie01.casino.PCReference;
import com.toastie01.casino.config.CasinoConfig;
import com.toastie01.casino.economy.EconomyManager;
import com.toastie01.casino.init.ModItems;
import com.toastie01.casino.item.ItemPokerChip;
import com.toastie01.casino.util.SellUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * GUI for the Casino chip shop where players can buy poker chips and card decks.
 * Supports both single purchases and bulk purchases (except for decks).
 * Integrates with the economy system for balance checking and transactions.
 */
public class ChipShopGui extends SimpleGui {
    
    public ChipShopGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.setTitle(Text.literal(CasinoConfig.getInstance().shopGuiTitle).formatted(Formatting.GOLD, Formatting.BOLD));
        this.setupGui();
    }
    
    /**
     * Gets the configured price for a chip color.
     */
    private double getChipPrice(String color) {
        return CasinoConfig.getInstance().getChipValue(color);
    }
    
    /**
     * Sets up the GUI layout with poker chips, card decks, and decorative elements.
     */
    private void setupGui() {
        // Row 1: All poker chips (0-8)
        // Slot 0: White Chip
        double whitePrice = getChipPrice("white");
        this.setSlot(0, new GuiElementBuilder(ModItems.WHITE_POKER_CHIP)
                .setName(Text.literal("White Poker Chip").formatted(Formatting.WHITE))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(whitePrice) : 1;
                    buyChip(ModItems.WHITE_POKER_CHIP, whitePrice, amount, "white");
                })
                .build());
        
        // Slot 1: Red Chip
        double redPrice = getChipPrice("red");
        this.setSlot(1, new GuiElementBuilder(ModItems.RED_POKER_CHIP)
                .setName(Text.literal("Red Poker Chip").formatted(Formatting.RED))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(redPrice) : 1;
                    buyChip(ModItems.RED_POKER_CHIP, redPrice, amount, "red");
                })
                .build());
        
        // Slot 2: Green Chip
        double greenPrice = getChipPrice("green");
        this.setSlot(2, new GuiElementBuilder(ModItems.GREEN_POKER_CHIP)
                .setName(Text.literal("Green Poker Chip").formatted(Formatting.GREEN))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(greenPrice) : 1;
                    buyChip(ModItems.GREEN_POKER_CHIP, greenPrice, amount, "green");
                })
                .build());
        
        // Slot 3: Blue Chip
        double bluePrice = getChipPrice("blue");
        this.setSlot(3, new GuiElementBuilder(ModItems.BLUE_POKER_CHIP)
                .setName(Text.literal("Blue Poker Chip").formatted(Formatting.BLUE))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(bluePrice) : 1;
                    buyChip(ModItems.BLUE_POKER_CHIP, bluePrice, amount, "blue");
                })
                .build());
        
        // Slot 4: Black Chip
        double blackPrice = getChipPrice("black");
        this.setSlot(4, new GuiElementBuilder(ModItems.BLACK_POKER_CHIP)
                .setName(Text.literal("Black Poker Chip").formatted(Formatting.DARK_GRAY))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(blackPrice) : 1;
                    buyChip(ModItems.BLACK_POKER_CHIP, blackPrice, amount, "black");
                })
                .build());
        
        // Slot 5: Purple Chip
        double purplePrice = getChipPrice("purple");
        this.setSlot(5, new GuiElementBuilder(ModItems.PURPLE_POKER_CHIP)
                .setName(Text.literal("Purple Poker Chip").formatted(Formatting.DARK_PURPLE))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(purplePrice) : 1;
                    buyChip(ModItems.PURPLE_POKER_CHIP, purplePrice, amount, "purple");
                })
                .build());
        
        // Slot 6: Yellow Chip
        double yellowPrice = getChipPrice("yellow");
        this.setSlot(6, new GuiElementBuilder(ModItems.YELLOW_POKER_CHIP)
                .setName(Text.literal("Yellow Poker Chip").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(yellowPrice) : 1;
                    buyChip(ModItems.YELLOW_POKER_CHIP, yellowPrice, amount, "yellow");
                })
                .build());
        
        // Slot 7: Pink Chip
        double pinkPrice = getChipPrice("pink");
        this.setSlot(7, new GuiElementBuilder(ModItems.PINK_POKER_CHIP)
                .setName(Text.literal("Pink Poker Chip").formatted(Formatting.LIGHT_PURPLE))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(pinkPrice) : 1;
                    buyChip(ModItems.PINK_POKER_CHIP, pinkPrice, amount, "pink");
                })
                .build());
        
        // Slot 8: Orange Chip
        double orangePrice = getChipPrice("orange");
        this.setSlot(8, new GuiElementBuilder(ModItems.ORANGE_POKER_CHIP)
                .setName(Text.literal("Orange Poker Chip").formatted(Formatting.GOLD))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(orangePrice) : 1;
                    buyChip(ModItems.ORANGE_POKER_CHIP, orangePrice, amount, "orange");
                })
                .build());
        
        // Row 2: Card Decks (9-12) + Glass Panes (13-17)
        // Note: Card deck visuals will be set up by updateCardDeckVisuals() method
        
        // Slots 13-17: Glass Panes
        for (int i = 13; i <= 17; i++) {
            this.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
        
        // Row 3: Sell Slot + Glass Panes + Balance + Close
        // Slot 18: Sell Slot
        this.setSlot(18, new GuiElementBuilder(Items.HOPPER)
                .setName(Text.literal("Sell All Casino Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .setCallback((index, type, action, gui) -> {
                    // This callback is handled in onAnyClick method
                })
                .build());
        
        // Slots 19-24: Glass Panes
        for (int i = 19; i <= 24; i++) {
            this.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
        
        // Slot 19: Balance Display (moved next to hopper)
        this.setSlot(19, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Your Balance").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("$" + String.format("%.2f", EconomyManager.getBalance(this.player))).formatted(Formatting.GREEN))
                .build());
        
        // Slot 25: Back Button (moved one to the left)
        this.setSlot(25, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    gui.close();
                    CasinoMainGui casinoGui = new CasinoMainGui(this.getPlayer());
                    casinoGui.open();
                })
                .build());
        
        // Slot 26: Close Button (moved one down)
        this.setSlot(26, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback((index, type, action, gui) -> {
                    gui.close();
                })
                .build());
        
        // Set the visual card deck items with proper NBT data
        this.updateCardDeckVisuals();
    }
    
    /**
     * Update the visual appearance of card decks in the GUI to show proper skin textures
     */
    private void updateCardDeckVisuals() {
        // Set the visual items in the slots using ModItems.CARD_DECK with CustomModelData
        this.setSlot(9, new GuiElementBuilder(ModItems.CARD_DECK)
                .setName(Text.literal("Blue Card Deck").formatted(Formatting.BLUE))
                .glow()
                .setCustomModelData(PCReference.CardSkins.BLUE)
                .setCallback((index, type, action, gui) -> {
                    buyCardDeck(1, PCReference.CardSkins.BLUE, "Blue");
                })
                .build());
        
        this.setSlot(10, new GuiElementBuilder(ModItems.CARD_DECK)
                .setName(Text.literal("Red Card Deck").formatted(Formatting.RED))
                .glow()
                .setCustomModelData(PCReference.CardSkins.RED)
                .setCallback((index, type, action, gui) -> {
                    buyCardDeck(1, PCReference.CardSkins.RED, "Red");
                })
                .build());
        
        this.setSlot(11, new GuiElementBuilder(ModItems.CARD_DECK)
                .setName(Text.literal("Black Card Deck").formatted(Formatting.DARK_GRAY))
                .glow()
                .setCustomModelData(PCReference.CardSkins.BLACK)
                .setCallback((index, type, action, gui) -> {
                    buyCardDeck(1, PCReference.CardSkins.BLACK, "Black");
                })
                .build());
        
        this.setSlot(12, new GuiElementBuilder(ModItems.CARD_DECK)
                .setName(Text.literal("Pig Card Deck").formatted(Formatting.LIGHT_PURPLE))
                .glow()
                .setCustomModelData(PCReference.CardSkins.PIG)
                .setCallback((index, type, action, gui) -> {
                    buyCardDeck(1, PCReference.CardSkins.PIG, "Pig");
                })
                .build());
    }
    
    private void buyChip(Item chipItem, double pricePerChip, int amount, String chipColor) {
        double cost = amount * pricePerChip;
        
        if (EconomyManager.hasBalance(this.player, cost)) {
            if (EconomyManager.withdraw(this.player, cost)) {
                // Give the chips to the player
                ItemStack chipStack = new ItemStack(chipItem, amount);
                if (!this.player.getInventory().insertStack(chipStack)) {
                    this.player.dropItem(chipStack, false);
                }
                
                // Play success sound
                this.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                
                // Send success message with natural formatting
                String message = formatPurchaseMessage(amount, chipColor + " chip", cost);
                this.player.sendMessage(Text.literal(message).formatted(Formatting.GREEN), false);
                
                // Update the balance display
                this.updateBalance();
            }
        } else {
            // Play error sound
            this.player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 2.0f);
            this.player.sendMessage(Text.literal("Insufficient funds!").formatted(Formatting.RED), false);
        }
    }
    
    private void buyCardDeck(int amount, byte skinId, String colorName) {
        double pricePerDeck = 50.0;
        double cost = amount * pricePerDeck;
        
        if (EconomyManager.hasBalance(this.player, cost)) {
            if (EconomyManager.withdraw(this.player, cost)) {
                // Give the card decks to the player
                for (int i = 0; i < amount; i++) {
                    // Create deck with specific skin
                    ItemStack deckStack = com.toastie01.casino.item.ItemCardDeck.createDeck(skinId);
                    
                    if (!this.player.getInventory().insertStack(deckStack)) {
                        this.player.dropItem(deckStack, false);
                    }
                }
                
                // Play success sound
                this.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                
                // Send success message with natural formatting
                String message = formatPurchaseMessage(amount, colorName.toLowerCase() + " card deck", cost);
                this.player.sendMessage(Text.literal(message).formatted(Formatting.GREEN), false);
                
                // Update balance display
                this.updateBalance();
            } else {
                // Play error sound
                this.player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 2.0f);
                this.player.sendMessage(Text.literal("Transaction failed!").formatted(Formatting.RED), false);
            }
        } else {
            // Play error sound
            this.player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 2.0f);
            this.player.sendMessage(Text.literal("Insufficient funds!").formatted(Formatting.RED), false);
        }
    }
    
    private int getMaxAffordable(double pricePerChip) {
        double balance = EconomyManager.getBalance(this.player);
        int maxAmount = (int) (balance / pricePerChip);
        return Math.min(maxAmount, 25); // Cap at 25 (max stack size)
    }
    
    private void sellChips(ItemStack itemStack) {
        // Check if it's a sellable item (poker chip or card deck)
        if (!(itemStack.getItem() instanceof ItemPokerChip) && 
            itemStack.getItem() != ModItems.CARD_DECK) {
            return;
        }
        
        double pricePerItem = getSellPrice(itemStack);
        if (pricePerItem == 0) {
            return;
        }
        
        int amount = itemStack.getCount();
        double totalValue = amount * pricePerItem;
        
        if (EconomyManager.deposit(this.player, totalValue)) {
            // Remove items from stack
            itemStack.setCount(0);
            
            // Play success sound
            this.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.2f);
            
            // Send success message with natural formatting
            String message = formatIndividualSellMessage(itemStack, amount, totalValue);
            this.player.sendMessage(Text.literal(message).formatted(Formatting.GREEN), false);
            
            // Update the balance display
            this.updateBalance();
        }
    }
    
    private double getSellPrice(ItemStack itemStack) {
        Item item = itemStack.getItem();
        
        // Poker chips sell for same price as bought (get from config)
        if (item instanceof ItemPokerChip pokerChip) {
            return pokerChip.getValue();
        }
        
        // Other items sell for same price as bought
        if (item == ModItems.CARD_DECK) return PCReference.CARD_DECK_SELL_PRICE;
        
        return 0.0;
    }
    
    private String getItemName(ItemStack itemStack) {
        Item item = itemStack.getItem();
        String plural = itemStack.getCount() > 1 ? "s" : "";
        
        // Handle poker chips using the existing getColorName method
        if (item instanceof ItemPokerChip pokerChip) {
            return pokerChip.getColorName() + " poker chip" + plural;
        }
        
        if (item == ModItems.CARD_DECK) {
            return "card deck" + plural;
        }
        
        return "item" + plural;
    }
    
    /**
     * Formats a natural-sounding message for individual item sales.
     * Examples: "Sold 1 white chip for $1", "Sold 5 red chips for $25"
     */
    private String formatIndividualSellMessage(ItemStack itemStack, int amount, double totalValue) {
        String itemName = getItemName(itemStack);
        return String.format("Sold %d %s for $%.0f", amount, itemName, totalValue);
    }
    
    /**
     * Formats a natural-sounding message for purchases.
     * Examples: "Purchased 1 white chip for $1.00", "Purchased 5 red chips for $25.00"
     */
    private String formatPurchaseMessage(int amount, String itemName, double totalCost) {
        String plural = amount == 1 ? "" : "s";
        return String.format("Purchased %d %s%s for $%.2f", amount, itemName, plural, totalCost);
    }
    
    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        // Handle selling when items are dragged/dropped into sell slot (18)
        if (index == 18) {
            ItemStack cursorStack = this.player.currentScreenHandler.getCursorStack();
            
            // If player has items in cursor (drag-and-drop), sell those items
            if (!cursorStack.isEmpty() && (cursorStack.getItem() instanceof ItemPokerChip || 
                cursorStack.getItem() == ModItems.CARD_DECK)) {
                
                // Handle different drop actions
                if (action == SlotActionType.PICKUP || action == SlotActionType.QUICK_MOVE || 
                    action == SlotActionType.THROW || action == SlotActionType.SWAP) {
                    sellChips(cursorStack);
                    return true;
                }
            }
            // If player clicks hopper without items in cursor, sell all
            else if (cursorStack.isEmpty() && type == ClickType.MOUSE_LEFT && action == SlotActionType.PICKUP) {
                sellAllCasinoItems();
                return true;
            }
        }
        
        // Handle shift-clicking chips from player inventory to auto-sell
        if (type.shift && action == SlotActionType.QUICK_MOVE) {
            // For shift-click, try to get the stack from the slot that was clicked
            try {
                ItemStack clickedStack = this.player.currentScreenHandler.getSlot(index).getStack();
                if (!clickedStack.isEmpty() && (clickedStack.getItem() instanceof ItemPokerChip || 
                    clickedStack.getItem() == ModItems.CARD_DECK)) {
                    // Check if it's from player inventory (slots 27+ in a 3-row GUI)
                    if (index >= 27) {
                        sellChips(clickedStack);
                        return true;
                    }
                }
            } catch (Exception e) {
                // If slot access fails, ignore
            }
        }
        
        return super.onAnyClick(index, type, action);
    }
    
    private void updateBalance() {
        double balance = EconomyManager.getBalance(this.player);
        this.setSlot(19, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Your Balance").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("$" + String.format("%.2f", balance)).formatted(Formatting.GREEN))
                .build());
    }
    
    /**
     * Sells all casino items in the player's inventory.
     */
    private void sellAllCasinoItems() {
        SellUtils.SellResult result = SellUtils.sellAllCasinoItems(this.player);
        
        if (result.success) {
            // Refresh GUI to update balance
            this.updateBalance();
        }
    }
}
