package com.ombremoon.playingcards.gui;

import com.ombremoon.playingcards.economy.EconomyManager;
import com.ombremoon.playingcards.item.ItemFantasyDice;
import com.ombremoon.playingcards.util.SellUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Paginated GUI for purchasing different types of fantasy dice.
 * Shows all combinations of materials and sides in a 9-column grid with pagination.
 */
public class DiceShopGui extends SimpleGui {
    
    private int currentPage = 0;
    private final int itemsPerPage = 45; // 9x5 grid (leave bottom row for navigation)
    
    public DiceShopGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.setTitle(Text.literal("Dice Shop - Page " + (currentPage + 1)).formatted(Formatting.GOLD, Formatting.BOLD));
        this.setupGui();
    }
    
    private void setupGui() {
        // Clear all slots first
        for (int i = 0; i < this.getSize(); i++) {
            this.clearSlot(i);
        }
        
        // Calculate total dice types and pages
        int totalDiceTypes = ItemFantasyDice.MATERIALS.length * ItemFantasyDice.SIDES.length;
        int totalPages = (int) Math.ceil((double) totalDiceTypes / itemsPerPage);
        
        // Calculate start and end indices for current page
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalDiceTypes);
        
        int slotIndex = 0;
        int diceIndex = 0;
        
        // Add dice for current page
        for (String material : ItemFantasyDice.MATERIALS) {
            for (int sides : ItemFantasyDice.SIDES) {
                if (diceIndex >= startIndex && diceIndex < endIndex) {
                    double price = ItemFantasyDice.calculatePrice(sides, material);
                    
                    // Create the actual dice item to display with NBT
                    ItemStack displayDice = ItemFantasyDice.createDice(sides, material);
                    
                    // Calculate custom model data
                    int materialIndex = ItemFantasyDice.getMaterialTier(material);
                    int sidesIndex = getSidesIndex(sides);
                    int customModelData = materialIndex * 6 + sidesIndex;
                    
                          this.setSlot(slotIndex, GuiElementBuilder.from(displayDice)
                            .setCustomModelData(customModelData)
                        .setName(Text.literal(formatMaterialName(material) + " " + sides + "-sided Dice")
                                .formatted(getColorForMaterial(material)))
                            .addLoreLine(Text.literal("$" + String.format("%.0f", price) + " each")
                                    .formatted(Formatting.GREEN))
                            .addLoreLine(Text.literal(""))
                            .addLoreLine(Text.literal("Left Click: Buy 1")
                                    .formatted(Formatting.YELLOW))
                            .addLoreLine(Text.literal("Shift Click: Buy max affordable")
                                    .formatted(Formatting.YELLOW))
                            .setCallback((clickIndex, type, action, gui) -> {
                                int amount = type.shift ? getMaxAffordable(price) : 1;
                                buyDice(sides, material, amount, price);
                            })
                            .build());
                    
                    slotIndex++;
                }
                diceIndex++;
            }
        }
        
        // Fill remaining slots in content area with glass panes
        while (slotIndex < itemsPerPage) {
            this.setSlot(slotIndex, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
            slotIndex++;
        }
        
        // Bottom row (slots 45-53) - Navigation
        setupNavigationRow(totalPages);
        
        // Update title with current page
        this.setTitle(Text.literal("Dice Shop - Page " + (currentPage + 1) + "/" + totalPages)
                .formatted(Formatting.GOLD, Formatting.BOLD));
    }
    
    private void setupNavigationRow(int totalPages) {
        // Previous page button (slot 45)
        if (currentPage > 0) {
            this.setSlot(45, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                    .setName(Text.literal("Previous Page").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("Go to page " + currentPage).formatted(Formatting.GRAY))
                    .setCallback((index, type, action, gui) -> {
                        currentPage--;
                        setupGui();
                    })
                    .build());
        } else {
            this.setSlot(45, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
        
        // Glass panes (slots 46-48)
        for (int i = 46; i <= 48; i++) {
            this.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
        
        // Sell All Casino Items button (slot 47)
        this.setSlot(47, new GuiElementBuilder(Items.HOPPER)
                .setName(Text.literal("Sell All Casino Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal("Sell all poker chips, decks,").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("and dice in your inventory").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to sell all").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    sellAllCasinoItems();
                })
                .build());
        
        // Back to Casino Main UI button (slot 49 - center)
        this.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.YELLOW))
                .setCallback((index, type, action, gui) -> {
                    gui.close();
                    CasinoMainGui casinoGui = new CasinoMainGui(this.getPlayer());
                    casinoGui.open();
                })
                .build());
        
        // Glass panes (slots 50-51)
        for (int i = 50; i <= 51; i++) {
            this.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
        
        // Close button (slot 52)
        this.setSlot(52, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("Close").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback((index, type, action, gui) -> {
                    gui.close();
                })
                .build());
        
        // Next page button (slot 53)
        if (currentPage < totalPages - 1) {
            this.setSlot(53, new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE)
                    .setName(Text.literal("Next Page").formatted(Formatting.GREEN))
                    .addLoreLine(Text.literal("Go to page " + (currentPage + 2)).formatted(Formatting.GRAY))
                    .setCallback((index, type, action, gui) -> {
                        currentPage++;
                        setupGui();
                    })
                    .build());
        } else {
            this.setSlot(53, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
        }
    }
    
    private void buyDice(int sides, String material, int amount, double pricePerDice) {
        double totalCost = amount * pricePerDice;
        
        if (EconomyManager.hasBalance(this.player, totalCost)) {
            if (EconomyManager.withdraw(this.player, totalCost)) {
                // Give the dice to the player
                for (int i = 0; i < amount; i++) {
                    ItemStack diceStack = ItemFantasyDice.createDice(sides, material);
                    
                    // Set the custom model data for the correct texture
                    int materialIndex = ItemFantasyDice.getMaterialTier(material);
                    int sidesIndex = getSidesIndex(sides);
                    int customModelData = materialIndex * 6 + sidesIndex;
                    diceStack.getOrCreateNbt().putInt("CustomModelData", customModelData);
                    
                    if (!this.player.getInventory().insertStack(diceStack)) {
                        this.player.dropItem(diceStack, false);
                    }
                }
                
                // Play success sound
                this.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                
                // Send success message
                String diceName = formatMaterialName(material) + " " + sides + "-sided dice";
                EconomyManager.sendEconomyMessage(this.player, 
                    String.format("Purchased %d %s for $%.0f", amount, diceName, totalCost));
            }
        } else {
            // Play error sound
            this.player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 2.0f);
            EconomyManager.sendErrorMessage(this.player, "Insufficient funds!");
        }
    }
    
    private int getMaxAffordable(double pricePerDice) {
        double balance = EconomyManager.getBalance(this.player);
        int maxAmount = (int) (balance / pricePerDice);
        return Math.min(maxAmount, 8); // Max stack size for dice
    }
    
    private String formatMaterialName(String material) {
        if (material == null || material.isEmpty()) return "Unknown";
        return material.substring(0, 1).toUpperCase() + material.substring(1);
    }
    
    private Formatting getColorForMaterial(String material) {
        return switch (material) {
            case "chocolate" -> Formatting.DARK_RED;
            case "paper" -> Formatting.WHITE;
            case "bone" -> Formatting.GRAY;
            case "wooden" -> Formatting.GOLD;
            case "stone" -> Formatting.DARK_GRAY;
            case "copper" -> Formatting.GOLD;
            case "slime" -> Formatting.GREEN;
            case "redstone" -> Formatting.RED;
            case "iron" -> Formatting.GRAY;
            case "emerald" -> Formatting.DARK_GREEN;
            case "golden" -> Formatting.YELLOW;
            case "amethyst" -> Formatting.LIGHT_PURPLE;
            case "frozen" -> Formatting.AQUA;
            case "diamond" -> Formatting.BLUE;
            case "netherite" -> Formatting.DARK_PURPLE;
            case "ender" -> Formatting.DARK_PURPLE;
            default -> Formatting.WHITE;
        };
    }
    
    /**
     * Get the index of the dice sides in the SIDES array
     */
    private static int getSidesIndex(int sides) {
        return switch (sides) {
            case 4 -> 0;
            case 6 -> 1;
            case 8 -> 2;
            case 10 -> 3;
            case 12 -> 4;
            case 20 -> 5;
            default -> 0; // Default to 4-sided if unknown
        };
    }
    
    /**
     * Sells all casino items in the player's inventory.
     */
    private void sellAllCasinoItems() {
        SellUtils.SellResult result = SellUtils.sellAllCasinoItems(this.player);
        
        if (result.success) {
            // Show success message to player (result already contains the message)
            // The SellUtils handles showing the message to the player
        }
    }
}
