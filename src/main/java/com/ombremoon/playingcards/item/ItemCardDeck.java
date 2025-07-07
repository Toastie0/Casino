package com.ombremoon.playingcards.item;

import com.ombremoon.playingcards.entity.EntityCardDeck;
import com.ombremoon.playingcards.util.CardHelper;
import com.ombremoon.playingcards.util.ItemHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

public class ItemCardDeck extends Item {

    public ItemCardDeck() {
        super(new Settings().maxCount(1));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = ItemHelper.getNBT(stack);
        
        // Show deck skin information
        byte skinId = nbt.getByte("SkinID");
        if (skinId >= 0 && skinId < CardHelper.CARD_SKIN_NAMES.length) {
            tooltip.add(Text.translatable("lore.cover").append(" ")
                    .formatted(Formatting.GRAY)
                    .append(Text.translatable(CardHelper.CARD_SKIN_NAMES[skinId])
                            .formatted(Formatting.AQUA)));
        }
        
        // Show deck information
        int cardsLeft = getCardsLeft(stack);
        if (cardsLeft > 0) {
            tooltip.add(Text.translatable("lore.deck.cards", cardsLeft).formatted(Formatting.YELLOW));
            tooltip.add(Text.translatable("lore.deck.usage").formatted(Formatting.DARK_GRAY));
        } else {
            tooltip.add(Text.translatable("lore.deck.empty").formatted(Formatting.RED));
        }
        tooltip.add(Text.translatable("lore.deck.collect").formatted(Formatting.DARK_GRAY));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        if (!world.isClient) {
            int cardsLeft = getCardsLeft(stack);
            
            if (user.isSneaking()) {
                // Shift+Right-click in air: Collect cards from inventory back to deck
                // Note: This will only be called when not targeting a block (useOnBlock handles block targeting)
                return collectCardsFromInventory(world, user, stack);
            } else {
                // Regular Right-click: Draw a card
                if (cardsLeft > 0) {
                    return drawCardFromDeck(world, user, stack);
                } else {
                    // Empty deck
                    user.sendMessage(Text.translatable("message.deck.empty").formatted(Formatting.RED), true);
                    return TypedActionResult.fail(stack);
                }
            }
        }
        
        return TypedActionResult.consume(stack);
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity user = context.getPlayer();
        ItemStack stack = context.getStack();
        
        if (user != null && !world.isClient) {
            if (user.isSneaking()) {
                // Crouch+Right-click on block: Place deck entity
                if (getCardsLeft(stack) > 0) {
                    return placeDeckEntityOnBlock(world, user, stack, context);
                }
                return ActionResult.FAIL;
            } else {
                // Regular right-click on block: Draw a card (same as in air)
                // Return SUCCESS to prevent use() method from being called
                if (getCardsLeft(stack) > 0) {
                    drawCardFromDeck(world, user, stack);
                    return ActionResult.SUCCESS;
                } else {
                    user.sendMessage(Text.translatable("message.deck.empty").formatted(Formatting.RED), true);
                    return ActionResult.FAIL;
                }
            }
        }
        
        return ActionResult.PASS;
    }
    
    private ActionResult placeDeckEntityOnBlock(World world, PlayerEntity user, ItemStack stack, ItemUsageContext context) {
        // Get deck properties
        NbtCompound nbt = ItemHelper.getNBT(stack);
        byte skinId = nbt.getByte("SkinID");
        
        // Get position above the clicked block
        BlockPos pos = context.getBlockPos().up();
        Vec3d spawnPos = Vec3d.ofCenter(pos);
        
        // Create deck entity without auto-filling (we'll transfer the state manually)
        EntityCardDeck deckEntity = new EntityCardDeck(world, spawnPos, user.getYaw(), skinId, false);
        
        // Transfer the deck state from the item to the entity
        transferDeckState(stack, deckEntity);
        
        world.spawnEntity(deckEntity);
        
        // Play sound
        world.playSound(null, pos, SoundEvents.ITEM_BOOK_PUT, SoundCategory.PLAYERS, 0.5F, 1.0F);
        
        // Consume item
        stack.decrement(1);
        
        return ActionResult.SUCCESS;
    }
    
    private TypedActionResult<ItemStack> drawCardFromDeck(World world, PlayerEntity user, ItemStack stack) {
        int cardsLeft = getCardsLeft(stack);
        
        // Check if deck is empty
        if (cardsLeft <= 0) {
            user.sendMessage(Text.translatable("message.deck.empty").formatted(Formatting.RED), true);
            return TypedActionResult.fail(stack);
        }
        
        // Prevent rapid clicking by checking last draw time
        NbtCompound nbt = ItemHelper.getNBT(stack);
        long currentTime = world.getTime();
        long lastDrawTime = nbt.getLong("LastDrawTime");
        
        // Prevent drawing more than once per tick (20 times per second)
        if (currentTime - lastDrawTime < 1) {
            return TypedActionResult.fail(stack);
        }
        
        nbt.putLong("LastDrawTime", currentTime);
        
        // Create a covered card from the deck
        ItemStack card = createCardFromDeck(stack);
        
        // Try to add card to player's inventory
        if (!user.getInventory().insertStack(card)) {
            // If inventory is full, drop the card
            user.dropItem(card, false);
        }
        
        // No need to manually update CardsLeft - getCardsLeft() now calculates it automatically
        // from the shuffled deck state (CurrentIndex was already incremented in getNextCardId)
        
        return TypedActionResult.success(stack);
    }

    /**
     * Collect cards from player's inventory back into the deck
     */
    private TypedActionResult<ItemStack> collectCardsFromInventory(World world, PlayerEntity user, ItemStack deck) {
        int cardsLeft = getCardsLeft(deck);
        int maxCards = 52;
        int cardsToCollect = maxCards - cardsLeft;
        
        if (cardsToCollect <= 0) {
            user.sendMessage(Text.translatable("message.deck.full").formatted(Formatting.RED), true);
            return TypedActionResult.fail(deck);
        }
        
        int cardsCollected = 0;
        java.util.List<Integer> collectedCardIds = new java.util.ArrayList<>();
        
        // Search through player's inventory for matching cards
        for (int i = 0; i < user.getInventory().size(); i++) {
            ItemStack slot = user.getInventory().getStack(i);
            
            if (canCollectCard(deck, slot)) {
                // Get the card ID to track what we're collecting
                NbtCompound cardNbt = ItemHelper.getNBT(slot);
                int cardId = cardNbt.getInt("CardID");
                
                // Check if we already collected this card (prevent duplicates)
                if (!collectedCardIds.contains(cardId)) {
                    // This card matches our deck and is valid, collect it
                    slot.decrement(1);
                    cardsCollected++;
                    collectedCardIds.add(cardId);
                    cardsToCollect--;
                    
                    if (cardsToCollect <= 0) {
                        break; // Deck is full
                    }
                }
            }
        }
        
        if (cardsCollected > 0) {
            // Update the deck state properly by decreasing the CurrentIndex
            // This effectively "adds" cards back to the deck
            addCardsBackToDeck(deck, cardsCollected);
            
            // Log what cards were collected
            String deckUUID = getDeckUUID(deck);
            com.ombremoon.playingcards.PCReference.LOGGER.info("Collected {} cards back into deck {}. Card IDs: {}", 
                cardsCollected, deckUUID.substring(0, 8), collectedCardIds);
                
            user.sendMessage(Text.translatable("message.deck.collected", cardsCollected)
                    .formatted(Formatting.GREEN), true);
            return TypedActionResult.success(deck);
        } else {
            user.sendMessage(Text.translatable("message.deck.no_cards").formatted(Formatting.RED), true);
            return TypedActionResult.fail(deck);
        }
    }

    /**
     * Add cards back to the deck by decreasing the CurrentIndex
     * This effectively "uncollects" previously drawn cards
     * @param deck The deck ItemStack
     * @param cardsToAdd Number of cards to add back
     */
    private static void addCardsBackToDeck(ItemStack deck, int cardsToAdd) {
        NbtCompound nbt = ItemHelper.getNBT(deck);
        
        // Initialize shuffled deck if it doesn't exist
        if (!nbt.contains("ShuffledDeck")) {
            initializeShuffledDeck(deck);
        }
        
        int currentIndex = nbt.getInt("CurrentIndex");
        
        // Calculate new index (cannot go below 0)
        int newIndex = Math.max(0, currentIndex - cardsToAdd);
        int actualCardsAdded = currentIndex - newIndex;
        
        // Update the current index
        nbt.putInt("CurrentIndex", newIndex);
        
        com.ombremoon.playingcards.PCReference.LOGGER.info("Added {} cards back to deck. Index changed from {} to {}. New cards left: {}", 
            actualCardsAdded, currentIndex, newIndex, getCardsLeft(deck));
    }

    /**
     * Check if a card belongs to a specific deck and can be collected
     */
    private boolean isCardFromDeck(ItemStack cardStack, byte deckSkinId) {
        // Check if it's a covered card or playing card
        if (cardStack.getItem() != com.ombremoon.playingcards.init.ModItems.CARD_COVERED && 
            cardStack.getItem() != com.ombremoon.playingcards.init.ModItems.CARD) {
            return false;
        }
        
        // Check if skin matches
        NbtCompound nbt = ItemHelper.getNBT(cardStack);
        byte cardSkinId = nbt.getByte("SkinID");
        
        return cardSkinId == deckSkinId;
    }
    
    /**
     * Check if a specific card can be collected back into this deck
     * This prevents duplicate cards and ensures only the deck's own cards are collected
     */
    private boolean canCollectCard(ItemStack deck, ItemStack cardStack) {
        if (!isCardFromDeck(cardStack, getSkinId(deck))) {
            return false;
        }
        
        NbtCompound deckNbt = ItemHelper.getNBT(deck);
        NbtCompound cardNbt = ItemHelper.getNBT(cardStack);
        
        // Check if this card came from this specific deck using UUID
        String deckUUID = getDeckUUID(deck);
        String cardDeckUUID = cardNbt.getString("DeckUUID");
        
        // If the card doesn't have a deck UUID (old cards), fall back to old logic
        if (cardDeckUUID.isEmpty()) {
            // Get the card ID from the card
            int cardId = cardNbt.getInt("CardID");
            
            // If no shuffled deck exists, we can't validate - allow collection
            if (!deckNbt.contains("ShuffledDeck")) {
                return true;
            }
            
            int[] shuffledDeck = deckNbt.getIntArray("ShuffledDeck");
            int currentIndex = deckNbt.getInt("CurrentIndex");
            
            // Check if this card ID exists in the "drawn" portion of the deck
            // (cards that were drawn are from index 0 to currentIndex-1)
            for (int i = 0; i < currentIndex; i++) {
                if (shuffledDeck[i] == cardId) {
                    return true; // This card was drawn from this deck
                }
            }
            
            return false; // This card doesn't belong to this deck's drawn cards
        }
        
        // Use UUID matching for new cards
        return deckUUID.equals(cardDeckUUID);
    }

    /**
     * Get the number of cards left in the deck
     * @param stack The ItemStack
     * @return Number of cards left (0-52)
     */
    public static int getCardsLeft(ItemStack stack) {
        NbtCompound nbt = ItemHelper.getNBT(stack);
        
        // If we have a shuffled deck, calculate cards left from the current index
        if (nbt.contains("ShuffledDeck") && nbt.contains("CurrentIndex")) {
            int[] shuffledDeck = nbt.getIntArray("ShuffledDeck");
            int currentIndex = nbt.getInt("CurrentIndex");
            return Math.max(0, shuffledDeck.length - currentIndex);
        }
        
        // Fallback: use stored CardsLeft value
        return nbt.contains("CardsLeft") ? nbt.getInt("CardsLeft") : 52;
    }

    /**
     * Set the number of cards left in the deck
     * @param stack The ItemStack
     * @param cardsLeft Number of cards left (0-52)
     */
    public static void setCardsLeft(ItemStack stack, int cardsLeft) {
        ItemHelper.getNBT(stack).putInt("CardsLeft", Math.max(0, Math.min(52, cardsLeft)));
    }

    /**
     * Create a covered card from the deck
     * @param deck The deck ItemStack
     * @return New covered card ItemStack
     */
    public static ItemStack createCardFromDeck(ItemStack deck) {
        NbtCompound deckNbt = ItemHelper.getNBT(deck);
        byte skinId = deckNbt.getByte("SkinID");
        
        // Get the next unique card ID
        int nextCardId = getNextCardId(deck);
        
        // Ensure card ID is in valid range
        nextCardId = Math.max(0, Math.min(51, nextCardId));
        
        // Create covered card
        ItemStack card = new ItemStack(com.ombremoon.playingcards.init.ModItems.CARD_COVERED);
        NbtCompound cardNbt = ItemHelper.getNBT(card);
        
        // Set card properties in NBT
        cardNbt.putInt("CardID", nextCardId);
        cardNbt.putByte("SkinID", skinId);
        cardNbt.putBoolean("Covered", true);
        
        // Add deck UUID to track which deck this card came from
        String deckUUID = getDeckUUID(deck);
        cardNbt.putString("DeckUUID", deckUUID);
        
        // IMPORTANT: Set damage value for compatibility (this is what getCardId() reads)
        card.setDamage(nextCardId);
        
        // Set CustomModelData for texture variants
        card.getOrCreateNbt().putInt("CustomModelData", skinId);
        
        // Debug logging
        com.ombremoon.playingcards.PCReference.LOGGER.info("Created unique card with ID: {} ({}), damage: {}", 
            nextCardId, com.ombremoon.playingcards.util.CardHelper.getCardName(nextCardId).getString(),
            card.getDamage());
        
        return card;
    }

    /**
     * Get or create a unique UUID for this deck
     * @param deck The deck ItemStack
     * @return Unique deck UUID string
     */
    private static String getDeckUUID(ItemStack deck) {
        NbtCompound nbt = ItemHelper.getNBT(deck);
        
        if (!nbt.contains("DeckUUID")) {
            // Generate a new UUID for this deck
            String uuid = java.util.UUID.randomUUID().toString();
            nbt.putString("DeckUUID", uuid);
            com.ombremoon.playingcards.PCReference.LOGGER.info("Generated new deck UUID: {}", uuid);
        }
        
        return nbt.getString("DeckUUID");
    }

    /**
     * Get the next card ID from the deck (ensures unique cards)
     * @param deck The deck ItemStack
     * @return Card ID (0-51)
     */
    private static int getNextCardId(ItemStack deck) {
        NbtCompound nbt = ItemHelper.getNBT(deck);
        
        // Initialize shuffled deck if it doesn't exist
        if (!nbt.contains("ShuffledDeck")) {
            initializeShuffledDeck(deck);
        }
        
        // Get the current card index
        int currentIndex = nbt.getInt("CurrentIndex");
        int[] shuffledDeck = nbt.getIntArray("ShuffledDeck");
        int cardsLeft = getCardsLeft(deck); // This now calculates from shuffled deck automatically
        
        // If we've reached the end or the deck is empty, we can't draw more cards
        if (currentIndex >= shuffledDeck.length || currentIndex >= 52 || cardsLeft <= 0) {
            com.ombremoon.playingcards.PCReference.LOGGER.warn("Attempted to draw from empty deck! Index: {}, Deck length: {}, Cards left: {}", currentIndex, shuffledDeck.length, cardsLeft);
            return 0; // Return first card as fallback
        }
        
        int cardId = shuffledDeck[currentIndex];
        
        // Increment index for next draw
        nbt.putInt("CurrentIndex", currentIndex + 1);
        
        com.ombremoon.playingcards.PCReference.LOGGER.info("Deck drawing card ID: {} (index {}), cards left: {}", cardId, currentIndex, cardsLeft - 1);
        
        return cardId;
    }
    
    /**
     * Initialize a shuffled deck with all 52 unique cards
     * @param deck The deck ItemStack
     */
    private static void initializeShuffledDeck(ItemStack deck) {
        NbtCompound nbt = ItemHelper.getNBT(deck);
        
        // Create array with all 52 cards (0-51)
        int[] cards = new int[52];
        for (int i = 0; i < 52; i++) {
            cards[i] = i;
        }
        
        // Shuffle the array using Fisher-Yates algorithm
        java.util.Random random = new java.util.Random();
        for (int i = cards.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = cards[i];
            cards[i] = cards[j];
            cards[j] = temp;
        }
        
        // Store shuffled deck and reset index
        nbt.putIntArray("ShuffledDeck", cards);
        nbt.putInt("CurrentIndex", 0);
        
        com.ombremoon.playingcards.PCReference.LOGGER.info("Initialized new shuffled deck with {} cards", cards.length);
    }
    
    /**
     * Debug method to verify deck integrity
     * @param deck The deck ItemStack
     */
    private static void verifyDeckIntegrity(ItemStack deck) {
        NbtCompound nbt = ItemHelper.getNBT(deck);
        if (nbt.contains("ShuffledDeck")) {
            int[] shuffledDeck = nbt.getIntArray("ShuffledDeck");
            int currentIndex = nbt.getInt("CurrentIndex");
            int cardsLeft = getCardsLeft(deck);
            
            com.ombremoon.playingcards.PCReference.LOGGER.info("Deck integrity check: {} cards in deck, index at {}, {} cards left", 
                shuffledDeck.length, currentIndex, cardsLeft);
            
            // Verify all cards are unique
            java.util.Set<Integer> uniqueCards = new java.util.HashSet<>();
            for (int card : shuffledDeck) {
                if (!uniqueCards.add(card)) {
                    com.ombremoon.playingcards.PCReference.LOGGER.warn("Duplicate card found: {}", card);
                }
            }
            
            if (uniqueCards.size() != 52) {
                com.ombremoon.playingcards.PCReference.LOGGER.warn("Expected 52 unique cards, found {}", uniqueCards.size());
            }
        }
    }

    /**
     * Get the deck's skin ID
     * @param stack The ItemStack
     * @return Skin ID (0-3)
     */
    public static byte getSkinId(ItemStack stack) {
        return ItemHelper.getNBT(stack).getByte("SkinID");
    }

    /**
     * Set the deck's skin ID
     * @param stack The ItemStack
     * @param skinId Skin ID (0-3)
     */
    public static void setSkinId(ItemStack stack, byte skinId) {
        ItemHelper.getNBT(stack).putByte("SkinID", skinId);
        // Set CustomModelData to match skin for texture variants
        stack.getOrCreateNbt().putInt("CustomModelData", skinId);
    }

    /**
     * Create a new deck with the specified skin
     * @param skinId Skin ID (0-3)
     * @return New deck ItemStack
     */
    public static ItemStack createDeck(byte skinId) {
        ItemStack stack = new ItemStack(com.ombremoon.playingcards.init.ModItems.CARD_DECK);
        setSkinId(stack, skinId);
        setCardsLeft(stack, 52); // Initialize with full deck
        
        // Initialize the shuffled deck with all 52 unique cards
        initializeShuffledDeck(stack);
        
        return stack;
    }

    /**
     * Transfer deck state from item to entity
     * @param deckItem The deck ItemStack
     * @param deckEntity The deck entity
     */
    private static void transferDeckState(ItemStack deckItem, EntityCardDeck deckEntity) {
        NbtCompound itemNbt = ItemHelper.getNBT(deckItem);
        int cardsLeft = getCardsLeft(deckItem);
        
        // Clear the entity's stack first (it starts with 52 cards by default)
        deckEntity.createStack();
        
        // If the deck has a shuffled deck and current index, transfer the remaining cards
        if (itemNbt.contains("ShuffledDeck") && itemNbt.contains("CurrentIndex")) {
            int[] shuffledDeck = itemNbt.getIntArray("ShuffledDeck");
            int currentIndex = itemNbt.getInt("CurrentIndex");
            
            // Store the original deck state in the entity for exact preservation
            deckEntity.setOriginalDeckState(shuffledDeck, currentIndex);
            
            // Calculate actual remaining cards from the shuffled deck
            int actualRemainingCards = shuffledDeck.length - currentIndex;
            
            // Validate and sync the cardsLeft with actual deck state
            if (actualRemainingCards != cardsLeft) {
                com.ombremoon.playingcards.PCReference.LOGGER.warn("Card count mismatch! CardsLeft: {}, Actual remaining: {}. Syncing...", cardsLeft, actualRemainingCards);
                setCardsLeft(deckItem, actualRemainingCards);
                cardsLeft = actualRemainingCards;
            }
            
            // Transfer the remaining cards in reverse order
            // The item stores cards to draw next at the beginning of the remaining cards
            // The entity stores cards with the bottom card at index 0 and top card at the last index
            // So we need to reverse the order when transferring to entity
            for (int i = currentIndex + actualRemainingCards - 1; i >= currentIndex; i--) {
                deckEntity.addToTop((byte) shuffledDeck[i]);
            }
            
            com.ombremoon.playingcards.PCReference.LOGGER.info("Transferred {} cards from deck item to entity (index {} to {})", actualRemainingCards, currentIndex, currentIndex + actualRemainingCards - 1);
        } else {
            // Fallback: create a new shuffled deck for the entity
            com.ombremoon.playingcards.PCReference.LOGGER.info("No deck state found in item, creating new shuffled deck for entity with {} cards", cardsLeft);
            
            // Create a new shuffled deck with the remaining cards
            java.util.List<Integer> remainingCardsList = new java.util.ArrayList<>();
            for (int i = 0; i < cardsLeft; i++) {
                remainingCardsList.add(i);
            }
            
            // Shuffle the remaining cards
            java.util.Collections.shuffle(remainingCardsList);
            
            // Add shuffled cards to entity (in reverse order so the last added is drawn first)
            for (int i = remainingCardsList.size() - 1; i >= 0; i--) {
                deckEntity.addToTop((byte) remainingCardsList.get(i).intValue());
            }
        }
        
        // Final validation
        int entityStackSize = deckEntity.getStackAmount();
        if (entityStackSize != cardsLeft) {
            com.ombremoon.playingcards.PCReference.LOGGER.error("Stack transfer failed! Expected: {}, Got: {}", cardsLeft, entityStackSize);
        }
    }
}
