package com.ombremoon.playingcards.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemHelper {

    /**
     * Get or create NBT data for an ItemStack
     * @param stack The ItemStack
     * @return NBT compound data
     */
    public static NbtCompound getNBT(ItemStack stack) {
        return stack.getOrCreateNbt();
    }

    /**
     * Spawn an ItemStack at an entity's location
     * @param world The world
     * @param entity The entity to spawn at
     * @param stack The ItemStack to spawn
     */
    public static void spawnStackAtEntity(World world, Entity entity, ItemStack stack) {
        spawnStack(world, entity.getX(), entity.getY(), entity.getZ(), stack);
    }

    /**
     * Spawn an ItemStack at a player's location
     * @param world The world
     * @param player The player to spawn at
     * @param stack The ItemStack to spawn
     */
    public static void spawnStackAtEntity(World world, PlayerEntity player, ItemStack stack) {
        spawnStack(world, player.getX(), player.getY(), player.getZ(), stack);
    }

    /**
     * Spawn an ItemStack at specific coordinates
     * @param world The world
     * @param x X coordinate
     * @param y Y coordinate  
     * @param z Z coordinate
     * @param stack The ItemStack to spawn
     */
    private static void spawnStack(World world, double x, double y, double z, ItemStack stack) {
        ItemEntity item = new ItemEntity(world, x, y, z, stack);
        item.setToDefaultPickupDelay();
        item.setVelocity(0, 0, 0);
        world.spawnEntity(item);
    }
}
