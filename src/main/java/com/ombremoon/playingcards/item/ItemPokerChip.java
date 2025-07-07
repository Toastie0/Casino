package com.ombremoon.playingcards.item;

import com.ombremoon.playingcards.init.ModItems;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemPokerChip extends Item {
    private final byte chipId;
    private final double value;
    
    public ItemPokerChip(byte chipId, double value) {
        super(new Settings().maxCount(20));
        this.chipId = chipId;
        this.value = value;
    }
    
    public byte getChipId() {
        return chipId;
    }
    
    public double getValue() {
        return value;
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        
        // Add value tooltip
        tooltip.add(Text.literal("Value: $" + String.format("%.2f", value)).formatted(Formatting.GOLD));
        
        // Add stack info if more than 1
        if (stack.getCount() > 1) {
            double totalValue = value * stack.getCount();
            tooltip.add(Text.literal("Total: $" + String.format("%.2f", totalValue)).formatted(Formatting.GREEN));
        }
        
        // Add ownership info tooltip
        tooltip.add(Text.literal("Right-click to place on ground").formatted(Formatting.GRAY));
    }
    
    public static Item getPokerChip(byte pokerChipID) {
        switch (pokerChipID) {
            case 1:
                return ModItems.RED_POKER_CHIP;
            case 2:
                return ModItems.GREEN_POKER_CHIP;
            case 3:
                return ModItems.BLUE_POKER_CHIP;
            case 4:
                return ModItems.BLACK_POKER_CHIP;
            case 5:
                return ModItems.PURPLE_POKER_CHIP;
            case 6:
                return ModItems.YELLOW_POKER_CHIP;
            case 7:
                return ModItems.PINK_POKER_CHIP;
            case 8:
                return ModItems.ORANGE_POKER_CHIP;
            default:
                return ModItems.WHITE_POKER_CHIP;
        }
    }
}
