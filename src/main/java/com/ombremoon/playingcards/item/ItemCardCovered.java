package com.ombremoon.playingcards.item;

import com.ombremoon.playingcards.util.CardHelper;
import com.ombremoon.playingcards.util.ItemHelper;
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
            tooltip.add(Text.translatable("lore.card.flip").formatted(Formatting.DARK_GRAY));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        // Flip the card on both client and server
        ItemStack newCard = flipCard(stack, user, hand);
        
        if (!world.isClient) {
            // Server side - return the new card
            return TypedActionResult.success(newCard);
        } else {
            // Client side - consume the action
            return TypedActionResult.consume(newCard);
        }
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
        
        // Debug logging
        com.ombremoon.playingcards.PCReference.LOGGER.info("Flipping card: currentCovered={}, cardID={}, damage={}", 
            currentCovered, nbt.getInt("CardID"), heldItem.getDamage());
        
        // Determine the new item type and state
        Item newItem;
        boolean newCovered;
        
        if (currentCovered) {
            // Currently covered -> flip to face-up
            newItem = com.ombremoon.playingcards.init.ModItems.CARD;
            newCovered = false;
        } else {
            // Currently face-up -> flip to covered
            newItem = com.ombremoon.playingcards.init.ModItems.CARD_COVERED;
            newCovered = true;
        }
        
        // Create new card item with flipped state
        ItemStack newCard = new ItemStack(newItem);
        
        // Copy all NBT data
        NbtCompound newNbt = ItemHelper.getNBT(newCard);
        newNbt.putInt("CardID", nbt.getInt("CardID"));
        newNbt.putByte("SkinID", nbt.getByte("SkinID"));
        newNbt.putString("UUID", nbt.getString("UUID")); // Copy UUID as string
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
        
        // Debug logging
        com.ombremoon.playingcards.PCReference.LOGGER.info("Flipped to: covered={}, cardID={}, damage={}, skinID={}, customModelData={}", 
            newCovered, newNbt.getInt("CardID"), newCard.getDamage(), newNbt.getByte("SkinID"),
            newCard.hasNbt() && newCard.getNbt().contains("CustomModelData") ? newCard.getNbt().getInt("CustomModelData") : -1);
        
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
        
        // Automatic cleanup like original - check every 60 ticks
        if (world.getTime() % 60 == 0 && entity instanceof net.minecraft.entity.player.PlayerEntity player) {
            NbtCompound nbt = ItemHelper.getNBT(stack);
            
            java.util.UUID deckUUID = ItemHelper.safeGetUuid(nbt, "UUID");
            if (deckUUID == null || deckUUID.getLeastSignificantBits() == 0) {
                return;
            }
            
            // Look for parent deck in nearby area (like original - 20 block radius)
            net.minecraft.util.math.BlockPos pos = player.getBlockPos();
            java.util.List<com.ombremoon.playingcards.entity.EntityCardDeck> nearbyDecks = world.getEntitiesByClass(
                com.ombremoon.playingcards.entity.EntityCardDeck.class,
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
                // Look for nearby decks (like original - 8 block radius)
                java.util.List<com.ombremoon.playingcards.entity.EntityCardDeck> nearbyDecks = world.getEntitiesByClass(
                    com.ombremoon.playingcards.entity.EntityCardDeck.class,
                    new net.minecraft.util.math.Box(pos.getX() - 8, pos.getY() - 8, pos.getZ() - 8,
                                                   pos.getX() + 8, pos.getY() + 8, pos.getZ() + 8),
                    deck -> true
                );
                
                // Debug logging
                com.ombremoon.playingcards.PCReference.LOGGER.info("Found {} nearby decks for card placement", nearbyDecks.size());
                
                NbtCompound nbt = ItemHelper.getNBT(stack);
                java.util.UUID cardDeckUUID = null;
                
                // Safe UUID reading
                if (nbt.contains("UUID")) {
                    try {
                        String uuidString = nbt.getString("UUID");
                        if (!uuidString.isEmpty()) {
                            cardDeckUUID = java.util.UUID.fromString(uuidString);
                            com.ombremoon.playingcards.PCReference.LOGGER.info("Card UUID: {}", cardDeckUUID);
                        }
                    } catch (Exception ex) {
                        // UUID is invalid, cardDeckUUID remains null
                        com.ombremoon.playingcards.PCReference.LOGGER.warn("Invalid UUID in card: {}", ex.getMessage());
                    }
                } else {
                    com.ombremoon.playingcards.PCReference.LOGGER.info("Card has no UUID in NBT");
                }
                
                // Find matching deck
                for (com.ombremoon.playingcards.entity.EntityCardDeck deck : nearbyDecks) {
                    com.ombremoon.playingcards.PCReference.LOGGER.info("Checking deck UUID: {} vs card UUID: {}", deck.getUuid(), cardDeckUUID);
                    if (cardDeckUUID != null && deck.getUuid().equals(cardDeckUUID)) {
                        // Create card entity (like original)
                        net.minecraft.util.math.Vec3d hitPos = context.getHitPos();
                        
                        // Safety checks before creating entity
                        if (hitPos != null && !world.isClient) {
                            com.ombremoon.playingcards.PCReference.LOGGER.info("Creating card entity at position: {}", hitPos);
                            com.ombremoon.playingcards.entity.EntityCard cardEntity = new com.ombremoon.playingcards.entity.EntityCard(
                                world, hitPos, context.getPlayerYaw(), nbt.getByte("SkinID"), 
                                cardDeckUUID, nbt.getBoolean("Covered"), (byte) stack.getDamage()
                            );
                            
                            world.spawnEntity(cardEntity);
                            stack.decrement(1);
                            com.ombremoon.playingcards.PCReference.LOGGER.info("Card entity spawned successfully");
                        }
                        
                        return net.minecraft.util.ActionResult.SUCCESS;
                    }
                }
                
                // If no matching deck found, log it
                if (cardDeckUUID != null) {
                    com.ombremoon.playingcards.PCReference.LOGGER.info("No matching deck found for card UUID: {}", cardDeckUUID);
                } else {
                    com.ombremoon.playingcards.PCReference.LOGGER.info("Card has no UUID, cannot place");
                }
            } catch (Exception e) {
                // Log the error and prevent crash
                com.ombremoon.playingcards.PCReference.LOGGER.error("Error placing card entity: {}", e.getMessage(), e);
                return net.minecraft.util.ActionResult.FAIL;
            }
        }
        
        return net.minecraft.util.ActionResult.PASS;
    }
}
