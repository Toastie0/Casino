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
        newNbt.putBoolean("Covered", newCovered);
        
        // Copy damage value (card ID)
        newCard.setDamage(heldItem.getDamage());
        
        // Handle CustomModelData properly based on card state
        if (newCovered) {
            // Flipping TO covered - restore the skin ID from NBT as CustomModelData (0-3)
            byte skinId = newNbt.getByte("SkinID");
            newCard.getOrCreateNbt().putInt("CustomModelData", skinId);
        } else {
            // Flipping TO face-up - use card ID + 100 as CustomModelData for face selection
            int cardId = newNbt.getInt("CardID");
            newCard.getOrCreateNbt().putInt("CustomModelData", 100 + cardId);
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
}
