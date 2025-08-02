package com.ombremoon.playingcards.gui;

import com.ombremoon.playingcards.config.CasinoConfig;
import com.ombremoon.playingcards.economy.EconomyManager;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.util.SellUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
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
        
        // Row 1: Casino Shop (slot 1)
        // Casino Shop - Use casino shop icon to avoid chip value tooltip
        this.setSlot(1, new GuiElementBuilder(ModItems.CASINO_SHOP_ICON)
                .setName(Text.literal("Casino Shop").formatted(Formatting.GREEN, Formatting.BOLD))
                .setCallback((index, type, action, gui) -> {
                    this.close();
                    ChipShopGui shopGui = new ChipShopGui(this.getPlayer());
                    shopGui.open();
                })
                .build());

        // Row 3: Check Balance (moved to bottom row, same column as casino shop)
        this.setSlot(19, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("Your Balance").formatted(Formatting.GOLD))
                .addLoreLine(Text.literal("$" + String.format("%.2f", EconomyManager.getBalance(this.getPlayer()))).formatted(Formatting.GREEN))
                .setCallback((index, type, action, gui) -> {
                    this.setupGui(); // Refresh the GUI
                })
                .build());
        
        // Row 3: Sell All Chips (slot 22) and Close (slot 25)
        // Sell All Chips (moved to bottom row)
        this.setSlot(22, new GuiElementBuilder(Items.HOPPER)
                .setName(Text.literal("Sell All Casino Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .setCallback((index, type, action, gui) -> {
                    sellAllCasinoItems();
                })
                .build());
        
        // Close button
        this.setSlot(25, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED, Formatting.BOLD))
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
