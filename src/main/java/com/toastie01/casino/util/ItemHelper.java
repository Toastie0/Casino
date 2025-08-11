package com.toastie01.casino.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Utility methods for ItemStack operations
 */
public class ItemHelper {

    /**
     * Get or create NBT data for an ItemStack
     */
    public static NbtCompound getNBT(ItemStack stack) {
        return stack.getOrCreateNbt();
    }

    /**
     * Spawn an ItemStack at an entity's location
     */
    public static void spawnStackAtEntity(World world, Entity entity, ItemStack stack) {
        if (stack.isEmpty()) return;
        
        ItemEntity item = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), stack);
        item.setToDefaultPickupDelay();
        item.setVelocity(0, 0, 0);
        world.spawnEntity(item);
    }
    
    /**
     * Safely read a UUID from NBT data, handling both UUID and String formats
     */
    public static UUID safeGetUuid(NbtCompound nbt, String key) {
        if (!nbt.contains(key)) return null;
        
        try {
            return nbt.getUuid(key);
        } catch (IllegalArgumentException e) {
            try {
                String uuidString = nbt.getString(key);
                return uuidString.isEmpty() ? null : UUID.fromString(uuidString);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
