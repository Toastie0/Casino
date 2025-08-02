package com.ombremoon.playingcards.item;

import net.minecraft.item.Item;

/**
 * Simple display item for the casino shop GUI.
 * Uses white poker chip texture but without the value tooltip.
 */
public class ItemCasinoShop extends Item {
    
    public ItemCasinoShop() {
        super(new Settings().maxCount(1));
    }
    
    // No appendTooltip method means no custom tooltip will be added
}
