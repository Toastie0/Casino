package com.toastie01.casino.item;

import com.toastie01.casino.init.ModItems;
import com.toastie01.casino.util.ItemHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Simple card back item that always shows the card back texture.
 * Right-click to flip to card face (ItemCard).
 */
public class ItemCardBack extends Item {

    public ItemCardBack() {
        super(new Settings().maxCount(1).maxDamage(51));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("Card (face down)").formatted(Formatting.GRAY));
        
        // Show deck info if available
        NbtCompound nbt = ItemHelper.getNBT(stack);
        if (nbt.contains("UUID")) {
            tooltip.add(Text.literal("Deck: " + nbt.getString("UUID").substring(0, 8) + "...")
                .formatted(Formatting.GRAY));
        }
        
        tooltip.add(Text.literal("Right-click to flip over").formatted(Formatting.YELLOW));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack heldItem = user.getStackInHand(hand);
        
        if (!world.isClient) {
            // Create the flipped card (card face)
            ItemStack cardFace = new ItemStack(ModItems.CARD);
            cardFace.setDamage(heldItem.getDamage()); // Same card ID
            
            // Copy all NBT data
            NbtCompound nbt = ItemHelper.getNBT(heldItem);
            NbtCompound newNbt = ItemHelper.getNBT(cardFace);
            newNbt.copyFrom(nbt);
            
            // Replace the item in the player's hand
            user.setStackInHand(hand, cardFace);
            
            return TypedActionResult.success(cardFace);
        }
        
        return TypedActionResult.success(heldItem);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        
        // Set CustomModelData based on SkinID for proper back texture
        NbtCompound nbt = ItemHelper.getNBT(stack);
        byte skinId = nbt.getByte("SkinID");
        nbt.putInt("CustomModelData", skinId);
        
        // Cleanup: Remove card if parent deck is too far away
        if (world.getTime() % 60 == 0 && entity instanceof PlayerEntity player) {
            java.util.UUID deckUUID = ItemHelper.safeGetUuid(nbt, "UUID");
            
            if (deckUUID != null && deckUUID.getLeastSignificantBits() != 0) {
                // Check for nearby deck
                net.minecraft.util.math.BlockPos pos = player.getBlockPos();
                java.util.List<com.toastie01.casino.entity.EntityCardDeck> nearbyDecks = world.getEntitiesByClass(
                    com.toastie01.casino.entity.EntityCardDeck.class,
                    new net.minecraft.util.math.Box(pos.getX() - 20, pos.getY() - 20, pos.getZ() - 20,
                                                   pos.getX() + 20, pos.getY() + 20, pos.getZ() + 20),
                    deck -> deck.getUuid().equals(deckUUID)
                );
                
                if (nearbyDecks.isEmpty()) {
                    player.getInventory().getStack(slot).decrement(1);
                }
            }
        }
    }

    @Override
    public net.minecraft.util.ActionResult useOnBlock(net.minecraft.item.ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        
        if (player != null && !player.isSneaking()) {
            net.minecraft.util.math.BlockPos pos = context.getBlockPos();
            net.minecraft.world.World world = context.getWorld();
            ItemStack stack = context.getStack();
            
            try {
                // Place card entity in the world (face down)
                if (!world.isClient) {
                    net.minecraft.util.math.Vec3d placementPos = net.minecraft.util.math.Vec3d.ofCenter(pos).add(0, 1, 0);
                    
                    // Get UUID from card
                    NbtCompound nbt = ItemHelper.getNBT(stack);
                    java.util.UUID deckUUID = ItemHelper.safeGetUuid(nbt, "UUID");
                    if (deckUUID == null) {
                        deckUUID = java.util.UUID.randomUUID(); // Fallback UUID
                    }
                    
                    com.toastie01.casino.entity.EntityCard cardEntity = new com.toastie01.casino.entity.EntityCard(
                        world, placementPos, player.getYaw(), (byte) 0, deckUUID, true, (byte) stack.getDamage());
                    
                    world.spawnEntity(cardEntity);
                    stack.decrement(1);
                    
                    return net.minecraft.util.ActionResult.SUCCESS;
                }
            } catch (Exception e) {
                com.toastie01.casino.PCReference.LOGGER.error("Error placing card entity: {}", e.getMessage(), e);
                return net.minecraft.util.ActionResult.FAIL;
            }
        }
        
        return net.minecraft.util.ActionResult.PASS;
    }
}
