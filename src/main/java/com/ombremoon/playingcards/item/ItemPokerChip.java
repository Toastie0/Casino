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
        
        // Add ownership info
        NbtCompound nbt = ItemHelper.getNBT(stack);
        if (nbt.contains("OwnerID")) {  // Note: Original uses "OwnerID" not "OwnerUUID"
            String ownerName = nbt.getString("OwnerName");
            if (!ownerName.isEmpty()) {
                tooltip.add(Text.literal("Owner: ").formatted(Formatting.GRAY)
                    .append(Text.literal(ownerName).formatted(Formatting.GOLD)));
            } else {
                tooltip.add(Text.literal("Owner: Set").formatted(Formatting.GRAY));
            }
        } else {
            tooltip.add(Text.literal("Owner: Not set").formatted(Formatting.GRAY));
        }
        
        // Add value tooltip
        tooltip.add(Text.literal("Value: $" + String.format("%.2f", value)).formatted(Formatting.GOLD));
        
        // Add stack info if more than 1
        if (stack.getCount() > 1) {
            double totalValue = value * stack.getCount();
            tooltip.add(Text.literal("Total: $" + String.format("%.2f", totalValue)).formatted(Formatting.GREEN));
        }
        
        // Add usage instructions
        tooltip.add(Text.literal("Crouch + Right-click: Set owner").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal("Right-click: Place on ground").formatted(Formatting.DARK_GRAY));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        if (user.isSneaking()) {
            // Set ownership
            NbtCompound nbt = ItemHelper.getNBT(stack);
            
            if (!nbt.contains("OwnerID")) {  // Note: Original uses "OwnerID" not "OwnerUUID"
                // Set the owner
                nbt.putUuid("OwnerID", user.getUuid());
                nbt.putString("OwnerName", user.getEntityName());
                
                if (!world.isClient) {
                    user.sendMessage(Text.translatable("message.poker_chip_owner_set").formatted(Formatting.GREEN), true);
                }
                
                return TypedActionResult.success(stack);
            } else {
                // Already has an owner
                if (!world.isClient) {
                    user.sendMessage(Text.translatable("message.poker_chip_owner_error").formatted(Formatting.RED), true);
                }
                return TypedActionResult.fail(stack);
            }
        }
        
        return TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        ItemStack stack = context.getStack();
        
        if (player != null && !player.isSneaking()) {
            NbtCompound nbt = ItemHelper.getNBT(stack);
            
            if (nbt.contains("OwnerID")) {  // Note: Original uses "OwnerID" not "OwnerUUID"
                UUID ownerUUID = nbt.getUuid("OwnerID");
                String ownerName = nbt.getString("OwnerName");
                
                // Check if placing on a casino table with ownership
                BlockPos blockPos = context.getBlockPos();
                if (world.getBlockState(blockPos).getBlock() instanceof com.ombremoon.playingcards.block.BlockCasinoTable) {
                    var blockEntity = world.getBlockEntity(blockPos);
                    if (blockEntity instanceof com.ombremoon.playingcards.block.entity.CasinoTableBlockEntity tableEntity) {
                        if (tableEntity.hasOwner() && !tableEntity.isOwner(ownerUUID)) {
                            // Table is owned by someone else
                            if (!world.isClient) {
                                player.sendMessage(Text.literal("Cannot place chips on " + tableEntity.getOwnerName() + "'s table!").formatted(Formatting.RED), true);
                            }
                            return ActionResult.FAIL;
                        }
                    }
                }
                
                // Create and spawn poker chip entity
                Vec3d pos = context.getHitPos();
                EntityPokerChip chipEntity = new EntityPokerChip(world, pos, ownerUUID, ownerName, chipId);
                
                if (!world.isClient) {
                    world.spawnEntity(chipEntity);
                    stack.decrement(1);
                }
                
                return ActionResult.SUCCESS;
            } else {
                if (!world.isClient) {
                    player.sendMessage(Text.literal("No owner found! Cannot place chip.").formatted(Formatting.RED), true);
                }
                return ActionResult.FAIL;
            }
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
