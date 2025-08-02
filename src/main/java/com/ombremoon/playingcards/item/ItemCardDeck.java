package com.ombremoon.playingcards.item;

import com.ombremoon.playingcards.entity.EntityCardDeck;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.util.CardHelper;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Simplified card deck item that can only be placed in the world.
 * Once placed, players interact with the deck entity to draw cards, shuffle, or collect.
 */
public class ItemCardDeck extends Item {

    public ItemCardDeck() {
        super(new Settings().maxCount(1));
    }

    @Override
    public Text getName(ItemStack stack) {
        NbtCompound nbt = ItemHelper.getNBT(stack);
        byte skinId = nbt.getByte("SkinID");
        
        // Create descriptive name based on card skin (matching GUI names exactly)
        String deckName = switch (skinId) {
            case 0 -> "Blue Card Deck";
            case 1 -> "Red Card Deck";
            case 2 -> "Black Card Deck";
            case 3 -> "Pig Card Deck";
            default -> "Card Deck";
        };
        
        return Text.literal(deckName)
                .formatted(getColorForSkin(skinId));
    }
    
    private Formatting getColorForSkin(byte skinId) {
        return switch (skinId) {
            case 0 -> Formatting.BLUE;
            case 1 -> Formatting.RED;
            case 2 -> Formatting.DARK_GRAY;
            case 3 -> Formatting.LIGHT_PURPLE;
            default -> Formatting.WHITE;
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // Only show tooltip in inventory, not in GUI (when advanced tooltip is false)
        if (!context.isAdvanced()) {
            NbtCompound nbt = ItemHelper.getNBT(stack);
            boolean faceUp = nbt.getBoolean("FaceUp");
            
            if (faceUp) {
                tooltip.add(Text.literal("Face Up").formatted(Formatting.GREEN));
            } else {
                tooltip.add(Text.literal("Face Down").formatted(Formatting.GRAY));
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Deck can only be used by placing it in the world, not used in inventory
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        
        if (player != null && !world.isClient) {
            // Place deck entity in the world
            NbtCompound nbt = ItemHelper.getNBT(stack);
            byte skinId = nbt.getByte("SkinID");
            boolean faceUpMode = nbt.getBoolean("FaceUp");
            
            EntityCardDeck cardDeck = new EntityCardDeck(world, context.getHitPos(), context.getPlayerYaw(), skinId, true, faceUpMode);
            world.spawnEntity(cardDeck);
            
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.PASS;
    }
    
    /**
     * Create a deck with a specific skin ID
     */
    public static ItemStack createDeck(byte skinId) {
        ItemStack stack = new ItemStack(ModItems.CARD_DECK);
        NbtCompound nbt = ItemHelper.getNBT(stack);
        nbt.putByte("SkinID", skinId);
        nbt.putInt("CustomModelData", skinId);
        nbt.putBoolean("FaceUp", false); // Default to face-down
        return stack;
    }
    
    /**
     * Create a deck with a specific skin ID and face-up state
     */
    public static ItemStack createDeck(byte skinId, boolean faceUp) {
        ItemStack stack = new ItemStack(ModItems.CARD_DECK);
        NbtCompound nbt = ItemHelper.getNBT(stack);
        nbt.putByte("SkinID", skinId);
        nbt.putInt("CustomModelData", skinId);
        nbt.putBoolean("FaceUp", faceUp);
        return stack;
    }
    
    /**
     * Create deck items for the creative menu with different skins
     */
    public static void fillItemGroup(List<ItemStack> stacks) {
        for (byte skinID = 0; skinID < CardHelper.CARD_SKIN_NAMES.length; skinID++) {
            ItemStack stack = new ItemStack(ModItems.CARD_DECK);
            NbtCompound nbt = ItemHelper.getNBT(stack);
            nbt.putByte("SkinID", skinID);
            nbt.putInt("CustomModelData", skinID);
            stacks.add(stack);
        }
    }
}