package com.toastie01.casino.gui;

import com.toastie01.casino.config.CasinoConfig;
import com.toastie01.casino.economy.EconomyManager;
import com.toastie01.casino.init.ModItems;
import com.toastie01.casino.item.ItemPokerChip;
import com.toastie01.casino.util.SellUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
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
                    // This callback is handled in onAnyClick method
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
    
    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        // Handle selling when items are dragged/dropped into sell slot (22)
        if (index == 22) {
            ItemStack cursorStack = this.player.currentScreenHandler.getCursorStack();
            
            // If player has items in cursor (drag-and-drop), sell those items
            if (!cursorStack.isEmpty() && (cursorStack.getItem() instanceof ItemPokerChip || 
                cursorStack.getItem() == ModItems.CARD_DECK)) {
                
                // Handle different drop actions
                if (action == SlotActionType.PICKUP || action == SlotActionType.QUICK_MOVE || 
                    action == SlotActionType.THROW || action == SlotActionType.SWAP) {
                    sellIndividualItem(cursorStack);
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
                        sellIndividualItem(clickedStack);
                        return true;
                    }
                }
            } catch (Exception e) {
                // If slot access fails, ignore
            }
        }
        
        return super.onAnyClick(index, type, action);
    }
    
    /**
     * Sells an individual item stack.
     */
    private void sellIndividualItem(ItemStack stack) {
        SellUtils.SellResult result = SellUtils.sellIndividualItem(this.getPlayer(), stack);
        if (result.success) {
            // Refresh GUI to update balance
            this.setupGui();
        }
    }
}
