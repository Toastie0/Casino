package com.ombremoon.playingcards.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

/**
 * Entity for throwable dice with physics.
 * Supports multi-sided dice (4, 6, 8, 10, 12, 20 sides).
 * Based on the Dice-Rebuild mod but adapted for our casino dice system.
 */
public class EntityDice extends Entity {

    // Data trackers for synchronization
    private static final TrackedData<Float> ROLL_FLOOR = DataTracker.registerData(EntityDice.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> PREVIOUS_Y_VEL = DataTracker.registerData(EntityDice.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> CURRENT_FACE = DataTracker.registerData(EntityDice.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> MAX_SIDES = DataTracker.registerData(EntityDice.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> MATERIAL = DataTracker.registerData(EntityDice.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> OWNER_UUID = DataTracker.registerData(EntityDice.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> OWNER_NAME = DataTracker.registerData(EntityDice.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> IS_SIMPLE = DataTracker.registerData(EntityDice.class, TrackedDataHandlerRegistry.BOOLEAN);

    // NBT tags
    public static final String ROLL_FLOOR_TAG = "roll_floor";
    public static final String PREVIOUS_Y_VEL_TAG = "previous_y_velocity";
    public static final String CURRENT_FACE_TAG = "current_face";
    public static final String MAX_SIDES_TAG = "max_sides";
    public static final String MATERIAL_TAG = "material";
    public static final String OWNER_UUID_TAG = "owner_uuid";
    public static final String OWNER_NAME_TAG = "owner_name";
    public static final String IS_SIMPLE_TAG = "is_simple";
    public static final String DICE_AGE_TAG = "dice_age";

    // Ground detection for final roll
    private long onGroundStartTime;
    private static final long REQUIRED_TIME = 200; // 200ms to settle
    private int diceAge;

    public EntityDice(EntityType<EntityDice> type, World world) {
        super(type, world);
    }

    public EntityDice(World world, double x, double y, double z) {
        this(com.ombremoon.playingcards.init.ModEntityTypes.DICE, world);
        this.setPosition(x, y, z);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ROLL_FLOOR, 0.0F);
        this.dataTracker.startTracking(PREVIOUS_Y_VEL, 0.0F);
        this.dataTracker.startTracking(CURRENT_FACE, 1);
        this.dataTracker.startTracking(MAX_SIDES, 6);
        this.dataTracker.startTracking(MATERIAL, "");
        this.dataTracker.startTracking(OWNER_UUID, "");
        this.dataTracker.startTracking(OWNER_NAME, "");
        this.dataTracker.startTracking(IS_SIMPLE, false);
    }

    public void initGroundTimer() {
        this.onGroundStartTime = 0;
    }

    public boolean isOnGroundForSettledTime() {
        if (this.isOnGround()) {
            if (onGroundStartTime == 0) {
                onGroundStartTime = System.currentTimeMillis();
            }
            return System.currentTimeMillis() - onGroundStartTime >= REQUIRED_TIME;
        } else {
            onGroundStartTime = 0;
        }
        return false;
    }

    public void setDiceProperties(int maxSides, String material, boolean isSimple, PlayerEntity owner) {
        this.dataTracker.set(MAX_SIDES, maxSides);
        this.dataTracker.set(MATERIAL, material);
        this.dataTracker.set(IS_SIMPLE, isSimple);
        if (owner != null) {
            this.dataTracker.set(OWNER_UUID, owner.getUuidAsString());
            this.dataTracker.set(OWNER_NAME, owner.getName().getString());
        }
        // Set initial random face
        this.setCurrentFace(new Random().nextInt(maxSides) + 1);
    }

    public int getCurrentFace() {
        return this.dataTracker.get(CURRENT_FACE);
    }

    public void setCurrentFace(int face) {
        int maxSides = this.dataTracker.get(MAX_SIDES);
        if (face >= 1 && face <= maxSides) {
            this.dataTracker.set(CURRENT_FACE, face);
        }
    }

    public int getMaxSides() {
        return this.dataTracker.get(MAX_SIDES);
    }

    public String getMaterial() {
        return this.dataTracker.get(MATERIAL);
    }

    public boolean isSimpleDice() {
        return this.dataTracker.get(IS_SIMPLE);
    }

    public String getOwnerUUID() {
        return this.dataTracker.get(OWNER_UUID);
    }

    public String getOwnerName() {
        return this.dataTracker.get(OWNER_NAME);
    }

    public boolean hasOwner() {
        return !this.getOwnerUUID().isEmpty();
    }

    public boolean isOwner(PlayerEntity player) {
        return this.hasOwner() && this.getOwnerUUID().equals(player.getUuidAsString());
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        // Only allow owner to manually adjust dice face (shift + right click)
        if (player.isSneaking() && this.isOwner(player)) {
            int currentFace = this.getCurrentFace();
            int maxSides = this.getMaxSides();
            int nextFace = currentFace >= maxSides ? 1 : currentFace + 1;
            this.setCurrentFace(nextFace);
            this.playSound(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.0F, 1.0F);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void tick() {
        if (this.diceAge != -32768) this.diceAge++;

        // Randomly set face while rolling (not settled)
        if (!this.isOnGround() && !this.isTouchingWater()) {
            Random random = new Random();
            this.setCurrentFace(random.nextInt(this.getMaxSides()) + 1);
        }

        // Physics simulation (adapted from Dice-Rebuild)
        if (this.isInLava()) {
            this.discard();
            return;
        }

        // Apply gravity
        this.addVelocity(new Vec3d(0, -0.09, 0));
        this.move(MovementType.SELF, this.getVelocity());

        // Water physics
        if (this.isTouchingWater()) {
            this.addVelocity(new Vec3d(0, 0.23, 0));
            this.setVelocity(this.getVelocity().multiply(0.75, 0.75, 0.75));
        }

        // Ground physics
        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.1, 1, 0.1));
            if (MathHelper.abs((float) this.getVelocity().getY()) < 0.01) {
                this.addVelocity(0, -this.dataTracker.get(PREVIOUS_Y_VEL), 0);
            }
        } else {
            // Air resistance
            this.dataTracker.set(ROLL_FLOOR, this.dataTracker.get(ROLL_FLOOR) + (float) this.getVelocity().lengthSquared());
            this.setVelocity(this.getVelocity().multiply(0.98, 1, 0.98));
        }

        // Store previous Y velocity for bounce calculation
        this.dataTracker.set(PREVIOUS_Y_VEL, (float) this.getVelocity().getY());

        super.tick();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity attacker && !attacker.isSpectator() && !this.isRemoved()) {
            // Push the dice when hit
            Vec3d bounceVec = attacker.getRotationVector().add(this.getVelocity());
            if (attacker.getY() > this.getY()) {
                bounceVec = bounceVec.multiply(1, -1, 1);
            }
            this.setVelocity(bounceVec.multiply(0.5));
            this.playSound(SoundEvents.BLOCK_STONE_HIT, 1.0F, 1.0F);
            return false; // Don't actually damage the dice
        }
        return super.damage(source, amount);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat(ROLL_FLOOR_TAG, this.dataTracker.get(ROLL_FLOOR));
        nbt.putFloat(PREVIOUS_Y_VEL_TAG, this.dataTracker.get(PREVIOUS_Y_VEL));
        nbt.putInt(CURRENT_FACE_TAG, this.dataTracker.get(CURRENT_FACE));
        nbt.putInt(MAX_SIDES_TAG, this.dataTracker.get(MAX_SIDES));
        nbt.putString(MATERIAL_TAG, this.dataTracker.get(MATERIAL));
        nbt.putString(OWNER_UUID_TAG, this.dataTracker.get(OWNER_UUID));
        nbt.putString(OWNER_NAME_TAG, this.dataTracker.get(OWNER_NAME));
        nbt.putBoolean(IS_SIMPLE_TAG, this.dataTracker.get(IS_SIMPLE));
        nbt.putInt(DICE_AGE_TAG, this.diceAge);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(ROLL_FLOOR, nbt.getFloat(ROLL_FLOOR_TAG));
        this.dataTracker.set(PREVIOUS_Y_VEL, nbt.getFloat(PREVIOUS_Y_VEL_TAG));
        this.dataTracker.set(CURRENT_FACE, nbt.getInt(CURRENT_FACE_TAG));
        this.dataTracker.set(MAX_SIDES, nbt.getInt(MAX_SIDES_TAG));
        this.dataTracker.set(MATERIAL, nbt.getString(MATERIAL_TAG));
        this.dataTracker.set(OWNER_UUID, nbt.getString(OWNER_UUID_TAG));
        this.dataTracker.set(OWNER_NAME, nbt.getString(OWNER_NAME_TAG));
        this.dataTracker.set(IS_SIMPLE, nbt.getBoolean(IS_SIMPLE_TAG));
        this.diceAge = nbt.getInt(DICE_AGE_TAG);
    }

    /**
     * Create an ItemStack representation of this dice for when picked up
     */
    public ItemStack getPickupItemStack() {
        if (this.isSimpleDice()) {
            return new ItemStack(com.ombremoon.playingcards.init.ModItems.SIMPLE_DICE);
        } else {
            ItemStack stack = new ItemStack(com.ombremoon.playingcards.init.ModItems.FANTASY_DICE);
            com.ombremoon.playingcards.item.ItemFantasyDice.setSides(stack, this.getMaxSides());
            com.ombremoon.playingcards.item.ItemFantasyDice.setMaterial(stack, this.getMaterial());
            return stack;
        }
    }
}
