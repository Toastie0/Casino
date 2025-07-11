package com.ombremoon.playingcards.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Block entity for casino tables that supports persistent ownership.
 * Allows players to claim tables and restricts chip placement to table owners.
 */
public class CasinoTableBlockEntity extends BlockEntity {
    
    private UUID ownerUUID;
    private String ownerName;
    
    public CasinoTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    /**
     * Sets the owner of this casino table.
     * @param playerUUID The UUID of the player claiming the table
     * @param playerName The display name of the player
     */
    public void setOwner(UUID playerUUID, String playerName) {
        this.ownerUUID = playerUUID;
        this.ownerName = playerName;
        markDirty();
        
        // Sync to client for tooltips/rendering
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }
    
    /**
     * Gets the UUID of the table owner.
     * @return Owner UUID, or null if unclaimed
     */
    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }
    
    /**
     * Gets the display name of the table owner.
     * @return Owner name, or null if unclaimed
     */
    @Nullable
    public String getOwnerName() {
        return this.ownerName;
    }
    
    /**
     * Checks if the table has an owner.
     * @return true if the table is claimed
     */
    public boolean hasOwner() {
        return this.ownerUUID != null;
    }
    
    /**
     * Checks if the given player is the owner of this table.
     * @param playerUUID UUID to check
     * @return true if the player owns this table
     */
    public boolean isOwner(UUID playerUUID) {
        return this.ownerUUID != null && this.ownerUUID.equals(playerUUID);
    }
    
    /**
     * Removes ownership from the table.
     */
    public void clearOwnership() {
        this.ownerUUID = null;
        this.ownerName = null;
        markDirty();
        
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }
    
    /**
     * Gets a formatted display text for the table owner.
     * @return Formatted owner text, or "Unclaimed" if no owner
     */
    public Text getOwnerDisplayText() {
        if (hasOwner()) {
            return Text.literal("Owner: " + ownerName);
        } else {
            return Text.literal("Unclaimed Table");
        }
    }
    
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        
        if (nbt.contains("OwnerUUID")) {
            this.ownerUUID = nbt.getUuid("OwnerUUID");
        }
        if (nbt.contains("OwnerName")) {
            this.ownerName = nbt.getString("OwnerName");
        }
    }
    
    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        
        if (this.ownerUUID != null) {
            nbt.putUuid("OwnerUUID", this.ownerUUID);
        }
        if (this.ownerName != null) {
            nbt.putString("OwnerName", this.ownerName);
        }
    }
    
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
