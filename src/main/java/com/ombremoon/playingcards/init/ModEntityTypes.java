package com.ombremoon.playingcards.init;

import com.ombremoon.playingcards.PCReference;
import com.ombremoon.playingcards.entity.EntityCard;
import com.ombremoon.playingcards.entity.EntityCardDeck;
import com.ombremoon.playingcards.entity.EntityPokerChip;
import com.ombremoon.playingcards.entity.EntitySeat;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registry for all Casino mod entity types including cards, chips, and seats.
 */
public class ModEntityTypes {
    
    // ===== CASINO ENTITIES =====
    public static final EntityType<EntityCard> CARD = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(PCReference.MOD_ID, "card"),
        FabricEntityTypeBuilder.<EntityCard>create(SpawnGroup.MISC, EntityCard::new)
            .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
            .trackRangeBlocks(64)
            .build()
    );
    
    public static final EntityType<EntityCardDeck> CARD_DECK = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(PCReference.MOD_ID, "card_deck"),
        FabricEntityTypeBuilder.<EntityCardDeck>create(SpawnGroup.MISC, EntityCardDeck::new)
            .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
            .trackRangeBlocks(64)
            .build()
    );
    
    public static final EntityType<EntityPokerChip> POKER_CHIP = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(PCReference.MOD_ID, "poker_chip"),
        FabricEntityTypeBuilder.<EntityPokerChip>create(SpawnGroup.MISC, EntityPokerChip::new)
            .dimensions(EntityDimensions.fixed(0.3F, 0.3F))
            .trackRangeBlocks(64)
            .build()
    );
    
    public static final EntityType<EntitySeat> ENTITY_SEAT = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(PCReference.MOD_ID, "seat"),
        FabricEntityTypeBuilder.<EntitySeat>create(SpawnGroup.MISC, EntitySeat::new)
            .dimensions(EntityDimensions.fixed(0.0F, 0.0F))
            .trackRangeBlocks(64)
            .build()
    );
    
    /**
     * Initializes all mod entity types.
     */
    public static void initialize() {
        PCReference.LOGGER.info("Registering Casino entity types...");
    }
}
