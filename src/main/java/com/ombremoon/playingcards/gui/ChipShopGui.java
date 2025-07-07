package com.ombremoon.playingcards.gui;

import com.ombremoon.playingcards.economy.EconomyManager;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.item.ItemPokerChip;
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

public class ChipShopGui extends SimpleGui {
    
    public ChipShopGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.setTitle(Text.literal("Chip Shop").formatted(Formatting.GOLD, Formatting.BOLD));
        this.setupGui();
    }
    
    private void setupGui() {
        // Row 1: All poker chips (0-8)
        // Slot 0: White Chip ($1.00)
        this.setSlot(0, new GuiElementBuilder(ModItems.WHITE_POKER_CHIP)
                .setName(Text.literal("White Poker Chip").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("$1.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(1.0) : 1;
                    buyChip(ModItems.WHITE_POKER_CHIP, 1.0, amount, "white");
                })
                .build());
        
        // Slot 1: Red Chip ($5.00)
        this.setSlot(1, new GuiElementBuilder(ModItems.RED_POKER_CHIP)
                .setName(Text.literal("Red Poker Chip").formatted(Formatting.RED))
                .addLoreLine(Text.literal("$5.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(5.0) : 1;
                    buyChip(ModItems.RED_POKER_CHIP, 5.0, amount, "red");
                })
                .build());
        
        // Slot 2: Green Chip ($25.00)
        this.setSlot(2, new GuiElementBuilder(ModItems.GREEN_POKER_CHIP)
                .setName(Text.literal("Green Poker Chip").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("$25.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(25.0) : 1;
                    buyChip(ModItems.GREEN_POKER_CHIP, 25.0, amount, "green");
                })
                .build());
        
        // Slot 3: Blue Chip ($50.00)
        this.setSlot(3, new GuiElementBuilder(ModItems.BLUE_POKER_CHIP)
                .setName(Text.literal("Blue Poker Chip").formatted(Formatting.BLUE))
                .addLoreLine(Text.literal("$50.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(50.0) : 1;
                    buyChip(ModItems.BLUE_POKER_CHIP, 50.0, amount, "blue");
                })
                .build());
        
        // Slot 4: Black Chip ($100.00)
        this.setSlot(4, new GuiElementBuilder(ModItems.BLACK_POKER_CHIP)
                .setName(Text.literal("Black Poker Chip").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("$100.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(100.0) : 1;
                    buyChip(ModItems.BLACK_POKER_CHIP, 100.0, amount, "black");
                })
                .build());
        
        // Slot 5: Purple Chip ($500.00)
        this.setSlot(5, new GuiElementBuilder(ModItems.PURPLE_POKER_CHIP)
                .setName(Text.literal("Purple Poker Chip").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal("$500.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(500.0) : 1;
                    buyChip(ModItems.PURPLE_POKER_CHIP, 500.0, amount, "purple");
                })
                .build());
        
        // Slot 6: Yellow Chip ($1000.00)
        this.setSlot(6, new GuiElementBuilder(ModItems.YELLOW_POKER_CHIP)
                .setName(Text.literal("Yellow Poker Chip").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("$1000.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(1000.0) : 1;
                    buyChip(ModItems.YELLOW_POKER_CHIP, 1000.0, amount, "yellow");
                })
                .build());
        
        // Slot 7: Pink Chip ($5000.00)
        this.setSlot(7, new GuiElementBuilder(ModItems.PINK_POKER_CHIP)
                .setName(Text.literal("Pink Poker Chip").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("$5000.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(5000.0) : 1;
                    buyChip(ModItems.PINK_POKER_CHIP, 5000.0, amount, "pink");
                })
                .build());
        
        // Slot 8: Orange Chip ($25000.00)
        this.setSlot(8, new GuiElementBuilder(ModItems.ORANGE_POKER_CHIP)
                .setName(Text.literal("Orange Poker Chip").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("$25000.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(25000.0) : 1;
                    buyChip(ModItems.ORANGE_POKER_CHIP, 25000.0, amount, "orange");
                })
                .build());
        
        // Row 2: Card Decks (9-12) + Glass Panes (13-17)
        // Slot 9: Blue Card Deck (Skin 0)
        ItemStack blueDeck = com.ombremoon.playingcards.item.ItemCardDeck.createDeck((byte) 0);
        this.setSlot(9, new GuiElementBuilder(blueDeck.getItem())
                .setCount(blueDeck.getCount())
                .setName(Text.literal("Classic Blue Card Deck").formatted(Formatting.BLUE))
                .addLoreLine(Text.literal("$10.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Classic blue design").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Contains 52 cards").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(10.0) : 1;
                    buyCardDeck(amount, (byte) 0, "Classic Blue");
                })
                .build());
        
        // Slot 10: Red Card Deck (Skin 1)
        ItemStack redDeck = com.ombremoon.playingcards.item.ItemCardDeck.createDeck((byte) 1);
        this.setSlot(10, new GuiElementBuilder(redDeck.getItem())
                .setCount(redDeck.getCount())
                .setName(Text.literal("Classic Red Card Deck").formatted(Formatting.RED))
                .addLoreLine(Text.literal("$10.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Classic red design").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Contains 52 cards").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(10.0) : 1;
                    buyCardDeck(amount, (byte) 1, "Classic Red");
                })
                .build());
        
        // Slot 11: Black Card Deck (Skin 2)
        ItemStack blackDeck = com.ombremoon.playingcards.item.ItemCardDeck.createDeck((byte) 2);
        this.setSlot(11, new GuiElementBuilder(blackDeck.getItem())
                .setCount(blackDeck.getCount())
                .setName(Text.literal("Classic Black Card Deck").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("$10.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Classic black design").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Contains 52 cards").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(10.0) : 1;
                    buyCardDeck(amount, (byte) 2, "Classic Black");
                })
                .build());
        
        // Slot 12: Pig Card Deck (Skin 3)
        ItemStack pigDeck = com.ombremoon.playingcards.item.ItemCardDeck.createDeck((byte) 3);
        this.setSlot(12, new GuiElementBuilder(pigDeck.getItem())
                .setCount(pigDeck.getCount())
                .setName(Text.literal("Pig Variant Card Deck").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("$10.00 each").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Pig variant design").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Contains 52 cards").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Left Click: Buy 1").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Shift Click: Buy max affordable").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    int amount = type.shift ? getMaxAffordable(10.0) : 1;
                    buyCardDeck(amount, (byte) 3, "Pig Variant");
                })
                .build());
        
        // Slots 13-17: Glass Panes
        for (int i = 13; i <= 17; i++) {
            this.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
        
        // Row 3: Sell Slot + Glass Panes + Balance + Close
        // Slot 18: Sell Slot
        this.setSlot(18, new GuiElementBuilder(Items.HOPPER)
                .setName(Text.literal("Sell Items").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Drag items here to sell").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Or shift-click items from inventory").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("White Chip: $1.00 each").formatted(Formatting.WHITE))
                .addLoreLine(Text.literal("Red Chip: $5.00 each").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Green Chip: $25.00 each").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("Blue Chip: $50.00 each").formatted(Formatting.BLUE))
                .addLoreLine(Text.literal("Black Chip: $100.00 each").formatted(Formatting.DARK_GRAY))
                .addLoreLine(Text.literal("Purple Chip: $500.00 each").formatted(Formatting.DARK_PURPLE))
                .addLoreLine(Text.literal("Yellow Chip: $1000.00 each").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Pink Chip: $5000.00 each").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("Orange Chip: $25000.00 each").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("Card Deck: $10.00 each").formatted(Formatting.AQUA))
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
        
        // Slot 26: Close Button
        this.setSlot(26, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED))
                .addLoreLine(Text.literal("Click to close the shop").formatted(Formatting.GRAY))
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
        // Create ItemStacks with proper skin data
        ItemStack blueDeck = com.ombremoon.playingcards.item.ItemCardDeck.createDeck((byte) 0);
        ItemStack redDeck = com.ombremoon.playingcards.item.ItemCardDeck.createDeck((byte) 1);
        ItemStack blackDeck = com.ombremoon.playingcards.item.ItemCardDeck.createDeck((byte) 2);
        ItemStack pigDeck = com.ombremoon.playingcards.item.ItemCardDeck.createDeck((byte) 3);
        
        // Set the visual items in the slots (keeping the existing callbacks)
        this.setSlot(9, new GuiElementBuilder(blueDeck.getItem())
                .setName(Text.translatable("item.playingcards.card_deck")
                        .append(" (")
                        .append(Text.translatable("card.skin.blue"))
                        .append(")")
                        .formatted(Formatting.AQUA))
                .setLore(java.util.List.of(Text.translatable("lore.deck.cards", 52).formatted(Formatting.GRAY)))
                .setCallback((index, type, action) -> {
                    if (type == ClickType.MOUSE_LEFT) {
                        buyDeck(blueDeck, "Blue");
                    }
                }));
        
        this.setSlot(10, new GuiElementBuilder(redDeck.getItem())
                .setName(Text.translatable("item.playingcards.card_deck")
                        .append(" (")
                        .append(Text.translatable("card.skin.red"))
                        .append(")")
                        .formatted(Formatting.RED))
                .setLore(java.util.List.of(Text.translatable("lore.deck.cards", 52).formatted(Formatting.GRAY)))
                .setCallback((index, type, action) -> {
                    if (type == ClickType.MOUSE_LEFT) {
                        buyDeck(redDeck, "Red");
                    }
                }));
        
        this.setSlot(11, new GuiElementBuilder(blackDeck.getItem())
                .setName(Text.translatable("item.playingcards.card_deck")
                        .append(" (")
                        .append(Text.translatable("card.skin.black"))
                        .append(")")
                        .formatted(Formatting.DARK_GRAY))
                .setLore(java.util.List.of(Text.translatable("lore.deck.cards", 52).formatted(Formatting.GRAY)))
                .setCallback((index, type, action) -> {
                    if (type == ClickType.MOUSE_LEFT) {
                        buyDeck(blackDeck, "Black");
                    }
                }));
        
        this.setSlot(12, new GuiElementBuilder(pigDeck.getItem())
                .setName(Text.translatable("item.playingcards.card_deck")
                        .append(" (")
                        .append(Text.translatable("card.skin.pig"))
                        .append(")")
                        .formatted(Formatting.LIGHT_PURPLE))
                .setLore(java.util.List.of(Text.translatable("lore.deck.cards", 52).formatted(Formatting.GRAY)))
                .setCallback((index, type, action) -> {
                    if (type == ClickType.MOUSE_LEFT) {
                        buyDeck(pigDeck, "Pig");
                    }
                }));
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
        double pricePerDeck = 10.0;
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
    
    private void buyDeck(ItemStack deckStack, String deckColor) {
        double deckPrice = 10.0; // Standard deck price
        
        if (EconomyManager.withdraw(this.player, deckPrice)) {
            // Give the deck to the player
            ItemStack deckToGive = deckStack.copy();
            if (!this.player.getInventory().insertStack(deckToGive)) {
                // If inventory is full, drop the item
                this.player.dropItem(deckToGive, false);
            }
            
            // Play success sound
            this.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            
            // Send success message
            EconomyManager.sendEconomyMessage(this.player, 
                String.format("Bought a %s card deck for $%.2f", deckColor, deckPrice));
            
            // Update the balance display
            this.updateBalance();
        } else {
            // Insufficient funds
            this.player.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 1.0f, 1.0f);
            EconomyManager.sendEconomyMessage(this.player, 
                String.format("Insufficient funds! You need $%.2f to buy a %s card deck", deckPrice, deckColor));
        }
    }
    
    private int getMaxAffordable(double pricePerChip) {
        double balance = EconomyManager.getBalance(this.player);
        int maxAmount = (int) (balance / pricePerChip);
        return Math.min(maxAmount, 20); // Cap at 20 (max stack size)
    }
    
    private void sellChips(ItemStack itemStack) {
        // Check if it's a sellable item (poker chip or card deck)
        if (!(itemStack.getItem() instanceof ItemPokerChip) && itemStack.getItem() != ModItems.CARD_DECK) {
            return;
        }
        
        double pricePerItem = getSellPrice(itemStack.getItem());
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
            String itemColor = getItemColor(itemStack.getItem());
            EconomyManager.sendEconomyMessage(this.player, 
                String.format("Sold %d %s%s for $%.2f", 
                    amount, itemColor, amount == 1 ? "" : "s", totalValue));
            
            // Update the balance display
            this.updateBalance();
        }
    }
    
    private double getSellPrice(Item item) {
        if (item == ModItems.WHITE_POKER_CHIP) return 1.0;
        if (item == ModItems.RED_POKER_CHIP) return 5.0;
        if (item == ModItems.GREEN_POKER_CHIP) return 25.0;
        if (item == ModItems.BLUE_POKER_CHIP) return 50.0;
        if (item == ModItems.BLACK_POKER_CHIP) return 100.0;
        if (item == ModItems.PURPLE_POKER_CHIP) return 500.0;
        if (item == ModItems.YELLOW_POKER_CHIP) return 1000.0;
        if (item == ModItems.PINK_POKER_CHIP) return 5000.0;
        if (item == ModItems.ORANGE_POKER_CHIP) return 25000.0;
        if (item == ModItems.CARD_DECK) return 10.0; // Card decks sell for same price as bought
        return 0.0;
    }
    
    private String getItemColor(Item item) {
        if (item == ModItems.WHITE_POKER_CHIP) return "white";
        if (item == ModItems.RED_POKER_CHIP) return "red";
        if (item == ModItems.GREEN_POKER_CHIP) return "green";
        if (item == ModItems.BLUE_POKER_CHIP) return "blue";
        if (item == ModItems.BLACK_POKER_CHIP) return "black";
        if (item == ModItems.PURPLE_POKER_CHIP) return "purple";
        if (item == ModItems.YELLOW_POKER_CHIP) return "yellow";
        if (item == ModItems.PINK_POKER_CHIP) return "pink";
        if (item == ModItems.ORANGE_POKER_CHIP) return "orange";
        if (item == ModItems.CARD_DECK) return "card deck";
        return "unknown";
    }
    
    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        // Handle selling chips when dropped into sell slot
        if (index == 18 && type == ClickType.MOUSE_LEFT && action == SlotActionType.PICKUP) {
            ItemStack cursorStack = this.player.currentScreenHandler.getCursorStack();
            if (!cursorStack.isEmpty() && (cursorStack.getItem() instanceof ItemPokerChip || cursorStack.getItem() == ModItems.CARD_DECK)) {
                sellChips(cursorStack);
                return true;
            }
        }
        
        // Handle shift-clicking chips from player inventory to auto-sell
        if (type.shift && action == SlotActionType.QUICK_MOVE) {
            // For shift-click, try to get the stack from the slot that was clicked
            try {
                ItemStack clickedStack = this.player.currentScreenHandler.getSlot(index).getStack();
                if (!clickedStack.isEmpty() && (clickedStack.getItem() instanceof ItemPokerChip || clickedStack.getItem() == ModItems.CARD_DECK)) {
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
}
