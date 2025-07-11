package com.ombremoon.playingcards.init;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.block.entity.CasinoTableBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registry for all Casino mod block entities.
 */
public class ModBlockEntities {
    
    public static BlockEntityType<CasinoTableBlockEntity> CASINO_TABLE;
    
    /**
     * Initializes all mod block entities.
     */
    public static void initialize() {
        PCReference.LOGGER.info("Registering Casino block entities...");
        
        CASINO_TABLE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(PCReference.MOD_ID, "casino_table"),
            BlockEntityType.Builder.create(
                (pos, state) -> new CasinoTableBlockEntity(CASINO_TABLE, pos, state), 
                ModBlocks.CASINO_TABLE
            ).build(null)
        );
    }
}
