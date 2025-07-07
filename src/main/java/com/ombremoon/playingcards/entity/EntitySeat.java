package com.ombremoon.playingcards.entity;

import com.ombremoon.playingcards.init.ModEntityTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class EntitySeat extends Entity {

    private BlockPos sourceBlock;

    public EntitySeat(EntityType<? extends EntitySeat> type, World world) {
        super(type, world);
    }

    public EntitySeat(World world, BlockPos sourceBlock) {
        this(ModEntityTypes.ENTITY_SEAT, world);
        this.sourceBlock = sourceBlock;
        setPosition(sourceBlock.getX() + 0.5F, sourceBlock.getY() + 0.3F, sourceBlock.getZ() + 0.5F);
    }

    private BlockPos getSourceBlock() {
        return sourceBlock;
    }

    public static void createSeat(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClient) {
            List<EntitySeat> seats = world.getEntitiesByClass(EntitySeat.class, 
                new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), 
                seat -> true);

            if (seats.isEmpty()) {
                EntitySeat seat = new EntitySeat(world, pos);
                world.spawnEntity(seat);
                player.startRiding(seat);
            }
        }
    }

    @Override
    protected void initDataTracker() {
        // No synced data for seat
    }

    @Override
    public void tick() {
        super.tick();

        if (sourceBlock == null) {
            sourceBlock = this.getBlockPos();
        }

        if (!getWorld().isClient) {
            if (getPassengerList().isEmpty() || this.getWorld().getBlockState(sourceBlock).isAir()) {
                discard();
            }
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // No additional data to read for seat
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // No additional data to save for seat
    }

    @Override
    public double getMountedHeightOffset() {
        return 0.0D;
    }

    @Override
    public EntitySpawnS2CPacket createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
