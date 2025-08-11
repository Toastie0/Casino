package com.toastie01.casino.item;

import com.toastie01.casino.util.CardHelper;
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

public class ItemCardCovered extends Item {

    public ItemCardCovered() {
        super(new Settings().maxCount(1).maxDamage(51));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = ItemHelper.getNBT(stack);
        
        // Show card skin information
        byte skinId = nbt.getByte("SkinID");
        if (skinId >= 0 && skinId < CardHelper.CARD_SKIN_NAMES.length) {
            tooltip.add(Text.translatable("lore.cover").append(" ")
                    .formatted(Formatting.GRAY)
                    .append(Text.translatable(CardHelper.CARD_SKIN_NAMES[skinId])
                            .formatted(Formatting.AQUA)));
        }
        
        // Show covered status
        boolean covered = nbt.getBoolean("Covered");
        if (covered) {
            tooltip.add(Text.translatable("lore.card.covered").formatted(Formatting.DARK_GRAY));
        } else {
            tooltip.add(Text.literal("Face-Up Card").formatted(Formatting.GREEN));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Right-click: Place card (handled by useOnBlock method)
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    /**
     * Flip the card between covered and face-up states
     * @param heldItem The card item stack
     * @param user The player flipping the card
     * @param hand The hand holding the card
     * @return The new flipped card
     */
    public ItemStack flipCard(ItemStack heldItem, PlayerEntity user, Hand hand) {
        NbtCompound nbt = ItemHelper.getNBT(heldItem);
        boolean currentCovered = nbt.getBoolean("Covered");
        
        // Determine the new item type and state
        Item newItem;
        boolean newCovered;
        
        if (currentCovered) {
            // Currently covered -> flip to face-up
            newItem = com.toastie01.casino.init.ModItems.CARD;
            newCovered = false;
        } else {
            // Currently face-up -> flip to covered
            newItem = com.toastie01.casino.init.ModItems.CARD_COVERED;
            newCovered = true;
        }
        
        // Create new card item with flipped state
        ItemStack newCard = new ItemStack(newItem);
        
        // Copy all NBT data
        NbtCompound newNbt = ItemHelper.getNBT(newCard);
        newNbt.putInt("CardID", nbt.getInt("CardID"));
        newNbt.putByte("SkinID", nbt.getByte("SkinID"));
        
        // Copy UUID in the format it's stored (handle both UUID and String formats)
        if (nbt.contains("UUID")) {
            if (nbt.containsUuid("UUID")) {
                // Copy as native UUID format (for cards picked up from entities)
                newNbt.putUuid("UUID", nbt.getUuid("UUID"));
            } else {
                // Copy as string format (for cards drawn from decks)
                newNbt.putString("UUID", nbt.getString("UUID"));
            }
        }
        
        newNbt.putBoolean("Covered", newCovered);
        
        // Copy damage value (card ID)
        newCard.setDamage(heldItem.getDamage());
        
        // Handle CustomModelData properly based on card state
        if (newCovered) {
            // Flipping TO covered - use the skin ID as CustomModelData (0-3)
            byte skinId = newNbt.getByte("SkinID");
            newNbt.putInt("CustomModelData", skinId);
        } else {
            // Flipping TO face-up - use card ID + 100 as CustomModelData for face selection
            int cardId = newCard.getDamage(); // Use damage value as card ID
            newNbt.putInt("CustomModelData", 100 + cardId);
        }
        
        // Replace the item in the player's hand
        user.setStackInHand(hand, newCard);
        
        return newCard;
    }

    /**
     * Get the card ID (0-51) from the item's damage value
     * @param stack The ItemStack
     * @return Card ID
     */
    public static int getCardId(ItemStack stack) {
        return stack.getDamage();
    }

    /**
     * Set the card ID (0-51) using the item's damage value
     * @param stack The ItemStack
     * @param cardId Card ID (0-51)
     */
    public static void setCardId(ItemStack stack, int cardId) {
        stack.setDamage(cardId);
    }

    /**
     * Get the card's skin ID
     * @param stack The ItemStack
     * @return Skin ID (0-3)
     */
    public static byte getSkinId(ItemStack stack) {
        return ItemHelper.getNBT(stack).getByte("SkinID");
    }

    /**
     * Set the card's skin ID
     * @param stack The ItemStack
     * @param skinId Skin ID (0-3)
     */
    public static void setSkinId(ItemStack stack, byte skinId) {
        ItemHelper.getNBT(stack).putByte("SkinID", skinId);
    }

    /**
     * Check if the card is covered (face-down)
     * @param stack The ItemStack
     * @return True if covered
     */
    public static boolean isCovered(ItemStack stack) {
        return ItemHelper.getNBT(stack).getBoolean("Covered");
    }

    /**
     * Set whether the card is covered (face-down)
     * @param stack The ItemStack
     * @param covered True if covered
     */
    public static void setCovered(ItemStack stack, boolean covered) {
        ItemHelper.getNBT(stack).putBoolean("Covered", covered);
    }

    @Override
    public boolean isDamageable() {
        return false; // Prevent durability damage
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false; // Prevent enchantments
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        
        // Ensure CustomModelData is properly set for rendering
        if (world.isClient) {
            ensureCustomModelData(stack);
        }
        
        // Automatic cleanup like original - check every 60 ticks
        if (world.getTime() % 60 == 0 && entity instanceof net.minecraft.entity.player.PlayerEntity player) {
            NbtCompound nbt = ItemHelper.getNBT(stack);
            
            java.util.UUID deckUUID = ItemHelper.safeGetUuid(nbt, "UUID");
            if (deckUUID == null || deckUUID.getLeastSignificantBits() == 0) {
                return;
            }
            
            // Look for parent deck in nearby area (like original - 20 block radius)
            net.minecraft.util.math.BlockPos pos = player.getBlockPos();
            java.util.List<com.toastie01.casino.entity.EntityCardDeck> nearbyDecks = world.getEntitiesByClass(
                com.toastie01.casino.entity.EntityCardDeck.class,
                new net.minecraft.util.math.Box(pos.getX() - 20, pos.getY() - 20, pos.getZ() - 20,
                                               pos.getX() + 20, pos.getY() + 20, pos.getZ() + 20),
                deck -> deck.getUuid().equals(deckUUID)
            );
            
            // If no parent deck found, remove this card from inventory (like original)
            if (nearbyDecks.isEmpty()) {
                player.getInventory().getStack(slot).decrement(1);
            }
        }
    }

    @Override
    public net.minecraft.util.ActionResult useOnBlock(net.minecraft.item.ItemUsageContext context) {
        net.minecraft.entity.player.PlayerEntity player = context.getPlayer();
        
        if (player != null && !player.isSneaking()) {
            net.minecraft.util.math.BlockPos pos = context.getBlockPos();
            net.minecraft.world.World world = context.getWorld();
            ItemStack stack = context.getStack();
            
            try {
                // Check if player is directly targeting a deck entity
                net.minecraft.util.math.Box targetBox = new net.minecraft.util.math.Box(
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
                );
                
                java.util.List<com.toastie01.casino.entity.EntityCardDeck> targetedDecks = world.getEntitiesByClass(
                    com.toastie01.casino.entity.EntityCardDeck.class,
                    targetBox,
                    deck -> true
                );
                
                // If targeting a deck directly, don't place the card - let deck handle the interaction
                if (!targetedDecks.isEmpty()) {
                    com.toastie01.casino.PCReference.LOGGER.info("Player targeting deck directly, passing interaction to deck");
                    return net.minecraft.util.ActionResult.PASS;
                }
                
                // Not targeting a deck, place the card on the ground
                net.minecraft.util.math.Vec3d hitPos = context.getHitPos();
                if (hitPos != null && !world.isClient) {
                    NbtCompound nbt = ItemHelper.getNBT(stack);
                    
                    // Get or preserve UUID - handle both UUID and String formats
                    java.util.UUID cardDeckUUID = null;
                    if (nbt.contains("UUID")) {
                        try {
                            // Try to read as UUID first (for cards picked up from entities)
                            if (nbt.containsUuid("UUID")) {
                                cardDeckUUID = nbt.getUuid("UUID");
                            } else {
                                // Fallback to string format (for cards drawn from decks)
                                String uuidString = nbt.getString("UUID");
                                if (!uuidString.isEmpty()) {
                                    cardDeckUUID = java.util.UUID.fromString(uuidString);
                                }
                            }
                        } catch (Exception ex) {
                            // UUID is invalid, but we can still place the card
                            com.toastie01.casino.PCReference.LOGGER.warn("Invalid UUID in card, placing anyway: {}", ex.getMessage());
                        }
                    }
                    
                    // Create card entity regardless of UUID status
                    com.toastie01.casino.PCReference.LOGGER.info("Placing card entity at position: {}", hitPos);
                    com.toastie01.casino.entity.EntityCard cardEntity = new com.toastie01.casino.entity.EntityCard(
                        world, hitPos, context.getPlayerYaw(), nbt.getByte("SkinID"), 
                        cardDeckUUID, nbt.getBoolean("Covered"), (byte) stack.getDamage()
                    );
                    
                    world.spawnEntity(cardEntity);
                    stack.decrement(1);
                    com.toastie01.casino.PCReference.LOGGER.info("Card entity placed successfully");
                    
                    return net.minecraft.util.ActionResult.SUCCESS;
                }
            } catch (Exception e) {
                // Log the error and prevent crash
                com.toastie01.casino.PCReference.LOGGER.error("Error placing card entity: {}", e.getMessage(), e);
                return net.minecraft.util.ActionResult.FAIL;
            }
        }
        
        return net.minecraft.util.ActionResult.PASS;
    }
    
    /**
     * Ensures the CustomModelData is properly set for correct rendering in all views.
     * This method should be called on the client side to fix third-person rendering issues.
     */
    private void ensureCustomModelData(ItemStack stack) {
        NbtCompound nbt = ItemHelper.getNBT(stack);
        boolean covered = nbt.getBoolean("Covered");
        
        // Check if CustomModelData is missing or incorrect
        int expectedModelData;
        if (covered) {
            // Covered cards should use skin ID (0-3)
            byte skinId = nbt.getByte("SkinID");
            expectedModelData = skinId;
        } else {
            // Face-up cards should use card ID + 100
            int cardId = stack.getDamage();
            expectedModelData = 100 + cardId;
        }
        
        // Update CustomModelData if it's missing or incorrect
        int currentModelData = nbt.getInt("CustomModelData");
        if (currentModelData != expectedModelData) {
            nbt.putInt("CustomModelData", expectedModelData);
        }
    }
}
