package com.ombremoon.playingcards.entity.base;

import com.ombremoon.playingcards.entity.data.PCDataSerializers;
import com.ombremoon.playingcards.util.ArrayHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class EntityStacked extends Entity {

    public static final byte MAX_STACK_SIZE = 52;

    protected static final TrackedData<Byte[]> STACK = DataTracker.registerData(EntityStacked.class, PCDataSerializers.STACK);

    public EntityStacked(EntityType<? extends EntityStacked> type, World world) {
        super(type, world);
        // Make the bounding box smaller for chips to reduce collision issues
        this.setBoundingBox(new Box(-0.125, 0.0, -0.125, 0.125, 0.05, 0.125));
    }

    public EntityStacked(EntityType<? extends EntityStacked> type, World world, Vec3d position) {
        this(type, world);
        setPosition(position.x, position.y, position.z);
        setRotation(0, 0);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(STACK, new Byte[0]);
        moreData();
    }

    public int getStackAmount() {
        return this.dataTracker.get(STACK).length;
    }

    public byte getTopStackID() {
        return getIDAt(getStackAmount() - 1);
    }

    public byte getIDAt(int index) {
        if (index >= 0 && index < getStackAmount()) {
            return this.dataTracker.get(STACK)[index];
        }
        return 0;
    }

    public void removeFromTop() {
        Byte[] currentStack = this.dataTracker.get(STACK);
        if (currentStack.length > 0) {
            Byte[] newStack = new Byte[currentStack.length - 1];
            System.arraycopy(currentStack, 0, newStack, 0, newStack.length);
            this.dataTracker.set(STACK, newStack);
        }
    }

    public void addToTop(byte id) {
        Byte[] currentStack = this.dataTracker.get(STACK);
        Byte[] newStack = new Byte[currentStack.length + 1];
        System.arraycopy(currentStack, 0, newStack, 0, currentStack.length);
        newStack[newStack.length - 1] = id;
        this.dataTracker.set(STACK, newStack);
    }

    public void createStack() {
        this.dataTracker.set(STACK, new Byte[0]);
    }

    public void createAndFillDeck() {
        Byte[] fullDeck = new Byte[52];
        for (byte i = 0; i < 52; i++) {
            fullDeck[i] = i;
        }
        this.dataTracker.set(STACK, fullDeck);
    }

    public void shuffleStack() {
        Byte[] currentStack = this.dataTracker.get(STACK);
        ArrayHelper.shuffle(currentStack);
        this.dataTracker.set(STACK, currentStack);
    }

    @Override
    public void tick() {
        super.tick();
        
        if (this.getWorld().isClient) {
            this.noClip = false;
        } else {
            // Simplified physics - less bouncy behavior
            if (!this.isOnGround()) {
                this.setVelocity(this.getVelocity().add(0.0D, -0.02D, 0.0D)); // Reduced gravity
            } else {
                // Dampen movement when on ground to reduce bouncing
                Vec3d velocity = this.getVelocity();
                this.setVelocity(velocity.multiply(0.8D, 0.8D, 0.8D)); // Add friction
            }
        }
        
        this.move(MovementType.SELF, this.getVelocity());
        
        // Set dynamic bounding box based on stack amount (like original)
        Vec3d pos = this.getPos();
        double size = 0.125D; // Smaller hitbox for chips
        double addAmount = 0.005D; // Smaller height increment per chip
        
        this.setBoundingBox(new Box(pos.x - size, pos.y, pos.z - size, 
                                   pos.x + size, pos.y + 0.05D + (addAmount * getStackAmount()), pos.z + size));
    }
    
    public abstract void moreData();

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        Byte[] stack = this.dataTracker.get(STACK);
        byte[] primitiveStack = ArrayHelper.toPrimitive(stack);
        nbt.putByteArray("Stack", primitiveStack);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Stack")) {
            byte[] primitiveStack = nbt.getByteArray("Stack");
            Byte[] stack = ArrayHelper.toObject(primitiveStack);
            this.dataTracker.set(STACK, stack);
        }
    }

    @Override
    public boolean isCollidable() {
        return false; // Disable collision to prevent bouncing between chips
    }

    @Override
    public boolean canHit() {
        return true; // Allow interaction
    }

    @Override
    public boolean isPushable() {
        return false; // Prevent entities from pushing each other
    }
}
