package com.ombremoon.playingcards.entity;

import com.ombremoon.playingcards.entity.base.EntityStacked;
import com.ombremoon.playingcards.init.ModEntityTypes;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.item.ItemPokerChip;
import com.ombremoon.playingcards.util.ItemHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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

import java.util.Optional;
import java.util.UUID;

public class EntityPokerChip extends EntityStacked {

    private static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(EntityPokerChip.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<String> OWNER_NAME = DataTracker.registerData(EntityPokerChip.class, TrackedDataHandlerRegistry.STRING);

    public EntityPokerChip(EntityType<? extends EntityPokerChip> type, World world) {
        super(type, world);
    }

    public EntityPokerChip(World world, Vec3d position, UUID ownerID, String ownerName, byte firstChipID) {
        super(ModEntityTypes.POKER_CHIP, world, position);

        createStack();
        addToTop(firstChipID);
        this.dataTracker.set(OWNER_UUID, Optional.of(ownerID));
        this.dataTracker.set(OWNER_NAME, ownerName);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
        this.dataTracker.startTracking(OWNER_NAME, "");
    }

    public UUID getOwnerUUID() {
        return this.dataTracker.get(OWNER_UUID).orElse(null);
    }

    public String getOwnerName() {
        return this.dataTracker.get(OWNER_NAME);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        
        if (stack.getItem() instanceof ItemPokerChip) {
            // Player is holding a poker chip - try to add it to this stack
            NbtCompound nbt = ItemHelper.getNBT(stack);
            
            if (nbt.contains("OwnerID")) {  // Note: Original uses "OwnerID" not "OwnerUUID"
                UUID ownerUUID = nbt.getUuid("OwnerID");
                
                if (ownerUUID.equals(getOwnerUUID())) {
                    if (player.isSneaking()) {
                        // Crouch + right-click: Add all chips from hand
                        while (getStackAmount() < MAX_STACK_SIZE && stack.getCount() > 0) {
                            ItemPokerChip chip = (ItemPokerChip) stack.getItem();
                            addToTop(chip.getChipId());
                            stack.decrement(1);
                        }
                    } else {
                        // Right-click: Add one chip
                        if (getStackAmount() < MAX_STACK_SIZE) {
                            ItemPokerChip chip = (ItemPokerChip) stack.getItem();
                            addToTop(chip.getChipId());
                            stack.decrement(1);
                        } else {
                            if (!this.getWorld().isClient) {
                                player.sendMessage(Text.translatable("message.stack_full").formatted(Formatting.RED), true);
                            }
                        }
                    }
                } else {
                    if (!this.getWorld().isClient) {
                        player.sendMessage(Text.translatable("message.stack_owner_error").formatted(Formatting.RED), true);
                    }
                }
            }
        } else {
            // Player is not holding a chip - try to take chips
            if (!this.getWorld().isClient) {
                if (player.isSneaking()) {
                    // Shift-click: Take entire stack
                    takeEntireStack(player);
                } else {
                    // Right-click: Take one chip
                    takeOneChip(player);
                }
            }
        }
        
        return ActionResult.SUCCESS;
    }

    private void takeOneChip(PlayerEntity player) {
        if (getStackAmount() <= 0) {
            return;
        }

        // Check ownership
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null && !ownerUUID.equals(player.getUuid())) {
            player.sendMessage(Text.translatable("message.poker_chip_wrong_owner").formatted(Formatting.RED), true);
            return;
        }

        // Create chip item
        ItemStack chipStack = getChipItemStack(getTopStackID(), 1);
        
        // Give to player
        if (!player.giveItemStack(chipStack)) {
            player.dropItem(chipStack, false);
        }

        // Play sound
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.PLAYERS, 0.3F, 1.2F);

        removeFromTop();

        if (getStackAmount() <= 0) {
            this.discard();
        }
    }

    private void takeEntireStack(PlayerEntity player) {
        if (getStackAmount() <= 0) {
            return;
        }

        // Check ownership
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null && !ownerUUID.equals(player.getUuid())) {
            player.sendMessage(Text.translatable("message.poker_chip_wrong_owner").formatted(Formatting.RED), true);
            return;
        }

        // Count chips by type
        int[] chipCounts = new int[9]; // Assuming 9 chip types (0-8)
        for (int i = 0; i < getStackAmount(); i++) {
            byte chipId = getIDAt(i);
            if (chipId >= 0 && chipId < chipCounts.length) {
                chipCounts[chipId]++;
            }
        }

        // Give chips to player
        for (int chipType = 0; chipType < chipCounts.length; chipType++) {
            if (chipCounts[chipType] > 0) {
                ItemStack chipStack = getChipItemStack((byte) chipType, chipCounts[chipType]);
                if (!player.giveItemStack(chipStack)) {
                    player.dropItem(chipStack, false);
                }
            }
        }

        // Play sound
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.5F, 1.0F);

        this.discard();
    }

    private ItemStack getChipItemStack(byte chipId, int count) {
        Item chipItem = getChipItemFromId(chipId);
        ItemStack stack = new ItemStack(chipItem, count);
        
        // Set owner data in NBT
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null) {
            stack.getOrCreateNbt().putUuid("OwnerID", ownerUUID);
            stack.getOrCreateNbt().putString("OwnerName", getOwnerName());
        }
        
        return stack;
    }

    private Item getChipItemFromId(byte chipId) {
        return switch (chipId) {
            case 0 -> ModItems.WHITE_POKER_CHIP;
            case 1 -> ModItems.RED_POKER_CHIP;
            case 2 -> ModItems.GREEN_POKER_CHIP;
            case 3 -> ModItems.BLUE_POKER_CHIP;
            case 4 -> ModItems.BLACK_POKER_CHIP;
            case 5 -> ModItems.PURPLE_POKER_CHIP;
            case 6 -> ModItems.YELLOW_POKER_CHIP;
            case 7 -> ModItems.PINK_POKER_CHIP;
            case 8 -> ModItems.ORANGE_POKER_CHIP;
            default -> ModItems.WHITE_POKER_CHIP;
        };
    }

    public double getChipValue(byte chipId) {
        Item chipItem = getChipItemFromId(chipId);
        if (chipItem instanceof ItemPokerChip pokerChip) {
            return pokerChip.getValue();
        }
        return 1.0; // Default value
    }

    public double getTotalValue() {
        double total = 0.0;
        for (int i = 0; i < getStackAmount(); i++) {
            total += getChipValue(getIDAt(i));
        }
        return total;
    }

    @Override
    public void tick() {
        super.tick();
        
        // Auto-collect nearby matching chips every 20 ticks (1 second)
        if (this.getWorld().getTime() % 20 == 0 && !this.getWorld().isClient) {
            collectNearbyChips();
        }
    }

    private void collectNearbyChips() {
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID == null) return;

        // Find nearby EntityPokerChip entities within a 1-block radius
        this.getWorld().getEntitiesByClass(EntityPokerChip.class, this.getBoundingBox().expand(1.0),
            chip -> chip != this && ownerUUID.equals(chip.getOwnerUUID()))
            .forEach(chip -> {
                // Add all chips from the other stack to this one
                for (int i = 0; i < chip.getStackAmount(); i++) {
                    addToTop(chip.getIDAt(i));
                }
                chip.discard();
                
                // Play a subtle sound
                this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.PLAYERS, 0.2F, 1.0F);
            });
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null) {
            nbt.putUuid("OwnerUUID", ownerUUID);
        }
        nbt.putString("OwnerName", getOwnerName());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("OwnerUUID")) {
            this.dataTracker.set(OWNER_UUID, Optional.of(nbt.getUuid("OwnerUUID")));
        }
        this.dataTracker.set(OWNER_NAME, nbt.getString("OwnerName"));
    }

    @Override
    public void moreData() {
        // Data tracker registration is handled in initDataTracker() to prevent duplicates
    }
}
