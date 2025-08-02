package com.ombremoon.playingcards.item;

import com.ombremoon.playingcards.entity.EntityPokerChip;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.util.ItemHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ItemPokerChip extends Item {
    private final byte chipId;
    private final double value;
    
    public ItemPokerChip(byte chipId, double value) {
        super(new Settings().maxCount(25));
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
        
        // Add value tooltip only
        tooltip.add(Text.literal("Value: $" + String.format("%.2f", value)).formatted(Formatting.GREEN));
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
