package com.ombremoon.playingcards.entity;

import com.ombremoon.playingcards.entity.base.EntityStacked;
import com.ombremoon.playingcards.init.ModEntityTypes;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.util.ItemHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class EntityCardDeck extends EntityStacked {

    private static final TrackedData<Float> ROTATION = DataTracker.registerData(EntityCardDeck.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Byte> SKIN_ID = DataTracker.registerData(EntityCardDeck.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> FACE_UP_MODE = DataTracker.registerData(EntityCardDeck.class, TrackedDataHandlerRegistry.BOOLEAN);
    
    private long lastDrawTime = 0; // Track last time a card was drawn
    
    // Store the original deck state to preserve it exactly
    private int[] originalShuffledDeck = null;
    private int originalCurrentIndex = 0;

    public EntityCardDeck(EntityType<? extends EntityCardDeck> type, World world) {
        super(type, world);
    }

    public EntityCardDeck(World world, Vec3d position, float rotation, byte skinID) {
        this(world, position, rotation, skinID, true, false);
    }

    public EntityCardDeck(World world, Vec3d position, float rotation, byte skinID, boolean autoFill) {
        this(world, position, rotation, skinID, autoFill, false);
    }

    public EntityCardDeck(World world, Vec3d position, float rotation, byte skinID, boolean autoFill, boolean faceUpMode) {
        super(ModEntityTypes.CARD_DECK, world, position);

        if (autoFill) {
            createAndFillDeck();
            shuffleStack();
        } else {
            // Just create an empty stack
            createStack();
        }

        // Set data after the entity is fully constructed
        this.getDataTracker().set(ROTATION, rotation);
        this.getDataTracker().set(SKIN_ID, skinID);
        this.getDataTracker().set(FACE_UP_MODE, faceUpMode);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(ROTATION, 0.0F);
        this.getDataTracker().startTracking(SKIN_ID, (byte) 0);
        this.getDataTracker().startTracking(FACE_UP_MODE, false);
    }

    public float getRotation() {
        return this.getDataTracker().get(ROTATION);
    }

    public byte getSkinID() {
        return this.getDataTracker().get(SKIN_ID);
    }

    public boolean isFaceUpMode() {
        return this.getDataTracker().get(FACE_UP_MODE);
    }

    // Method to set the original deck state when created from an item
    public void setOriginalDeckState(int[] shuffledDeck, int currentIndex) {
        this.originalShuffledDeck = shuffledDeck.clone(); // Clone to prevent external modification
        this.originalCurrentIndex = currentIndex;
    }

    // Method to check if we have original deck state
    public boolean hasOriginalDeckState() {
        return this.originalShuffledDeck != null;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            ItemStack heldItem = player.getStackInHand(hand);
            
            // Check if player is sneaking (shift + right-click)
            if (player.isSneaking()) {
                if (!this.getWorld().isClient) {
                    // Manual card collection
                    manualCollectCards(player);
                }
                return ActionResult.SUCCESS;
            }
            
            // If player is holding a card, try to add it to the deck
            if (heldItem.getItem() instanceof com.ombremoon.playingcards.item.ItemCardCovered) {
                return handleCardAddition(player, heldItem);
            }
            
            // If deck has cards and player is empty-handed, draw a card
            if (heldItem.isEmpty() && getStackAmount() > 0) {
                if (!this.getWorld().isClient) {
                    // Only allow one interaction per player at a time
                    synchronized (this) {
                        if (getStackAmount() > 0) { // Double-check inside synchronized block
                            com.ombremoon.playingcards.PCReference.LOGGER.info("Drawing card from placed deck for player: {}", player.getName().getString());
                            drawCard(player);
                        } else {
                            player.sendMessage(Text.translatable("message.deck.empty").formatted(Formatting.RED), true);
                            return ActionResult.FAIL;
                        }
                    }
                }
                // Always return SUCCESS when we attempt to draw a card
                return ActionResult.SUCCESS;
            } else if (getStackAmount() <= 0) {
                if (!this.getWorld().isClient) {
                    player.sendMessage(Text.translatable("message.deck.empty").formatted(Formatting.RED), true);
                }
                return ActionResult.FAIL;
            }
        }
        
        return ActionResult.PASS;
    }
    
    /**
     * Handle adding a card to the deck (like the original)
     */
    private ActionResult handleCardAddition(PlayerEntity player, ItemStack cardStack) {
        if (!this.getWorld().isClient) {
            // Check if deck is full
            if (getStackAmount() >= MAX_STACK_SIZE) {
                player.sendMessage(Text.translatable("message.stack_full").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
            
            // Check if card belongs to this deck (UUID validation like original)
            NbtCompound cardNbt = ItemHelper.getNBT(cardStack);
            String cardDeckUUID = cardNbt.getString("UUID");
            
            if (!cardDeckUUID.isEmpty() && !cardDeckUUID.equals(this.getUuidAsString())) {
                player.sendMessage(Text.translatable("message.deck.wrong_deck").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
            
            // Add card to deck
            addToTop((byte) cardStack.getDamage());
            cardStack.decrement(1);
            
            // Play sound
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PUT, SoundCategory.PLAYERS, 0.5F, 1.2F);
            
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.CONSUME;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            if (player.isSneaking()) {
                // Crouch + attack: Pick up entire deck
                if (!this.getWorld().isClient) {
                    takeEntireDeck(player);
                }
            } else {
                // Attack: Shuffle deck (like original)
                if (!this.getWorld().isClient) {
                    if (getStackAmount() > 0) {
                        shuffleStack();
                        player.sendMessage(Text.translatable("message.stack_shuffled").formatted(Formatting.GREEN), true);
                        
                        // Play shuffle sound
                        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0F, 0.8F);
                    } else {
                        player.sendMessage(Text.translatable("message.stack_empty").formatted(Formatting.RED), true);
                    }
                }
            }
            return true;
        }
        
        return false;
    }

    private void drawCard(PlayerEntity player) {
        if (getStackAmount() <= 0) {
            player.sendMessage(Text.translatable("message.deck.empty").formatted(Formatting.RED), true);
            return;
        }

        // Prevent rapid clicking by checking world time
        long currentTime = this.getWorld().getTime();
        if (this.getWorld().isClient) {
            // On client side, just play the interaction
            return;
        }
        
        // Server-side: check if enough time has passed since last draw
        if (currentTime - this.lastDrawTime < 10) { // Minimum 10 ticks (0.5 seconds) between draws
            // Give feedback that they're clicking too fast
            if (currentTime - this.lastDrawTime > 5) { // Only show message if they waited at least 5 ticks
                player.sendMessage(Text.literal("Drawing too fast! Wait a moment.").formatted(Formatting.YELLOW), true);
            }
            return;
        }
        this.lastDrawTime = currentTime;

        // Double-check stack amount again to prevent race conditions
        if (getStackAmount() <= 0) {
            player.sendMessage(Text.translatable("message.deck.empty").formatted(Formatting.RED), true);
            return;
        }

        // Create a card item based on deck's face-up mode
        ItemStack card;
        boolean faceUpMode = isFaceUpMode();
        
        if (faceUpMode) {
            // Face-up deck - create face-up card
            card = new ItemStack(ModItems.CARD);
            card.setDamage(getTopStackID());
            ItemHelper.getNBT(card).putString("UUID", this.getUuidAsString());
            ItemHelper.getNBT(card).putByte("SkinID", getSkinID());
            ItemHelper.getNBT(card).putInt("CustomModelData", 100 + getTopStackID()); // Face-up texture
            ItemHelper.getNBT(card).putBoolean("Covered", false);
        } else {
            // Face-down deck - create covered card
            card = new ItemStack(ModItems.CARD_COVERED);
            card.setDamage(getTopStackID());
            ItemHelper.getNBT(card).putString("UUID", this.getUuidAsString());
            ItemHelper.getNBT(card).putByte("SkinID", getSkinID());
            ItemHelper.getNBT(card).putInt("CustomModelData", getSkinID()); // Card back texture
            ItemHelper.getNBT(card).putBoolean("Covered", true);
        }
        
        // Spawn the card at the player using original method
        ItemHelper.spawnStackAtEntity(this.getWorld(), player, card);
        
        // Play sound
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 0.5F, 1.0F);

        // Store the stack amount before removing to validate
        int stackAmountBefore = getStackAmount();
        removeFromTop();
        int stackAmountAfter = getStackAmount();
        
        // Validate that the card was properly removed
        if (stackAmountAfter != stackAmountBefore - 1) {
            com.ombremoon.playingcards.PCReference.LOGGER.error("Card removal failed! Before: {}, After: {}, Expected: {}", 
                stackAmountBefore, stackAmountAfter, stackAmountBefore - 1);
        }

        if (getStackAmount() <= 0) {
            this.discard();
        }
    }

    private void takeEntireDeck(PlayerEntity player) {
        // First, collect all cards belonging to this deck from the world and players
        collectAllDeckCards(player);
        
        int stackAmount = getStackAmount();
        ItemStack deckStack = new ItemStack(ModItems.CARD_DECK);
        
        // Set NBT data
        NbtCompound nbt = ItemHelper.getNBT(deckStack);
        nbt.putByte("SkinID", getSkinID());
        nbt.putInt("CustomModelData", getSkinID()); // Add CustomModelData for texture variants
        nbt.putInt("CardsLeft", stackAmount);
        nbt.putBoolean("FaceUp", isFaceUpMode()); // Preserve face-up state
        
        if (hasOriginalDeckState()) {
            // Use the original deck state to preserve exact deck order
            nbt.putIntArray("ShuffledDeck", originalShuffledDeck);
            
            // Calculate how many cards were drawn since placement
            int cardsDrawn = (52 - originalCurrentIndex) - stackAmount;
            int currentIndex = originalCurrentIndex + cardsDrawn;
            nbt.putInt("CurrentIndex", currentIndex);
            
            com.ombremoon.playingcards.PCReference.LOGGER.info("Converting entity deck to item: {} cards remaining, original deck state preserved (index: {})", stackAmount, currentIndex);
        } else if (stackAmount > 0) {
            // Fallback: reconstruct deck state from current entity cards
            // Get the current stack order from the entity
            int[] entityCards = new int[stackAmount];
            for (int i = 0; i < stackAmount; i++) {
                // Reverse the order: last card in entity stack becomes first card to draw
                entityCards[stackAmount - 1 - i] = getIDAt(i);
            }
            
            // Create the shuffled deck array: remaining cards first, then missing cards
            int[] shuffledDeck = new int[52];
            
            // Add the current stack to the beginning of the shuffled deck
            System.arraycopy(entityCards, 0, shuffledDeck, 0, stackAmount);
            
            // Fill in the missing cards (ones that were already drawn)
            java.util.Set<Integer> existingCards = new java.util.HashSet<>();
            for (int cardId : entityCards) {
                existingCards.add(cardId);
            }
            
            int nextIndex = stackAmount;
            for (int cardId = 0; cardId < 52 && nextIndex < 52; cardId++) {
                if (!existingCards.contains(cardId)) {
                    shuffledDeck[nextIndex++] = cardId;
                }
            }
            
            // Store as shuffled deck with current index at 0 (pointing to the first remaining card)
            nbt.putIntArray("ShuffledDeck", shuffledDeck);
            nbt.putInt("CurrentIndex", 0);
            
            com.ombremoon.playingcards.PCReference.LOGGER.info("Converting entity deck to item: {} cards remaining, deck state reconstructed", stackAmount);
        } else {
            // Empty deck
            if (hasOriginalDeckState()) {
                // Use original deck but mark as empty
                nbt.putIntArray("ShuffledDeck", originalShuffledDeck);
                nbt.putInt("CurrentIndex", 52);
            } else {
                // Create a full shuffled deck at the end
                int[] shuffledDeck = new int[52];
                for (int i = 0; i < 52; i++) {
                    shuffledDeck[i] = i;
                }
                nbt.putIntArray("ShuffledDeck", shuffledDeck);
                nbt.putInt("CurrentIndex", 52);
            }
            
            com.ombremoon.playingcards.PCReference.LOGGER.info("Converting empty entity deck to item");
        }

        // Spawn deck at player using original method
        ItemHelper.spawnStackAtEntity(this.getWorld(), player, deckStack);

        // Play sound
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PUT, SoundCategory.PLAYERS, 0.5F, 1.0F);

        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        
        // Automatic card collection removed - now manual only via shift+right-click
    }

    /**
     * Collect all cards belonging to this deck from the world and all player inventories.
     * Called when picking up the deck to ensure no cards are lost.
     */
    private void collectAllDeckCards(PlayerEntity triggeringPlayer) {
        UUID thisDeckUUID = this.getUuid();
        int cardsCollected = 0;
        
        // 1. Collect cards from the world (unlimited radius to get all cards)
        for (EntityCard card : this.getWorld().getEntitiesByClass(EntityCard.class, 
            this.getBoundingBox().expand(1000.0), // Large radius to catch all cards
            c -> {
                UUID cardDeckUUID = c.getDeckUUID();
                return cardDeckUUID != null && cardDeckUUID.equals(thisDeckUUID);
            })) {
            
            // Add the card to this deck
            addToTop(card.getTopStackID());
            card.discard();
            cardsCollected++;
        }
        
        // 2. Collect cards from all online players' inventories
        for (net.minecraft.server.network.ServerPlayerEntity serverPlayer : 
             this.getWorld().getServer().getPlayerManager().getPlayerList()) {
            
            // Check player's inventory for cards belonging to this deck
            for (int i = 0; i < serverPlayer.getInventory().size(); i++) {
                ItemStack stack = serverPlayer.getInventory().getStack(i);
                
                if (isCardFromThisDeck(stack, thisDeckUUID)) {
                    // Add all cards in this stack to the deck
                    for (int j = 0; j < stack.getCount(); j++) {
                        addToTop((byte) stack.getDamage());
                        cardsCollected++;
                    }
                    
                    // Remove the stack from player inventory
                    serverPlayer.getInventory().setStack(i, ItemStack.EMPTY);
                }
            }
        }
        
        // Send feedback message about cards collected
        if (cardsCollected > 0) {
            triggeringPlayer.sendMessage(Text.literal("Collected " + cardsCollected + " cards back into the deck")
                .formatted(Formatting.GREEN), false);
            
            // Play collection sound
            this.getWorld().playSound(null, this.getBlockPos(), 
                SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0F, 1.2F);
        }
    }
    
    /**
     * Check if an item stack is a card that belongs to this deck
     */
    private boolean isCardFromThisDeck(ItemStack stack, UUID thisDeckUUID) {
        if (stack.isEmpty()) return false;
        
        // Check if it's a card item (covered or uncovered)
        if (!(stack.getItem() instanceof com.ombremoon.playingcards.item.ItemCardCovered)) {
            return false;
        }
        
        // Check UUID in NBT data
        NbtCompound nbt = ItemHelper.getNBT(stack);
        if (nbt.contains("UUID")) {
            try {
                UUID cardDeckUUID = nbt.getUuid("UUID");
                return thisDeckUUID.equals(cardDeckUUID);
            } catch (IllegalArgumentException e) {
                // Try string UUID format as fallback
                try {
                    String uuidString = nbt.getString("UUID");
                    if (!uuidString.isEmpty()) {
                        UUID cardDeckUUID = UUID.fromString(uuidString);
                        return thisDeckUUID.equals(cardDeckUUID);
                    }
                } catch (Exception ex) {
                    // Invalid UUID format
                }
            }
        }
        
        return false;
    }

    private void manualCollectCards(PlayerEntity player) {
        // Find nearby EntityCard entities within a 2-block radius that belong to THIS deck
        UUID thisDeckUUID = this.getUuid();
        int cardsCollected = 0;
        
        for (EntityCard card : this.getWorld().getEntitiesByClass(EntityCard.class, this.getBoundingBox().expand(2.0), 
            c -> {
                UUID cardDeckUUID = c.getDeckUUID();
                // Only collect cards that belong to this specific deck
                return cardDeckUUID != null && cardDeckUUID.equals(thisDeckUUID);
            })) {
            
            // Add the card to this deck
            addToTop(card.getTopStackID());
            card.discard();
            cardsCollected++;
            
            // Play a subtle sound
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 0.2F, 1.5F);
        }
        
        // Send feedback to player
        if (cardsCollected > 0) {
            player.sendMessage(Text.literal("Collected " + cardsCollected + " nearby cards back into the deck")
                .formatted(Formatting.GREEN), true);
        } else {
            player.sendMessage(Text.literal("No cards found nearby to collect")
                .formatted(Formatting.YELLOW), true);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("Rotation", this.getDataTracker().get(ROTATION));
        nbt.putByte("SkinID", this.getDataTracker().get(SKIN_ID));
        nbt.putBoolean("FaceUpMode", this.getDataTracker().get(FACE_UP_MODE));
        
        // Save original deck state
        if (originalShuffledDeck != null) {
            nbt.putIntArray("OriginalShuffledDeck", originalShuffledDeck);
            nbt.putInt("OriginalCurrentIndex", originalCurrentIndex);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.getDataTracker().set(ROTATION, nbt.getFloat("Rotation"));
        this.getDataTracker().set(SKIN_ID, nbt.getByte("SkinID"));
        this.getDataTracker().set(FACE_UP_MODE, nbt.getBoolean("FaceUpMode"));
        
        // Load original deck state
        if (nbt.contains("OriginalShuffledDeck")) {
            originalShuffledDeck = nbt.getIntArray("OriginalShuffledDeck");
            originalCurrentIndex = nbt.getInt("OriginalCurrentIndex");
        }
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public void moreData() {
        // Data tracking is handled in initDataTracker() - no need to duplicate here
    }
}
