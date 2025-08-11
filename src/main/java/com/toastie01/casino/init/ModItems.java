package com.toastie01.casino.init;

import com.toastie01.casino.PCReference;
import com.toastie01.casino.item.ItemPokerChip;
import com.toastie01.casino.item.ItemCard;
import com.toastie01.casino.item.ItemCardCovered;
import com.toastie01.casino.item.ItemCardDeck;
import com.toastie01.casino.item.ItemCasinoShop;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Registry for all Casino mod items including poker chips and cards.
 * Also defines the creative mode tab for the mod.
 */
public class ModItems {
    
    // ===== POKER CHIPS =====
    // Note: Chip values are retrieved from config at runtime, with these as fallback defaults
    public static final Item WHITE_POKER_CHIP = registerItem("white_poker_chip", 
        new ItemPokerChip(PCReference.ChipIds.WHITE, PCReference.DefaultChipValues.WHITE));
    public static final Item RED_POKER_CHIP = registerItem("red_poker_chip", 
        new ItemPokerChip(PCReference.ChipIds.RED, PCReference.DefaultChipValues.RED));
    public static final Item GREEN_POKER_CHIP = registerItem("green_poker_chip", 
        new ItemPokerChip(PCReference.ChipIds.GREEN, PCReference.DefaultChipValues.GREEN));
    public static final Item BLUE_POKER_CHIP = registerItem("blue_poker_chip", 
        new ItemPokerChip(PCReference.ChipIds.BLUE, PCReference.DefaultChipValues.BLUE));
    public static final Item BLACK_POKER_CHIP = registerItem("black_poker_chip", 
        new ItemPokerChip(PCReference.ChipIds.BLACK, PCReference.DefaultChipValues.BLACK));
    
    // High-value poker chips
    public static final Item PURPLE_POKER_CHIP = registerItem("purple_poker_chip", 
        new ItemPokerChip(PCReference.ChipIds.PURPLE, PCReference.DefaultChipValues.PURPLE));
    public static final Item YELLOW_POKER_CHIP = registerItem("yellow_poker_chip", 
        new ItemPokerChip(PCReference.ChipIds.YELLOW, PCReference.DefaultChipValues.YELLOW));
    public static final Item PINK_POKER_CHIP = registerItem("pink_poker_chip", 
        new ItemPokerChip(PCReference.ChipIds.PINK, PCReference.DefaultChipValues.PINK));
    public static final Item ORANGE_POKER_CHIP = registerItem("orange_poker_chip", 
        new ItemPokerChip(PCReference.ChipIds.ORANGE, PCReference.DefaultChipValues.ORANGE));
    
    // ===== PLAYING CARDS =====
    public static final Item CARD_DECK = registerItem("card_deck", 
        new ItemCardDeck());
    public static final Item CARD_COVERED = registerItem("card_covered", 
        new ItemCardCovered());
    public static final Item CARD = registerItem("card", 
        new ItemCard());
    
    // ===== GUI DISPLAY ITEMS =====
    public static final Item CASINO_SHOP_ICON = registerItem("casino_shop_icon", 
        new ItemCasinoShop());
    
    // ===== CREATIVE TAB =====
    public static final ItemGroup CASINO_TAB = FabricItemGroup.builder()
        .displayName(Text.translatable("itemgroup.casino.tab"))
        .icon(() -> new ItemStack(WHITE_POKER_CHIP))
        .entries((displayContext, entries) -> {
            // Casino blocks
            entries.add(ModBlocks.CASINO_CARPET);
            entries.add(ModBlocks.VIP_CASINO_CARPET);
            entries.add(ModBlocks.CASINO_TABLE);
            entries.add(ModBlocks.VIP_CASINO_TABLE);
            entries.add(ModBlocks.POKER_POT);
            entries.add(ModBlocks.BAR_STOOL);
            
            // Standard poker chips (single entry for each)
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
            
            // Card decks with different skins (face-down only)
            for (byte skinId = 0; skinId < PCReference.CARD_DECK_SKIN_COUNT; skinId++) {
                ItemStack deckStack = com.toastie01.casino.item.ItemCardDeck.createDeck(skinId, false);
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
