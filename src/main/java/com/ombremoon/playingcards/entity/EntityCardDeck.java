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

public class EntityCardDeck extends EntityStacked {

    private static final TrackedData<Float> ROTATION = DataTracker.registerData(EntityCardDeck.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Byte> SKIN_ID = DataTracker.registerData(EntityCardDeck.class, TrackedDataHandlerRegistry.BYTE);
    
    private long lastDrawTime = 0; // Track last time a card was drawn
    
    // Store the original deck state to preserve it exactly
    private int[] originalShuffledDeck = null;
    private int originalCurrentIndex = 0;

    public EntityCardDeck(EntityType<? extends EntityCardDeck> type, World world) {
        super(type, world);
    }

    public EntityCardDeck(World world, Vec3d position, float rotation, byte skinID) {
        super(ModEntityTypes.CARD_DECK, world, position);

        createAndFillDeck();
        shuffleStack();

        // Set data after the entity is fully constructed
        this.getDataTracker().set(ROTATION, rotation);
        this.getDataTracker().set(SKIN_ID, skinID);
    }

    public EntityCardDeck(World world, Vec3d position, float rotation, byte skinID, boolean autoFill) {
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
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(ROTATION, 0.0F);
        this.getDataTracker().startTracking(SKIN_ID, (byte) 0);
    }

    public float getRotation() {
        return this.getDataTracker().get(ROTATION);
    }

    public byte getSkinID() {
        return this.getDataTracker().get(SKIN_ID);
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
        // Debug logging
        com.ombremoon.playingcards.PCReference.LOGGER.info("EntityCardDeck.interact() called by player: {}, hand: {}, stack amount: {}", 
            player.getName().getString(), hand, getStackAmount());
        
        if (hand == Hand.MAIN_HAND) {
            if (getStackAmount() > 0) {
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
            } else {
                if (!this.getWorld().isClient) {
                    player.sendMessage(Text.translatable("message.deck.empty").formatted(Formatting.RED), true);
                }
                return ActionResult.FAIL;
            }
        }
        
        return ActionResult.FAIL;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            if (player.isSneaking()) {
                if (!this.getWorld().isClient) {
                    takeEntireDeck(player);
                }
            } else {
                shuffleStack();
                if (this.getWorld().isClient) {
                    player.sendMessage(Text.translatable("message.deck.shuffled").formatted(Formatting.GREEN), true);
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

        // Create a card item like the original
        ItemStack card = new ItemStack(ModItems.CARD_COVERED);
        card.setDamage(getTopStackID());
        ItemHelper.getNBT(card).putString("UUID", this.getUuidAsString()); // Use string UUID like original
        ItemHelper.getNBT(card).putByte("SkinID", getSkinID());
        ItemHelper.getNBT(card).putBoolean("Covered", true);
        
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
        int stackAmount = getStackAmount();
        ItemStack deckStack = new ItemStack(ModItems.CARD_DECK);
        
        // Set NBT data
        NbtCompound nbt = ItemHelper.getNBT(deckStack);
        nbt.putByte("SkinID", getSkinID());
        nbt.putInt("CardsLeft", stackAmount);
        
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
        
        // Auto-collect nearby cards of the same deck every 20 ticks (1 second)
        if (this.getWorld().getTime() % 20 == 0 && !this.getWorld().isClient) {
            collectNearbyCards();
        }
    }

    private void collectNearbyCards() {
        // Find nearby EntityCard entities within a 2-block radius
        this.getWorld().getEntitiesByClass(EntityCard.class, this.getBoundingBox().expand(2.0), 
            card -> card.getDeckUUID() != null)
            .forEach(card -> {
                // Add the card to this deck
                addToTop(card.getTopStackID());
                card.discard();
                
                // Play a subtle sound
                this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 0.2F, 1.5F);
            });
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("Rotation", this.getDataTracker().get(ROTATION));
        nbt.putByte("SkinID", this.getDataTracker().get(SKIN_ID));
        
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
