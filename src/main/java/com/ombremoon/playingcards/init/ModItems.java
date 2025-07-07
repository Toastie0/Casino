package com.ombremoon.playingcards.init;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.item.ItemPokerChip;
import com.ombremoon.playingcards.item.ItemCard;
import com.ombremoon.playingcards.item.ItemCardCovered;
import com.ombremoon.playingcards.item.ItemCardDeck;
import com.ombremoon.playingcards.item.ItemDice;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {
    
    // Poker Chips with custom values
    public static final Item WHITE_POKER_CHIP = registerItem("white_poker_chip", 
        new ItemPokerChip((byte)0, 1.0));
    public static final Item RED_POKER_CHIP = registerItem("red_poker_chip", 
        new ItemPokerChip((byte)1, 5.0));
    public static final Item GREEN_POKER_CHIP = registerItem("green_poker_chip", 
        new ItemPokerChip((byte)2, 25.0));
    public static final Item BLUE_POKER_CHIP = registerItem("blue_poker_chip", 
        new ItemPokerChip((byte)3, 50.0));
    public static final Item BLACK_POKER_CHIP = registerItem("black_poker_chip", 
        new ItemPokerChip((byte)4, 100.0));
    
    // High-value poker chips
    public static final Item PURPLE_POKER_CHIP = registerItem("purple_poker_chip", 
        new ItemPokerChip((byte)5, 500.0));
    public static final Item YELLOW_POKER_CHIP = registerItem("yellow_poker_chip", 
        new ItemPokerChip((byte)6, 1000.0));
    public static final Item PINK_POKER_CHIP = registerItem("pink_poker_chip", 
        new ItemPokerChip((byte)7, 5000.0));
    public static final Item ORANGE_POKER_CHIP = registerItem("orange_poker_chip", 
        new ItemPokerChip((byte)8, 25000.0));
    
    // Playing Cards
    public static final Item CARD_DECK = registerItem("card_deck", 
        new ItemCardDeck());
    public static final Item CARD_COVERED = registerItem("card_covered", 
        new ItemCardCovered());
    public static final Item CARD = registerItem("card", 
        new ItemCard());
    
    // Dice
    public static final Item DICE = registerItem("dice", 
        new ItemDice());
    
    // Creative Tab
    public static final ItemGroup PLAYING_CARDS_TAB = FabricItemGroup.builder()
        .displayName(Text.translatable("itemgroup.playingcards.playing_cards"))
        .icon(() -> new ItemStack(WHITE_POKER_CHIP))
        .entries((displayContext, entries) -> {
            // Blocks
            entries.add(ModBlocks.CASINO_CARPET);
            entries.add(ModBlocks.CASINO_TABLE);
            
            // Poker chips
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
            
            // Dice
            entries.add(DICE);
            
            // Card decks with different skins
            for (byte skinId = 0; skinId < 4; skinId++) {
                ItemStack deckStack = com.ombremoon.playingcards.item.ItemCardDeck.createDeck(skinId);
                entries.add(deckStack);
            }
        })
        .build();
    
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(PCReference.MOD_ID, name), item);
    }
    
    public static void initialize() {
        PCReference.LOGGER.info("Registering Playing Cards items...");
        
        // Register the creative tab
        Registry.register(Registries.ITEM_GROUP, new Identifier(PCReference.MOD_ID, "playing_cards"), PLAYING_CARDS_TAB);
    }
}
