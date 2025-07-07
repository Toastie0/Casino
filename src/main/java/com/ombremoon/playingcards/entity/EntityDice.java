package com.ombremoon.playingcards.entity;

import com.ombremoon.playingcards.init.ModEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityDice extends Entity {

    private static final TrackedData<Integer> DICE_NUMBER = DataTracker.registerData(EntityDice.class, TrackedDataHandlerRegistry.INTEGER);

    public EntityDice(EntityType<? extends EntityDice> type, World world) {
        super(type, world);
        this.setBoundingBox(new Box(-0.25, 0.0, -0.25, 0.25, 0.25, 0.25));
    }

    public EntityDice(World world, Vec3d position, float rotation) {
        this(ModEntityTypes.ENTITY_DICE, world);
        setPosition(position.x, position.y, position.z);
        setYaw(rotation);
        setPitch(0);

        // Set initial random number
        this.dataTracker.set(DICE_NUMBER, world.getRandom().nextInt(6) + 1);

        float sin = MathHelper.sin(this.getYaw() * 0.017453292F - 11);
        float cos = MathHelper.cos(this.getYaw() * 0.017453292F - 11);

        this.setVelocity(0.5D * cos, 0.2D, 0.5D * sin);
    }

    public int getDiceNumber() {
        return this.dataTracker.get(DICE_NUMBER);
    }

    public void rollNewNumber() {
        this.dataTracker.set(DICE_NUMBER, this.getWorld().getRandom().nextInt(6) + 1);
    }

    @Override
    public void tick() {
        super.tick();

        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();

        Vec3d motion = this.getVelocity();

        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0D, -0.04D, 0.0D));
        }

        if (this.getWorld().isClient) {
            this.noClip = false;
        }

        if (!this.isOnGround() || (this.age + this.getId()) % 4 == 0) {
            this.move(MovementType.SELF, this.getVelocity());
            float f = 0.98F;

            if (this.isOnGround()) {
                BlockPos pos = new BlockPos(this.getBlockX(), (int) (this.getBlockY() - 1.0D), this.getBlockZ());
                BlockState blockState = this.getWorld().getBlockState(pos);
                f = blockState.getBlock().getSlipperiness() * 0.98F;
            }

            this.setVelocity(this.getVelocity().multiply(f, 0.98D, f));
            if (this.isOnGround()) {
                this.setVelocity(this.getVelocity().multiply(1.0D, -0.5D, 1.0D));
            }
        }

        if (!this.getWorld().isClient) {
            double d0 = this.getVelocity().subtract(motion).lengthSquared();
            if (d0 > 0.01D) {
                this.velocityDirty = true;
            }
            
            // Roll new number when dice comes to rest
            if (this.isOnGround() && this.getVelocity().lengthSquared() < 0.01D && d0 <= 0.01D) {
                // Only roll occasionally to avoid constant changes
                if (this.age % 40 == 0) { // Every 2 seconds
                    rollNewNumber();
                }
            }
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal(String.valueOf(getDiceNumber()));
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public boolean shouldRenderName() {
        return true;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // No additional data to save for dice
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // No additional data to read for dice
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(DICE_NUMBER, 6); // Default to 6
    }

    @Override
    public EntitySpawnS2CPacket createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    // Original dice entity has no interaction - only shows name label

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    protected Box calculateBoundingBox() {
        return new Box(-0.25, 0.0, -0.25, 0.25, 0.25, 0.25);
    }

    @Override
    public boolean isCollidable() {
        return true;
    }
}
