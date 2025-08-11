package com.toastie01.casino.entity;

import com.toastie01.casino.PCReference;
import com.toastie01.casino.entity.base.EntityStacked;
import com.toastie01.casino.init.ModEntityTypes;
import com.toastie01.casino.init.ModItems;
import com.toastie01.casino.item.ItemCardDeck;
import com.toastie01.casino.util.ItemHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class EntityCardDeck extends EntityStacked {

    private static final TrackedData<Float> ROTATION = DataTracker.registerData(EntityCardDeck.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Byte> SKIN_ID = DataTracker.registerData(EntityCardDeck.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> FACE_UP_MODE = DataTracker.registerData(EntityCardDeck.class, TrackedDataHandlerRegistry.BOOLEAN);
    
    private long lastDrawTime = 0; // Track last time a card was drawn

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
        super(ModEntityTypes.CARD_DECK, world);
        this.setPosition(position.x, position.y, position.z);
        this.setRotation(rotation);
        this.setSkinID(skinID);
        this.setFaceUpMode(faceUpMode);
        
        // Create and fill deck if requested
        if (autoFill) {
            createAndFillDeck();
            shuffleStack();
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        // Data tracker registration is handled in moreData() to prevent duplicates
    }

    @Override
    public void moreData() {
        this.dataTracker.startTracking(ROTATION, 0.0F);
        this.dataTracker.startTracking(SKIN_ID, (byte) 0);
        this.dataTracker.startTracking(FACE_UP_MODE, false);
    }

    public float getRotation() {
        return this.dataTracker.get(ROTATION);
    }

    public void setRotation(float rotation) {
        this.dataTracker.set(ROTATION, rotation);
    }

    public byte getSkinID() {
        return this.dataTracker.get(SKIN_ID);
    }

    public void setSkinID(byte skinID) {
        this.dataTracker.set(SKIN_ID, skinID);
    }

    public boolean getFaceUpMode() {
        return this.dataTracker.get(FACE_UP_MODE);
    }

    public void setFaceUpMode(boolean faceUpMode) {
        this.dataTracker.set(FACE_UP_MODE, faceUpMode);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (this.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }

        ItemStack heldItem = player.getStackInHand(hand);
        
        // Sneak + Right-click: Collect all cards from server and shuffle
        if (player.isSneaking()) {
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                int collectedCards = collectAllCardsFromServer(serverWorld);
                if (collectedCards > 0) {
                    shuffleStack();
                    player.sendMessage(Text.translatable("message.deck.collected_and_shuffled", collectedCards).formatted(Formatting.GREEN), true);
                    
                    // Play collection and shuffle sound
                    this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PUT, SoundCategory.PLAYERS, 1.0F, 1.2F);
                    this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0F, 0.8F);
                } else {
                    player.sendMessage(Text.translatable("message.deck.no_cards_to_collect").formatted(Formatting.YELLOW), true);
                }
                return ActionResult.SUCCESS;
            }
        }
        
        // Right-click with empty hand: Draw a card
        if (heldItem.isEmpty()) {
            if (getStackAmount() > 0) {
                // Only allow one player to interact with the deck at a time
                long currentTime = this.getWorld().getTime();
                if (currentTime - lastDrawTime < 10) { // 0.5 second cooldown
                    return ActionResult.FAIL;
                }
                
                lastDrawTime = currentTime;
                
                // Create appropriate card based on deck mode
                ItemStack card = createCardFromTop();
                
                // Give card to player
                if (!player.getInventory().insertStack(card)) {
                    // If inventory is full, drop the card
                    player.dropItem(card, false);
                }
                
                // Remove card from deck
                removeFromTop();
                
                // Play sound
                this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 0.5F, 1.0F + (this.getWorld().random.nextFloat() * 0.4F - 0.2F));
                
                return ActionResult.SUCCESS;
            } else {
                player.sendMessage(Text.translatable("message.stack_empty").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
        }
        
        return ActionResult.PASS;
    }

    // handleCardAddition method removed - no longer used in simplified interaction system

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            if (!this.getWorld().isClient) {
                // Left-click/attack: Pick up entire deck
                takeEntireDeck(player);
            }
        }
        return false; // Don't actually damage the entity
    }

    private void takeEntireDeck(PlayerEntity player) {
        // First, collect all cards from the server that belong to this deck
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            int collectedCards = collectAllCardsFromServer(serverWorld);
            if (collectedCards > 0) {
                shuffleStack();
                player.sendMessage(Text.translatable("message.deck.collected_and_shuffled", collectedCards).formatted(Formatting.GREEN), true);
                
                // Play collection sound
                this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PUT, SoundCategory.PLAYERS, 1.0F, 1.2F);
            }
        }
        
        // Create a deck item with the same properties as this deck
        byte skinId = getSkinID();
        boolean faceUpMode = getFaceUpMode();
        
        // Create deck item
        ItemStack deckItem = ItemCardDeck.createDeck(skinId, faceUpMode);
        
        // Give deck to player
        if (!player.getInventory().insertStack(deckItem)) {
            player.dropItem(deckItem, false);
        }
        
        // Remove the deck entity
        this.discard();
        
        // Play pickup sound
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.5F, 1.0F);
    }

    private ItemStack createCardFromTop() {
        ItemStack card;
        
        if (getFaceUpMode()) {
            // Face-up deck - create regular card (face visible)
            card = new ItemStack(ModItems.CARD);
            card.setDamage(getTopStackID());
            ItemHelper.getNBT(card).putString("UUID", this.getUuidAsString());
            ItemHelper.getNBT(card).putByte("SkinID", getSkinID());
        } else {
            // Face-down deck - create covered card (back visible)
            card = new ItemStack(ModItems.CARD_COVERED);
            card.setDamage(getTopStackID());
            NbtCompound nbt = ItemHelper.getNBT(card);
            nbt.putString("UUID", this.getUuidAsString());
            nbt.putByte("SkinID", getSkinID());
            nbt.putBoolean("Covered", true); // Face-down
            nbt.putInt("CustomModelData", getSkinID()); // Card back texture
        }
        
        return card;
    }

    // New method to check if deck can auto-fill from player's inventory
    // REMOVED - no longer used in simplified interaction system
    
    // New method to auto-fill deck from player's inventory  
    // REMOVED - no longer used in simplified interaction system

    /**
     * Check if a card belongs to this deck by UUID
     */
    public boolean belongsToThisDeck(ItemStack stack) {
        // Check if it's a card item (both types)
        if (!(stack.getItem() instanceof com.toastie01.casino.item.ItemCard) && 
            !(stack.getItem() instanceof com.toastie01.casino.item.ItemCardCovered)) {
            return false;
        }
        
        // Check UUID in NBT data (cards use "UUID" key, not "DeckUUID")
        NbtCompound nbt = ItemHelper.getNBT(stack);
        if (nbt.contains("UUID")) {
            try {
                String cardUuidString = nbt.getString("UUID");
                return cardUuidString.equals(this.getUuidAsString());
            } catch (Exception e) {
                PCReference.LOGGER.warn("Failed to parse card UUID: " + e.getMessage());
                return false;
            }
        }
        
        return false;
    }

    // Original deck restoration method for use when deck is picked up
    public void restoreOriginalDeck() {
        // For now, just create and shuffle a new deck
        createAndFillDeck();
        shuffleStack();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("Rotation", getRotation());
        nbt.putByte("SkinID", getSkinID());
        nbt.putBoolean("FaceUpMode", getFaceUpMode());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setRotation(nbt.getFloat("Rotation"));
        setSkinID(nbt.getByte("SkinID"));
        setFaceUpMode(nbt.getBoolean("FaceUpMode"));
    }

    protected boolean canPlayerAccessInventory(PlayerEntity player) {
        return false; // Disable direct inventory access
    }

    /**
     * Manually collect cards from player's inventory that belong to this deck
     */
    public void manualCollectCards(PlayerEntity player) {
        int collected = 0;
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            
            if (stack.getItem() instanceof com.toastie01.casino.item.ItemCard) {
                // Check if this card belongs to our deck
                NbtCompound nbt = stack.getNbt();
                if (nbt != null && nbt.contains("UUID")) {
                    String cardDeckUUID = nbt.getString("UUID");
                    if (cardDeckUUID.equals(this.getUuidAsString())) {
                        // This card belongs to our deck
                        int cardValue = stack.getDamage(); // Card value stored in damage
                        
                        // Add card to deck (multiple times if stack size > 1)
                        for (int j = 0; j < stack.getCount() && getStackAmount() < MAX_STACK_SIZE; j++) {
                            addToTop((byte) cardValue);
                            collected++;
                        }
                        
                        // Remove from inventory
                        player.getInventory().setStack(i, ItemStack.EMPTY);
                        
                        if (getStackAmount() >= MAX_STACK_SIZE) {
                            break; // Deck is full
                        }
                    }
                }
            }
        }
        
        if (collected > 0) {
            player.sendMessage(Text.translatable("message.deck.collected", collected).formatted(Formatting.GREEN), true);
            // Play sound
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOOK_PUT, SoundCategory.PLAYERS, 0.5F, 1.2F);
        } else {
            player.sendMessage(Text.translatable("message.deck.no_cards_to_collect").formatted(Formatting.YELLOW), true);
        }
    }
    
    /**
     * Collect all cards from the entire server that belong to this deck
     */
    private int collectAllCardsFromServer(ServerWorld serverWorld) {
        int totalCollected = 0;
        
        // Collect from all players on the server (inventory + ender chest)
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            totalCollected += collectCardsFromPlayer(player);
            totalCollected += collectCardsFromEnderChest(player);
        }
        
        // Collect from all placed card entities in the world
        totalCollected += collectCardsFromWorld(serverWorld);
        
        // Collect from dropped item entities in the world
        totalCollected += collectCardsFromDroppedItems(serverWorld);
        
        // Collect from all storage containers in the world
        totalCollected += collectCardsFromStorageContainers(serverWorld);
        
        return totalCollected;
    }
    
    /**
     * Collect cards from a specific player's inventory
     */
    private int collectCardsFromPlayer(PlayerEntity player) {
        int collected = 0;
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            
            if (belongsToThisDeck(stack)) {
                // This card belongs to our deck
                int cardValue = stack.getDamage(); // Card value stored in damage
                
                // Add card to deck (multiple times if stack size > 1)
                for (int j = 0; j < stack.getCount() && getStackAmount() < MAX_STACK_SIZE; j++) {
                    addToTop((byte) cardValue);
                    collected++;
                }
                
                // Remove from inventory
                player.getInventory().setStack(i, ItemStack.EMPTY);
                
                if (getStackAmount() >= MAX_STACK_SIZE) {
                    break; // Deck is full
                }
            }
        }
        
        return collected;
    }
    
    /**
     * Collect cards from a player's ender chest
     */
    private int collectCardsFromEnderChest(ServerPlayerEntity player) {
        int collected = 0;
        
        net.minecraft.inventory.EnderChestInventory enderChest = player.getEnderChestInventory();
        
        for (int i = 0; i < enderChest.size(); i++) {
            ItemStack stack = enderChest.getStack(i);
            
            if (belongsToThisDeck(stack)) {
                // This card belongs to our deck
                int cardValue = stack.getDamage(); // Card value stored in damage
                
                // Add card to deck (multiple times if stack size > 1)
                for (int j = 0; j < stack.getCount() && getStackAmount() < MAX_STACK_SIZE; j++) {
                    addToTop((byte) cardValue);
                    collected++;
                }
                
                // Remove from ender chest
                enderChest.setStack(i, ItemStack.EMPTY);
                
                if (getStackAmount() >= MAX_STACK_SIZE) {
                    break; // Deck is full
                }
            }
        }
        
        return collected;
    }
    
    /**
     * Collect cards from all storage containers in the world
     * Note: This searches in a reasonable radius around spawn to avoid performance issues
     */
    private int collectCardsFromStorageContainers(ServerWorld serverWorld) {
        int collected = 0;
        
        // Search for storage containers in a reasonable area around spawn (1000x1000 blocks)
        net.minecraft.util.math.BlockPos spawnPos = serverWorld.getSpawnPos();
        int searchRadius = 500;
        
        for (int x = spawnPos.getX() - searchRadius; x <= spawnPos.getX() + searchRadius; x += 16) {
            for (int z = spawnPos.getZ() - searchRadius; z <= spawnPos.getZ() + searchRadius; z += 16) {
                int chunkX = x >> 4;
                int chunkZ = z >> 4;
                
                if (serverWorld.isChunkLoaded(chunkX, chunkZ)) {
                    net.minecraft.world.chunk.WorldChunk chunk = serverWorld.getChunk(chunkX, chunkZ);
                    
                    // Check all block entities in this chunk
                    for (net.minecraft.block.entity.BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                        if (blockEntity instanceof net.minecraft.inventory.Inventory inventory) {
                            collected += collectCardsFromInventory(inventory);
                            
                            if (getStackAmount() >= MAX_STACK_SIZE) {
                                return collected; // Deck is full
                            }
                        }
                    }
                }
            }
        }
        
        return collected;
    }
    
    /**
     * Helper method to collect cards from any inventory
     */
    private int collectCardsFromInventory(net.minecraft.inventory.Inventory inventory) {
        int collected = 0;
        
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            
            if (belongsToThisDeck(stack)) {
                // This card belongs to our deck
                int cardValue = stack.getDamage(); // Card value stored in damage
                
                // Add card to deck (multiple times if stack size > 1)
                for (int j = 0; j < stack.getCount() && getStackAmount() < MAX_STACK_SIZE; j++) {
                    addToTop((byte) cardValue);
                    collected++;
                }
                
                // Remove from inventory
                inventory.setStack(i, ItemStack.EMPTY);
                
                if (getStackAmount() >= MAX_STACK_SIZE) {
                    break; // Deck is full
                }
            }
        }
        
        return collected;
    }
    
    /**
     * Collect all placed card entities in the world that belong to this deck
     */
    private int collectCardsFromWorld(ServerWorld serverWorld) {
        int collected = 0;
        
        // Find all EntityCard entities in the world that belong to this deck
        // Use a large search box to cover the entire world
        Box searchBox = new Box(-30000000, -64, -30000000, 30000000, 320, 30000000);
        java.util.List<EntityCard> allCards = serverWorld.getEntitiesByClass(
            EntityCard.class,
            searchBox,
            entity -> {
                UUID cardDeckUUID = entity.getDeckUUID();
                return cardDeckUUID != null && cardDeckUUID.equals(this.getUuid());
            }
        );
        
        for (EntityCard cardEntity : allCards) {
            if (getStackAmount() >= MAX_STACK_SIZE) {
                break; // Deck is full
            }
            
            // Add the card value to our deck
            byte cardValue = cardEntity.getTopStackID();
            addToTop(cardValue);
            collected++;
            
            // Remove the card entity from the world
            cardEntity.discard();
        }
        
        return collected;
    }
    
    /**
     * Collect cards from dropped item entities in the world
     */
    private int collectCardsFromDroppedItems(ServerWorld serverWorld) {
        int collected = 0;
        
        // Find all dropped ItemEntity objects in the world that contain cards belonging to this deck
        Box searchBox = new Box(-30000000, -64, -30000000, 30000000, 320, 30000000);
        java.util.List<net.minecraft.entity.ItemEntity> droppedItems = serverWorld.getEntitiesByClass(
            net.minecraft.entity.ItemEntity.class,
            searchBox,
            itemEntity -> {
                ItemStack stack = itemEntity.getStack();
                return belongsToThisDeck(stack);
            }
        );
        
        for (net.minecraft.entity.ItemEntity itemEntity : droppedItems) {
            if (getStackAmount() >= MAX_STACK_SIZE) {
                break; // Deck is full
            }
            
            ItemStack stack = itemEntity.getStack();
            
            // Add all cards from this dropped item stack to our deck
            for (int i = 0; i < stack.getCount() && getStackAmount() < MAX_STACK_SIZE; i++) {
                int cardValue = stack.getDamage(); // Card value stored in damage
                addToTop((byte) cardValue);
                collected++;
            }
            
            // Remove the dropped item entity from the world
            itemEntity.discard();
        }
        
        return collected;
    }
}
