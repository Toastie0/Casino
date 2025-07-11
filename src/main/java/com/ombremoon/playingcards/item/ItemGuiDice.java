package com.ombremoon.playingcards.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A simple dice item specifically for GUI display purposes.
 * This item has no functionality and shows no default tooltips.
 */
public class ItemGuiDice extends Item {
    
    public ItemGuiDice() {
        super(new Settings().maxCount(1));
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // Intentionally empty - no default tooltips for GUI display
    }
}
