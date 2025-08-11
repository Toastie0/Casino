package com.toastie01.casino;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central reference class for the Casino mod.
 * Contains mod ID, logger, and other shared constants.
 */
public final class PCReference {
    
    /** The mod identifier used for registration and namespacing */
    public static final String MOD_ID = "casino";
    
    /** Centralized logger for the Casino mod */
    public static final Logger LOGGER = LoggerFactory.getLogger("Casino");
    
    /** Mod name for display purposes */
    public static final String MOD_NAME = "Casino";
    
    // ===== GAME CONSTANTS =====
    
    /** Maximum number of chips that can be stacked in one slot */
    public static final int MAX_CHIP_STACK_SIZE = 25;
    
    /** Number of different card deck skin designs available */
    public static final int CARD_DECK_SKIN_COUNT = 4;
    
    /** Total number of cards in a standard deck */
    public static final int STANDARD_DECK_SIZE = 52;
    
    /** Number of different poker chip types */
    public static final int POKER_CHIP_TYPE_COUNT = 9;
    
    /** Default sell price for card decks */
    public static final double CARD_DECK_SELL_PRICE = 50.0;
    
    // ===== PHYSICS CONSTANTS =====
    
    /** Entity bounding box size constants */
    public static final double ENTITY_BOX_SIZE = 0.125;
    public static final double ENTITY_BOX_HEIGHT = 0.05;
    
    /** Physics constants for entity movement */
    public static final double REDUCED_GRAVITY = -0.02D;
    public static final double FRICTION_MULTIPLIER = 0.8D;
    
    // ===== DEFAULT CHIP VALUES =====
    
    /** Default chip values (used as fallbacks if config is unavailable) */
    public static final class DefaultChipValues {
        public static final double WHITE = 1.0;
        public static final double RED = 5.0;
        public static final double GREEN = 25.0;
        public static final double BLUE = 50.0;
        public static final double BLACK = 100.0;
        public static final double PURPLE = 500.0;
        public static final double YELLOW = 1000.0;
        public static final double PINK = 5000.0;
        public static final double ORANGE = 25000.0;
    }
    
    // ===== CHIP CONSTANTS =====
    
    /** Chip IDs for different chip types */
    public static final class ChipIds {
        public static final byte WHITE = 0;
        public static final byte RED = 1;
        public static final byte GREEN = 2;
        public static final byte BLUE = 3;
        public static final byte BLACK = 4;
        public static final byte PURPLE = 5;
        public static final byte YELLOW = 6;
        public static final byte PINK = 7;
        public static final byte ORANGE = 8;
    }
    
    // ===== CARD CONSTANTS =====
    
    /** Card skin IDs for different deck designs */
    public static final class CardSkins {
        public static final byte BLUE = 0;
        public static final byte RED = 1;
        public static final byte BLACK = 2;
        public static final byte PIG = 3;
    }
    
    private PCReference() {
        // Utility class - prevent instantiation
    }
}
