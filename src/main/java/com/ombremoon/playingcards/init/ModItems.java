package com.ombremoon.playingcards.init;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.config.CasinoConfig;
import com.ombremoon.playingcards.item.ItemPokerChip;
import com.ombremoon.playingcards.item.ItemCard;
import com.ombremoon.playingcards.item.ItemCardCovered;
import com.ombremoon.playingcards.item.ItemCardDeck;
import com.ombremoon.playingcards.item.ItemFantasyDice;
import com.ombremoon.playingcards.item.ItemGuiDice;
import com.ombremoon.playingcards.item.ItemGuiPokerChip;
import com.ombremoon.playingcards.item.ItemSimpleDice;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Registry for all Casino mod items including poker chips, cards, and dice.
 * Also defines the creative mode tab for the mod.
 */
public class ModItems {
    
    // ===== POKER CHIPS =====
    // Standard value poker chips
    public static final Item WHITE_POKER_CHIP = registerItem("white_poker_chip", 
        new ItemPokerChip((byte)0, CasinoConfig.getInstance().getChipValue("white")));
    public static final Item RED_POKER_CHIP = registerItem("red_poker_chip", 
        new ItemPokerChip((byte)1, CasinoConfig.getInstance().getChipValue("red")));
    public static final Item GREEN_POKER_CHIP = registerItem("green_poker_chip", 
        new ItemPokerChip((byte)2, CasinoConfig.getInstance().getChipValue("green")));
    public static final Item BLUE_POKER_CHIP = registerItem("blue_poker_chip", 
        new ItemPokerChip((byte)3, CasinoConfig.getInstance().getChipValue("blue")));
    public static final Item BLACK_POKER_CHIP = registerItem("black_poker_chip", 
        new ItemPokerChip((byte)4, CasinoConfig.getInstance().getChipValue("black")));
    
    // High-value poker chips
    public static final Item PURPLE_POKER_CHIP = registerItem("purple_poker_chip", 
        new ItemPokerChip((byte)5, CasinoConfig.getInstance().getChipValue("purple")));
    public static final Item YELLOW_POKER_CHIP = registerItem("yellow_poker_chip", 
        new ItemPokerChip((byte)6, CasinoConfig.getInstance().getChipValue("yellow")));
    public static final Item PINK_POKER_CHIP = registerItem("pink_poker_chip", 
        new ItemPokerChip((byte)7, CasinoConfig.getInstance().getChipValue("pink")));
    public static final Item ORANGE_POKER_CHIP = registerItem("orange_poker_chip", 
        new ItemPokerChip((byte)8, CasinoConfig.getInstance().getChipValue("orange")));
    
    // ===== PLAYING CARDS =====
    public static final Item CARD_DECK = registerItem("card_deck", 
        new ItemCardDeck());
    public static final Item CARD_COVERED = registerItem("card_covered", 
        new ItemCardCovered());
    public static final Item CARD = registerItem("card", 
        new ItemCard());
    
    // ===== DICE =====
    public static final Item SIMPLE_DICE = registerItem("simple_dice", 
        new ItemSimpleDice());
    public static final Item FANTASY_DICE = registerItem("fantasy_dice", 
        new ItemFantasyDice());
    public static final Item GUI_DICE = registerItem("gui_dice", 
        new ItemGuiDice());
    public static final Item GUI_POKER_CHIP = registerItem("gui_poker_chip", 
        new ItemGuiPokerChip());
    
    // ===== CREATIVE TAB =====
    public static final ItemGroup CASINO_TAB = FabricItemGroup.builder()
        .displayName(Text.translatable("itemgroup.casino.tab"))
        .icon(() -> new ItemStack(WHITE_POKER_CHIP))
        .entries((displayContext, entries) -> {
            // Casino blocks
            entries.add(ModBlocks.CASINO_CARPET);
            entries.add(ModBlocks.VIP_CASINO_CARPET);
            entries.add(ModBlocks.CASINO_TABLE);
            entries.add(ModBlocks.BAR_STOOL);
            
            // Standard poker chips
            entries.add(WHITE_POKER_CHIP);
            entries.add(RED_POKER_CHIP);
            entries.add(GREEN_POKER_CHIP);
            entries.add(BLUE_POKER_CHIP);
            entries.add(BLACK_POKER_CHIP);
            
            // High-value poker chips
            entries.add(PURPLE_POKER_CHIP);
            entries.add(YELLOW_POKER_CHIP);
            entries.add(PINK_POKER_CHIP);
            entries.add(ORANGE_POKER_CHIP);
            
            // Gaming items
            entries.add(SIMPLE_DICE);
            
            // Card decks with different skins
            for (byte skinId = 0; skinId < 4; skinId++) {
                ItemStack deckStack = com.ombremoon.playingcards.item.ItemCardDeck.createDeck(skinId);
                entries.add(deckStack);
            }
        })
        .build();
    
    /**
     * Registers an item with the game registry.
     */
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(PCReference.MOD_ID, name), item);
    }
    
    /**
     * Initializes all mod items and registers the creative tab.
     */
    public static void initialize() {
        PCReference.LOGGER.info("Registering Casino items...");
        
        // Register the creative tab
        Registry.register(Registries.ITEM_GROUP, new Identifier(PCReference.MOD_ID, "casino"), CASINO_TAB);
    }
}
