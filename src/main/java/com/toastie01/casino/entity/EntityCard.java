package com.toastie01.casino.entity;

import com.toastie01.casino.entity.base.EntityStacked;
import com.toastie01.casino.init.ModEntityTypes;
import com.toastie01.casino.init.ModItems;
import com.toastie01.casino.util.ItemHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

public class EntityCard extends EntityStacked {

    private static final TrackedData<Float> ROTATION = DataTracker.registerData(EntityCard.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Byte> SKIN_ID = DataTracker.registerData(EntityCard.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Optional<UUID>> DECK_UUID = DataTracker.registerData(EntityCard.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Boolean> COVERED = DataTracker.registerData(EntityCard.class, TrackedDataHandlerRegistry.BOOLEAN);

    public EntityCard(EntityType<? extends EntityCard> type, World world) {
        super(type, world);
    }

    public EntityCard(World world, Vec3d position, float rotation, byte skinID, UUID deckUUID, boolean covered, byte firstCardID) {
        super(ModEntityTypes.CARD, world, position);

        createStack();
        addToTop(firstCardID);
        
        // Initialize the data tracker values after entity construction
        initializeCardData(rotation, skinID, deckUUID, covered);
    }
    
    private void initializeCardData(float rotation, byte skinID, UUID deckUUID, boolean covered) {
        this.dataTracker.set(ROTATION, rotation);
        this.dataTracker.set(SKIN_ID, skinID);
        this.dataTracker.set(DECK_UUID, Optional.of(deckUUID));
        this.dataTracker.set(COVERED, covered);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ROTATION, 0.0F);
        this.dataTracker.startTracking(SKIN_ID, (byte) 0);
        this.dataTracker.startTracking(DECK_UUID, Optional.empty());
        this.dataTracker.startTracking(COVERED, false);
    }

    public float getRotation() {
        return this.dataTracker.get(ROTATION);
    }

    public byte getSkinID() {
        return this.dataTracker.get(SKIN_ID);
    }

    public UUID getDeckUUID() {
        return this.dataTracker.get(DECK_UUID).orElse(null);
    }

    public boolean isCovered() {
        return this.dataTracker.get(COVERED);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient) {
            // Right-click: Flip card on table
            boolean newCoveredState = !this.dataTracker.get(COVERED);
            this.dataTracker.set(COVERED, newCoveredState);
            
            // Play sound for card flip
            this.getWorld().playSound(null, this.getBlockPos(), 
                net.minecraft.sound.SoundEvents.ITEM_BOOK_PAGE_TURN, 
                net.minecraft.sound.SoundCategory.PLAYERS, 0.5F, 1.5F);
        }
        return ActionResult.SUCCESS;
    }
    
    // handleCardStacking method removed - no longer used in simplified interaction system

    private void takeCard(PlayerEntity player) {
        ItemStack card;
        if (this.dataTracker.get(COVERED)) {
            card = new ItemStack(ModItems.CARD_COVERED);
        } else {
            card = new ItemStack(ModItems.CARD);
        }

        // Set card ID as damage value (this is how the original mod works)
        card.setDamage(getTopStackID());
        
        // Set NBT data
        NbtCompound nbt = ItemHelper.getNBT(card);
        UUID deckUUID = getDeckUUID();
        if (deckUUID != null) {
            nbt.putString("UUID", deckUUID.toString());
        }
        nbt.putByte("SkinID", this.dataTracker.get(SKIN_ID));
        nbt.putBoolean("Covered", this.dataTracker.get(COVERED));
        
        // Set proper CustomModelData based on card state
        if (this.dataTracker.get(COVERED)) {
            // Covered card - use skin ID for card back texture
            nbt.putInt("CustomModelData", this.dataTracker.get(SKIN_ID));
        } else {
            // Face-up card - use card ID + 100 for specific card face texture
            nbt.putInt("CustomModelData", 100 + getTopStackID());
        }

        // Give item to player
        if (!player.giveItemStack(card)) {
            player.dropItem(card, false);
        }

        removeFromTop();

        if (getStackAmount() <= 0) {
            this.discard();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("Rotation", this.dataTracker.get(ROTATION));
        nbt.putByte("SkinID", this.dataTracker.get(SKIN_ID));
        UUID deckUUID = getDeckUUID();
        if (deckUUID != null) {
            nbt.putUuid("DeckUUID", deckUUID);
        }
        nbt.putBoolean("Covered", this.dataTracker.get(COVERED));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(ROTATION, nbt.getFloat("Rotation"));
        this.dataTracker.set(SKIN_ID, nbt.getByte("SkinID"));
        if (nbt.containsUuid("DeckUUID")) {
            this.dataTracker.set(DECK_UUID, Optional.of(nbt.getUuid("DeckUUID")));
        }
        this.dataTracker.set(COVERED, nbt.getBoolean("Covered"));
    }

    @Override
    public void moreData() {
        // Data tracker registration is handled in initDataTracker() to prevent duplicates
    }

    @Override
    public void tick() {
        super.tick();
        
        // Automatic cleanup like original - check every 20 ticks (1 second)
        if (this.getWorld().getTime() % 20 == 0 && !this.getWorld().isClient) {
            UUID deckUUID = getDeckUUID();
            if (deckUUID != null) {
                // Look for parent deck in nearby area (like original - 20 block radius)
                net.minecraft.util.math.BlockPos pos = this.getBlockPos();
                java.util.List<EntityCardDeck> nearbyDecks = this.getWorld().getEntitiesByClass(
                    EntityCardDeck.class, 
                    new net.minecraft.util.math.Box(pos.getX() - 20, pos.getY() - 20, pos.getZ() - 20, 
                                                   pos.getX() + 20, pos.getY() + 20, pos.getZ() + 20),
                    deck -> deck.getUuid().equals(deckUUID)
                );
                
                // If no parent deck found, remove this card (like original)
                if (nearbyDecks.isEmpty()) {
                    this.discard();
                }
            }
        }
    }
    
    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        // Left-click: Pick up card from table
        if (source.getAttacker() instanceof PlayerEntity player) {
            if (!this.getWorld().isClient) {
                takeCard(player);
            }
            return true;
        }
        return false;
    }
}
