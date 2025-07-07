package com.ombremoon.playingcards.init;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.block.BlockCasinoCarpet;
import com.ombremoon.playingcards.block.BlockCasinoTable;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    
    // Blocks
    public static final Block CASINO_CARPET = registerBlock("casino_carpet", 
        new BlockCasinoCarpet());
    public static final Block CASINO_TABLE = registerBlock("casino_table", 
        new BlockCasinoTable());
    
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(PCReference.MOD_ID, name), block);
    }
    
    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(PCReference.MOD_ID, name), 
            new BlockItem(block, new Item.Settings()));
    }
    
    public static void initialize() {
        PCReference.LOGGER.info("Registering Playing Cards blocks...");
    }
}
