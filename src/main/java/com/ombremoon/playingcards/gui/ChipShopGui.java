package com.ombremoon.playingcards.gui;

import com.ombremoon.playingcards.config.CasinoConfig;
import com.ombremoon.playingcards.economy.EconomyManager;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.item.ItemPokerChip;
import com.ombremoon.playingcards.item.ItemFantasyDice;
import com.ombremoon.playingcards.util.SellUtils;
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
                .addLoreLine(Text.literal("$" + String.format("%.2f", whitePrice) + " each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(whitePrice) : 1;
                    buyChip(ModItems.WHITE_POKER_CHIP, whitePrice, amount, "white");
                })
                .build());
        
        // Slot 1: Red Chip
        double redPrice = getChipPrice("red");
        this.setSlot(1, new GuiElementBuilder(ModItems.RED_POKER_CHIP)
                .setName(Text.literal("Red Poker Chip").formatted(Formatting.RED))
                .addLoreLine(Text.literal("$" + String.format("%.2f", redPrice) + " each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(redPrice) : 1;
                    buyChip(ModItems.RED_POKER_CHIP, redPrice, amount, "red");
                })
                .build());
        
        // Slot 2: Green Chip
        double greenPrice = getChipPrice("green");
        this.setSlot(2, new GuiElementBuilder(ModItems.GREEN_POKER_CHIP)
                .setName(Text.literal("Green Poker Chip").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("$" + String.format("%.2f", greenPrice) + " each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(greenPrice) : 1;
                    buyChip(ModItems.GREEN_POKER_CHIP, greenPrice, amount, "green");
                })
                .build());
        
        // Slot 3: Blue Chip
        double bluePrice = getChipPrice("blue");
        this.setSlot(3, new GuiElementBuilder(ModItems.BLUE_POKER_CHIP)
                .setName(Text.literal("Blue Poker Chip").formatted(Formatting.BLUE))
                .addLoreLine(Text.literal("$" + String.format("%.2f", bluePrice) + " each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(bluePrice) : 1;
                    buyChip(ModItems.BLUE_POKER_CHIP, bluePrice, amount, "blue");
                })
                .build());
        
        // Slot 4: Black Chip
        double blackPrice = getChipPrice("black");
        this.setSlot(4, new GuiElementBuilder(ModItems.BLACK_POKER_CHIP)
                .setName(Text.literal("Black Poker Chip").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("$" + String.format("%.2f", blackPrice) + " each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(blackPrice) : 1;
                    buyChip(ModItems.BLACK_POKER_CHIP, blackPrice, amount, "black");
                })
                .build());
        
        // Slot 5: Purple Chip
        double purplePrice = getChipPrice("purple");
        this.setSlot(5, new GuiElementBuilder(ModItems.PURPLE_POKER_CHIP)
                .setName(Text.literal("Purple Poker Chip").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal("$" + String.format("%.2f", purplePrice) + " each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(purplePrice) : 1;
                    buyChip(ModItems.PURPLE_POKER_CHIP, purplePrice, amount, "purple");
                })
                .build());
        
        // Slot 6: Yellow Chip
        double yellowPrice = getChipPrice("yellow");
        this.setSlot(6, new GuiElementBuilder(ModItems.YELLOW_POKER_CHIP)
                .setName(Text.literal("Yellow Poker Chip").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("$" + String.format("%.2f", yellowPrice) + " each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(yellowPrice) : 1;
                    buyChip(ModItems.YELLOW_POKER_CHIP, yellowPrice, amount, "yellow");
                })
                .build());
        
        // Slot 7: Pink Chip
        double pinkPrice = getChipPrice("pink");
        this.setSlot(7, new GuiElementBuilder(ModItems.PINK_POKER_CHIP)
                .setName(Text.literal("Pink Poker Chip").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("$" + String.format("%.2f", pinkPrice) + " each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(pinkPrice) : 1;
                    buyChip(ModItems.PINK_POKER_CHIP, pinkPrice, amount, "pink");
                })
                .build());
        
        // Slot 8: Orange Chip
        double orangePrice = getChipPrice("orange");
        this.setSlot(8, new GuiElementBuilder(ModItems.ORANGE_POKER_CHIP)
                .setName(Text.literal("Orange Poker Chip").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("$" + String.format("%.2f", orangePrice) + " each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(orangePrice) : 1;
                    buyChip(ModItems.ORANGE_POKER_CHIP, orangePrice, amount, "orange");
                })
                .build());
        
        // Row 2: Card Decks (9-12) + Glass Panes (13-17)
        // Note: Card deck visuals will be set up by updateCardDeckVisuals() method
        
        // Slots 13-16: Glass Panes (moved from 13-17 to make room for dice)
        for (int i = 13; i <= 16; i++) {
            this.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
        
        // Slot 17: Traditional Dice
        this.setSlot(17, new GuiElementBuilder(ModItems.SIMPLE_DICE)
                .setName(Text.literal("Traditional Dice").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Price: $10").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordableSimpleDice(10.0) : 1;
                    buySimpleDice(amount, 10.0);
                })
                .build());
        
        // Row 3: Sell Slot + Glass Panes + Balance + Close
        // Slot 18: Sell Slot
        this.setSlot(18, new GuiElementBuilder(Items.HOPPER)
                .setName(Text.literal("Sell All Casino Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Sell all poker chips, decks,").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("and dice in your inventory").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to sell all").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    sellAllCasinoItems();
                })
                .build());
        
        // Slots 19-24: Glass Panes
        for (int i = 19; i <= 24; i++) {
            this.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
        
        // Slot 25: Balance Display
        this.setSlot(25, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Your Balance").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("$" + String.format("%.2f", EconomyManager.getBalance(this.player))).formatted(Formatting.GREEN))
                .build());
        
        // Slot 26: Back Button
        this.setSlot(26, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Return to Casino Management").formatted(Formatting.GRAY))
                .setCallback((index, type, action, gui) -> {
                    gui.close();
                    CasinoMainGui casinoGui = new CasinoMainGui(this.getPlayer());
                    casinoGui.open();
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
                .setName(Text.literal("Classic Blue Card Deck").formatted(Formatting.BLUE))
                .addLoreLine(Text.literal("$50.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Contains 52 cards").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to buy 1 deck").formatted(Formatting.YELLOW))
                .glow()
                .setCustomModelData(0)
                .setCallback((index, type, action, gui) -> {
                    buyCardDeck(1, (byte) 0, "Classic Blue");
                })
                .build());
        
        this.setSlot(10, new GuiElementBuilder(ModItems.CARD_DECK)
                .setName(Text.literal("Classic Red Card Deck").formatted(Formatting.RED))
                .addLoreLine(Text.literal("$50.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Contains 52 cards").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to buy 1 deck").formatted(Formatting.YELLOW))
                .glow()
                .setCustomModelData(1)
                .setCallback((index, type, action, gui) -> {
                    buyCardDeck(1, (byte) 1, "Classic Red");
                })
                .build());
        
        this.setSlot(11, new GuiElementBuilder(ModItems.CARD_DECK)
                .setName(Text.literal("Classic Black Card Deck").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("$50.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Contains 52 cards").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to buy 1 deck").formatted(Formatting.YELLOW))
                .glow()
                .setCustomModelData(2)
                .setCallback((index, type, action, gui) -> {
                    buyCardDeck(1, (byte) 2, "Classic Black");
                })
                .build());
        
        this.setSlot(12, new GuiElementBuilder(ModItems.CARD_DECK)
                .setName(Text.literal("Pig Card Deck").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("$50.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Contains 52 cards").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to buy 1 deck").formatted(Formatting.YELLOW))
                .glow()
                .setCustomModelData(3)
                .setCallback((index, type, action, gui) -> {
                    buyCardDeck(1, (byte) 3, "Pig");
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
                
                // Send success message
                EconomyManager.sendEconomyMessage(this.player, 
                    String.format("Purchased %d %s chip%s for $%.2f", 
                        amount, chipColor, amount == 1 ? "" : "s", cost));
                
                // Update the balance display
                this.updateBalance();
            }
        } else {
            // Play error sound
            this.player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 2.0f);
            EconomyManager.sendErrorMessage(this.player, "Insufficient funds!");
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
                    ItemStack deckStack = com.ombremoon.playingcards.item.ItemCardDeck.createDeck(skinId);
                    
                    if (!this.player.getInventory().insertStack(deckStack)) {
                        this.player.dropItem(deckStack, false);
                    }
                }
                
                // Play success sound
                this.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                
                // Send confirmation message
                this.player.sendMessage(Text.literal("Bought " + amount + " " + colorName + " card deck" + (amount > 1 ? "s" : "") + " for $" + String.format("%.2f", cost)).formatted(Formatting.GREEN), false);
                
                // Update balance display
                this.updateBalance();
            } else {
                this.player.sendMessage(Text.literal("Transaction failed!").formatted(Formatting.RED), false);
                // Play error sound
                this.player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 2.0f);
            }
        } else {
            this.player.sendMessage(Text.literal("You don't have enough money! Cost: $" + String.format("%.2f", cost)).formatted(Formatting.RED), false);
            // Play error sound
            this.player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 2.0f);
            EconomyManager.sendErrorMessage(this.player, "Insufficient funds!");
        }
    }
    
    private int getMaxAffordable(double pricePerChip) {
        double balance = EconomyManager.getBalance(this.player);
        int maxAmount = (int) (balance / pricePerChip);
        return Math.min(maxAmount, 25); // Cap at 25 (max stack size)
    }
    
    private void sellChips(ItemStack itemStack) {
        // Check if it's a sellable item (poker chip, card deck, simple dice, or fantasy dice)
        if (!(itemStack.getItem() instanceof ItemPokerChip) && 
            itemStack.getItem() != ModItems.CARD_DECK && 
            itemStack.getItem() != ModItems.SIMPLE_DICE &&
            itemStack.getItem() != ModItems.FANTASY_DICE) {
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
            
            // Send success message
            String itemName = getItemName(itemStack);
            EconomyManager.sendEconomyMessage(this.player, 
                String.format("Sold %d %s for $%.0f", amount, itemName, totalValue));
            
            // Update the balance display
            this.updateBalance();
        }
    }
    
    private double getSellPrice(ItemStack itemStack) {
        Item item = itemStack.getItem();
        
        // Poker chips sell for same price as bought
        if (item == ModItems.WHITE_POKER_CHIP) return 1.0;
        if (item == ModItems.RED_POKER_CHIP) return 5.0;
        if (item == ModItems.GREEN_POKER_CHIP) return 25.0;
        if (item == ModItems.BLUE_POKER_CHIP) return 50.0;
        if (item == ModItems.BLACK_POKER_CHIP) return 100.0;
        if (item == ModItems.PURPLE_POKER_CHIP) return 500.0;
        if (item == ModItems.YELLOW_POKER_CHIP) return 1000.0;
        if (item == ModItems.PINK_POKER_CHIP) return 5000.0;
        if (item == ModItems.ORANGE_POKER_CHIP) return 25000.0;
        
        // Other items sell for same price as bought
        if (item == ModItems.CARD_DECK) return 50.0;
        if (item == ModItems.SIMPLE_DICE) return 10.0; // Traditional dice
        
        // Fantasy dice - calculate price based on material and sides
        if (item == ModItems.FANTASY_DICE) {
            int sides = ItemFantasyDice.getSides(itemStack);
            String material = ItemFantasyDice.getMaterial(itemStack);
            if (sides > 0 && !material.isEmpty()) {
                return ItemFantasyDice.calculatePrice(sides, material);
            }
        }
        
        return 0.0;
    }
    
    private String getItemName(ItemStack itemStack) {
        Item item = itemStack.getItem();
        
        if (item == ModItems.WHITE_POKER_CHIP) return "white poker chip" + (itemStack.getCount() > 1 ? "s" : "");
        if (item == ModItems.RED_POKER_CHIP) return "red poker chip" + (itemStack.getCount() > 1 ? "s" : "");
        if (item == ModItems.GREEN_POKER_CHIP) return "green poker chip" + (itemStack.getCount() > 1 ? "s" : "");
        if (item == ModItems.BLUE_POKER_CHIP) return "blue poker chip" + (itemStack.getCount() > 1 ? "s" : "");
        if (item == ModItems.BLACK_POKER_CHIP) return "black poker chip" + (itemStack.getCount() > 1 ? "s" : "");
        if (item == ModItems.PURPLE_POKER_CHIP) return "purple poker chip" + (itemStack.getCount() > 1 ? "s" : "");
        if (item == ModItems.YELLOW_POKER_CHIP) return "yellow poker chip" + (itemStack.getCount() > 1 ? "s" : "");
        if (item == ModItems.PINK_POKER_CHIP) return "pink poker chip" + (itemStack.getCount() > 1 ? "s" : "");
        if (item == ModItems.ORANGE_POKER_CHIP) return "orange poker chip" + (itemStack.getCount() > 1 ? "s" : "");
        if (item == ModItems.CARD_DECK) return "card deck" + (itemStack.getCount() > 1 ? "s" : "");
        if (item == ModItems.SIMPLE_DICE) return "traditional dice";
        
        if (item == ModItems.FANTASY_DICE) {
            int sides = ItemFantasyDice.getSides(itemStack);
            String material = ItemFantasyDice.getMaterial(itemStack);
            if (sides > 0 && !material.isEmpty()) {
                String materialName = material.substring(0, 1).toUpperCase() + material.substring(1);
                return materialName.toLowerCase() + " " + sides + "-sided dice";
            }
            return "fantasy dice";
        }
        
        return "item" + (itemStack.getCount() > 1 ? "s" : "");
    }
    
    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        // Handle selling chips when dropped into sell slot
        if (index == 18 && type == ClickType.MOUSE_LEFT && action == SlotActionType.PICKUP) {
            ItemStack cursorStack = this.player.currentScreenHandler.getCursorStack();
            if (!cursorStack.isEmpty() && (cursorStack.getItem() instanceof ItemPokerChip || 
                cursorStack.getItem() == ModItems.CARD_DECK || 
                cursorStack.getItem() == ModItems.SIMPLE_DICE || 
                cursorStack.getItem() == ModItems.FANTASY_DICE)) {
                sellChips(cursorStack);
                return true;
            }
        }
        
        // Handle shift-clicking chips from player inventory to auto-sell
        if (type.shift && action == SlotActionType.QUICK_MOVE) {
            // For shift-click, try to get the stack from the slot that was clicked
            try {
                ItemStack clickedStack = this.player.currentScreenHandler.getSlot(index).getStack();
                if (!clickedStack.isEmpty() && (clickedStack.getItem() instanceof ItemPokerChip || 
                    clickedStack.getItem() == ModItems.CARD_DECK || 
                    clickedStack.getItem() == ModItems.SIMPLE_DICE || 
                    clickedStack.getItem() == ModItems.FANTASY_DICE)) {
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
        this.setSlot(25, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Your Balance").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("$" + String.format("%.2f", balance)).formatted(Formatting.GREEN))
                .build());
    }
    
    /**
     * Buys simple dice for the player.
     */
    private void buySimpleDice(int amount, double pricePerDice) {
        double totalCost = amount * pricePerDice;
        
        if (EconomyManager.hasBalance(this.player, totalCost)) {
            if (EconomyManager.withdraw(this.player, totalCost)) {
                // Give the dice to the player
                for (int i = 0; i < amount; i++) {
                    ItemStack diceStack = new ItemStack(ModItems.SIMPLE_DICE);
                    if (!this.player.getInventory().insertStack(diceStack)) {
                        this.player.dropItem(diceStack, false);
                    }
                }
                
                // Play success sound
                this.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                
                // Send success message
                EconomyManager.sendEconomyMessage(this.player, 
                    String.format("Purchased %d traditional dice for $%.0f", amount, totalCost));
                
                // Update the balance display
                this.updateBalance();
            }
        } else {
            // Play error sound
            this.player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 2.0f);
            EconomyManager.sendErrorMessage(this.player, "Insufficient funds!");
        }
    }
    
    /**
     * Gets the maximum affordable amount of simple dice.
     */
    private int getMaxAffordableSimpleDice(double pricePerDice) {
        double balance = EconomyManager.getBalance(this.player);
        int maxAmount = (int) (balance / pricePerDice);
        return Math.min(maxAmount, 8); // Max stack size for simple dice
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
