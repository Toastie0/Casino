package com.toastie01.casino.item;

import com.toastie01.casino.PCReference;
import com.toastie01.casino.config.CasinoConfig;
import com.toastie01.casino.entity.EntityPokerChip;
import com.toastie01.casino.init.ModItems;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemPokerChip extends Item {
    private final byte chipId;
    private final double defaultValue; // Fallback value if config is not available
    
    public ItemPokerChip(byte chipId, double defaultValue) {
        super(new Settings().maxCount(PCReference.MAX_CHIP_STACK_SIZE));
        this.chipId = chipId;
        this.defaultValue = defaultValue;
    }
    
    public byte getChipId() {
        return chipId;
    }
    
    /**
     * Gets the current value from config, falling back to default if config is unavailable
     */
    public double getValue() {
        try {
            String colorName = getColorName(chipId);
            return CasinoConfig.getInstance().getChipValue(colorName);
        } catch (Exception e) {
            // Fallback to default value if config is not available
            return defaultValue;
        }
    }
    
    /**
     * Gets the color name for this chip
     */
    public String getColorName() {
        return getColorName(chipId);
    }
    
    /**
     * Gets the color name for a chip ID
     */
    private String getColorName(byte chipId) {
        return switch (chipId) {
            case PCReference.ChipIds.WHITE -> "white";
            case PCReference.ChipIds.RED -> "red";
            case PCReference.ChipIds.GREEN -> "green";
            case PCReference.ChipIds.BLUE -> "blue";
            case PCReference.ChipIds.BLACK -> "black";
            case PCReference.ChipIds.PURPLE -> "purple";
            case PCReference.ChipIds.YELLOW -> "yellow";
            case PCReference.ChipIds.PINK -> "pink";
            case PCReference.ChipIds.ORANGE -> "orange";
            default -> "white";
        };
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        
        // Add value tooltip using current config value
        tooltip.add(Text.literal("Value: $" + String.format("%.2f", getValue())).formatted(Formatting.GREEN));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        // Chips can be used directly without ownership setting
        return TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        ItemStack stack = context.getStack();
        
        if (player != null && !player.isSneaking()) {
            // Create and spawn poker chip entity
            Vec3d pos = context.getHitPos();
            EntityPokerChip chipEntity = new EntityPokerChip(world, pos, chipId);
            
            if (!world.isClient) {
                world.spawnEntity(chipEntity);
                stack.decrement(1);
            }
            
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.PASS;
    }
    
    public static Item getPokerChip(byte pokerChipID) {
        switch (pokerChipID) {
            case PCReference.ChipIds.RED:
                return ModItems.RED_POKER_CHIP;
            case PCReference.ChipIds.GREEN:
                return ModItems.GREEN_POKER_CHIP;
            case PCReference.ChipIds.BLUE:
                return ModItems.BLUE_POKER_CHIP;
            case PCReference.ChipIds.BLACK:
                return ModItems.BLACK_POKER_CHIP;
            case PCReference.ChipIds.PURPLE:
                return ModItems.PURPLE_POKER_CHIP;
            case PCReference.ChipIds.YELLOW:
                return ModItems.YELLOW_POKER_CHIP;
            case PCReference.ChipIds.PINK:
                return ModItems.PINK_POKER_CHIP;
            case PCReference.ChipIds.ORANGE:
                return ModItems.ORANGE_POKER_CHIP;
            default:
                return ModItems.WHITE_POKER_CHIP;
        }
    }
}
