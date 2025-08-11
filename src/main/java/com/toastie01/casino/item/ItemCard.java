package com.toastie01.casino.item;

import com.toastie01.casino.util.CardHelper;
import com.toastie01.casino.util.ItemHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Card item that can be flipped between face-up and face-down states.
 * Left-click to flip between showing card face and card back.
 */
public class ItemCard extends Item {

    public ItemCard() {
        super(new Settings().maxCount(1).maxDamage(51));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // Show card name
        Text cardName = CardHelper.getCardName(stack.getDamage());
        tooltip.add(Text.literal("").append(cardName).formatted(Formatting.GOLD));
        
        // Show deck info if available
        NbtCompound nbt = ItemHelper.getNBT(stack);
        if (nbt.contains("UUID")) {
            tooltip.add(Text.literal("Deck: " + nbt.getString("UUID").substring(0, 8) + "...")
                .formatted(Formatting.GRAY));
        }
        
        // ItemCard is always face-up
        tooltip.add(Text.literal("Face-Up Card").formatted(Formatting.GREEN));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Right-click: Place card (handled by useOnBlock method)
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    /**
     * Flip the card between face-up and face-down states
     * @param heldItem The card item stack
     * @param user The player flipping the card
     * @param hand The hand holding the card
     * @return The flipped card
     */
    public ItemStack flipCard(ItemStack heldItem, PlayerEntity user, Hand hand) {
        // ItemCard should always flip TO face-down (ItemCardCovered)
        NbtCompound nbt = ItemHelper.getNBT(heldItem);
        
        // Create new covered card item
        ItemStack newCard = new ItemStack(com.toastie01.casino.init.ModItems.CARD_COVERED);
        
        // Copy all NBT data
        NbtCompound newNbt = ItemHelper.getNBT(newCard);
        newNbt.putByte("SkinID", nbt.getByte("SkinID"));
        
        // Copy UUID in the format it's stored
        if (nbt.contains("UUID")) {
            if (nbt.containsUuid("UUID")) {
                newNbt.putUuid("UUID", nbt.getUuid("UUID"));
            } else {
                newNbt.putString("UUID", nbt.getString("UUID"));
            }
        }
        
        newNbt.putBoolean("Covered", true); // Always covered when flipping from ItemCard
        
        // Copy damage value (card ID)
        newCard.setDamage(heldItem.getDamage());
        
        // Set CustomModelData for card back (use skin ID)
        byte skinId = newNbt.getByte("SkinID");
        newNbt.putInt("CustomModelData", skinId);
        
        // Play flip sound
        user.getWorld().playSound(null, user.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, 
                          SoundCategory.PLAYERS, 0.5F, 1.5F);
        
        // Replace the item in the player's hand
        user.setStackInHand(hand, newCard);
        
        return newCard;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        
        // Set CustomModelData for face-up card display
        if (world.isClient) {
            int cardId = stack.getDamage();
            NbtCompound nbt = ItemHelper.getNBT(stack);
            
            // ItemCard should always show face-up (use card ID + 100)
            nbt.putInt("CustomModelData", 100 + cardId);
        }
        
        // Cleanup: Remove card if parent deck is too far away
        if (world.getTime() % 60 == 0 && entity instanceof PlayerEntity player) {
            NbtCompound nbt = ItemHelper.getNBT(stack);
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
                // Place card entity in the world (face up)
                if (!world.isClient) {
                    net.minecraft.util.math.Vec3d placementPos = net.minecraft.util.math.Vec3d.ofCenter(pos).add(0, 1, 0);
                    
                    // Get UUID from card
                    NbtCompound nbt = ItemHelper.getNBT(stack);
                    java.util.UUID deckUUID = ItemHelper.safeGetUuid(nbt, "UUID");
                    if (deckUUID == null) {
                        deckUUID = java.util.UUID.randomUUID(); // Fallback UUID
                    }
                    
                    com.toastie01.casino.entity.EntityCard cardEntity = new com.toastie01.casino.entity.EntityCard(
                        world, placementPos, player.getYaw(), (byte) 0, deckUUID, false, (byte) stack.getDamage());
                    
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
