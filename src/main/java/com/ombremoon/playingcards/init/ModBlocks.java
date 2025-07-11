package com.ombremoon.playingcards.init;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.block.BlockBarStool;
import com.ombremoon.playingcards.block.BlockCasinoCarpet;
import com.ombremoon.playingcards.block.BlockCasinoTable;
import com.ombremoon.playingcards.block.BlockVipCasinoCarpet;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registry for all Casino mod blocks including tables, carpets, and furniture.
 */
public class ModBlocks {
    
    // ===== CASINO BLOCKS =====
    public static final Block CASINO_CARPET = registerBlock("casino_carpet", 
        new BlockCasinoCarpet());
    public static final Block VIP_CASINO_CARPET = registerBlock("vip_casino_carpet", 
        new BlockVipCasinoCarpet());
    public static final Block CASINO_TABLE = registerBlock("casino_table", 
        new BlockCasinoTable());
    public static final Block BAR_STOOL = registerBlock("bar_stool", 
        new BlockBarStool());
    
    /**
     * Registers a block and its corresponding block item.
     */
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(PCReference.MOD_ID, name), block);
    }
    
    /**
     * Registers the block item for a block.
     */
    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(PCReference.MOD_ID, name), 
            new BlockItem(block, new Item.Settings()));
    }
    
    /**
     * Initializes all mod blocks.
     */
    public static void initialize() {
        PCReference.LOGGER.info("Registering Casino blocks...");
    }
}
