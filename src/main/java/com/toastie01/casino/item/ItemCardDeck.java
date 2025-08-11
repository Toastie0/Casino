package com.toastie01.casino.item;

import com.toastie01.casino.PCReference;
import com.toastie01.casino.entity.EntityCardDeck;
import com.toastie01.casino.init.ModItems;
import com.toastie01.casino.util.CardHelper;
import com.toastie01.casino.util.ItemHelper;
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
 * Right-click in air to flip between face-up and face-down modes.
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
            case PCReference.CardSkins.BLUE -> "Blue Card Deck";
            case PCReference.CardSkins.RED -> "Red Card Deck";
            case PCReference.CardSkins.BLACK -> "Black Card Deck";
            case PCReference.CardSkins.PIG -> "Pig Card Deck";
            default -> "Card Deck";
        };
        
        return Text.literal(deckName).formatted(getColorForSkin(skinId));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Right-click in air: Place deck
        // This method handles right-click in air (not on blocks)
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        
        if (player != null) {
            // Place deck entity in the world (similar to poker chip placement)
            NbtCompound nbt = ItemHelper.getNBT(stack);
            byte skinId = nbt.getByte("SkinID");
            boolean faceUpMode = nbt.getBoolean("FaceUp");
            
            // Use hit position for placement
            EntityCardDeck cardDeck = new EntityCardDeck(world, context.getHitPos(), context.getPlayerYaw(), skinId, true, faceUpMode);
            
            if (!world.isClient) {
                world.spawnEntity(cardDeck);
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
            }
            
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.PASS;
    }
    
    private Formatting getColorForSkin(byte skinId) {
        return switch (skinId) {
            case PCReference.CardSkins.BLUE -> Formatting.BLUE;
            case PCReference.CardSkins.RED -> Formatting.RED;
            case PCReference.CardSkins.BLACK -> Formatting.DARK_GRAY;
            case PCReference.CardSkins.PIG -> Formatting.LIGHT_PURPLE;
            default -> Formatting.WHITE;
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = ItemHelper.getNBT(stack);
        
        // Only show face mode if the NBT contains face data (real deck, not GUI display)
        if (nbt.contains("FaceUp")) {
            boolean faceUp = nbt.getBoolean("FaceUp");
            if (faceUp) {
                tooltip.add(Text.literal("Face Up").formatted(Formatting.GRAY));
            } else {
                tooltip.add(Text.literal("Face Down").formatted(Formatting.GRAY));
            }
        }
        
        // Deck value (show everywhere)
        tooltip.add(Text.literal("Value: $" + String.format("%.2f", PCReference.CARD_DECK_SELL_PRICE)).formatted(Formatting.GREEN));
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
