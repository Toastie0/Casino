package com.ombremoon.playingcards.block.base;

import net.minecraft.block.Block;

/**
 * The base class for Blocks.
 */
public class BlockBase extends Block {

    /**
     * @param settings The specific properties for the Block. (Creative Tab, hardness, material, etc.)
     */
    public BlockBase(Settings settings) {
        super(settings);
    }
}
