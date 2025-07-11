package com.ombremoon.playingcards.gui;

import com.ombremoon.playingcards.config.CasinoConfig;
import com.ombremoon.playingcards.economy.EconomyManager;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.item.ItemPokerChip;
import com.ombremoon.playingcards.util.SellUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Main casino GUI providing access to all casino features in one interface.
 * Players can buy chips, sell chips, check balance, and access help.
 */
public class CasinoMainGui extends SimpleGui {
    
    public CasinoMainGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.setTitle(Text.literal(CasinoConfig.getInstance().mainGuiTitle).formatted(Formatting.GOLD, Formatting.BOLD));
        this.setupGui();
    }
    
    /**
     * Sets up the main casino GUI layout.
     */
    private void setupGui() {
        // Fill background with black stained glass
        for (int i = 0; i < this.getSize(); i++) {
            this.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
        
        // Row 1: Buy Chips (slot 1), Dice Shop (slot 4), Balance (slot 7)
        // Casino Shop - Use the GUI poker chip with pink appearance
        this.setSlot(1, new GuiElementBuilder(ModItems.GUI_POKER_CHIP)
                .setName(Text.literal("Casino Shop").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Buy and sell poker chips").formatted(Formatting.GRAY))
                .setCallback((index, type, action, gui) -> {
                    this.close();
                    ChipShopGui shopGui = new ChipShopGui(this.getPlayer());
                    shopGui.open();
                })
                .build());
        
        // Dice Shop - Use the GUI dice item with ender 20-sided appearance
        this.setSlot(4, new GuiElementBuilder(ModItems.GUI_DICE)
                .setName(Text.literal("Dice Shop").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Prices start from $10").formatted(Formatting.GREEN))
                .glow()
                .setCallback((index, type, action, gui) -> {
                    this.close();
                    DiceShopGui diceShop = new DiceShopGui(this.getPlayer());
                    diceShop.open();
                })
                .build());

        // Check Balance
        this.setSlot(7, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Balance").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal("Current Balance:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(String.format("$%.2f", EconomyManager.getBalance(this.getPlayer()))).formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to refresh").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    this.setupGui(); // Refresh the GUI
                })
                .build());
        
        // Row 3: Sell All Chips (slot 22) and Close (slot 25)
        // Sell All Chips (moved to bottom row)
        this.setSlot(22, new GuiElementBuilder(Items.HOPPER)
                .setName(Text.literal("Sell All Casino Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Sell all poker chips, decks,").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("and dice in your inventory").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to sell all").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    sellAllCasinoItems();
                })
                .build());
        
        // Close button
        this.setSlot(25, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal("Close this menu").formatted(Formatting.GRAY))
                .setCallback((index, type, action, gui) -> {
                    this.close();
                })
                .build());
    }
    
    /**
     * Sells all casino items in the player's inventory.
     */
    private void sellAllCasinoItems() {
        SellUtils.SellResult result = SellUtils.sellAllCasinoItems(this.getPlayer());
        
        if (result.success) {
            // Refresh GUI to update balance
            this.setupGui();
        }
    }
}
