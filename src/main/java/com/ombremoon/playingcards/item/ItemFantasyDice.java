package com.ombremoon.playingcards.item;

import com.ombremoon.playingcards.entity.EntityDice;
import com.ombremoon.playingcards.util.ItemHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemFantasyDice extends Item {
    
    // Material order from cheapest to most expensive
    public static final String[] MATERIALS = {
        "chocolate", "paper", "bone", "wooden", "stone", "copper", 
        "slime", "redstone", "iron", "emerald", "golden", "amethyst", 
        "frozen", "diamond", "netherite", "ender"
    };
    
    // Available dice sides
    public static final int[] SIDES = {4, 6, 8, 10, 12, 20};

    public ItemFantasyDice() {
        super(new Item.Settings().maxCount(8));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        
        if (!world.isClient) {
            int sides = getSides(stack);
            String material = getMaterial(stack);
            
            // Only throw if the dice has valid properties
            if (sides > 0 && !material.isEmpty()) {
                // Spawn dice entity in front of player
                Vec3d spawnPos = player.getPos()
                    .add(0, player.getEyeHeight(player.getPose()), 0)
                    .add(player.getRotationVector().multiply(1.5));
                
                EntityDice diceEntity = new EntityDice(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                
                // Set dice properties (sides, material, not simple dice, owner)
                diceEntity.setDiceProperties(sides, material, false, player);
                
                // Set throwing velocity
                diceEntity.setVelocity(player.getRotationVector().multiply(1.0));
                
                // Spawn the entity
                world.spawnEntity(diceEntity);
                
                // Play throwing sound
                player.playSound(SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                
                // Add cooldown
                player.getItemCooldownManager().set(this, 20); // 1 second cooldown
                
                // Consume the dice item (unless in creative)
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
            }
        }
        
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int sides = getSides(stack);
        String material = getMaterial(stack);
        
        tooltip.add(Text.literal("Material: " + formatMaterialName(material))
                .formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Sides: " + sides)
                .formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Right-click to throw!")
                .formatted(Formatting.YELLOW));
    }
    
    @Override
    public Text getName(ItemStack stack) {
        int sides = getSides(stack);
        String material = getMaterial(stack);
        return Text.literal(formatMaterialName(material) + " " + sides + "-sided Dice")
                .formatted(Formatting.WHITE);
    }

    // Create a dice with specific properties
    public static ItemStack createDice(int sides, String material) {
        ItemStack stack = new ItemStack(com.ombremoon.playingcards.init.ModItems.FANTASY_DICE);
        setSides(stack, sides);
        setMaterial(stack, material);
        return stack;
    }

    // NBT data management
    public static void setSides(ItemStack stack, int sides) {
        NbtCompound nbt = ItemHelper.getNBT(stack);
        nbt.putInt("Sides", sides);
    }

    public static void setMaterial(ItemStack stack, String material) {
        NbtCompound nbt = ItemHelper.getNBT(stack);
        nbt.putString("Material", material);
    }

    public static int getSides(ItemStack stack) {
        NbtCompound nbt = ItemHelper.getNBT(stack);
        return nbt.getInt("Sides"); // Default will be 0, handle in GUI
    }

    public static String getMaterial(ItemStack stack) {
        NbtCompound nbt = ItemHelper.getNBT(stack);
        return nbt.getString("Material"); // Default will be empty, handle in GUI
    }
    
    // Calculate dice price based on sides and material
    public static double calculatePrice(int sides, String material) {
        // Base price for sides
        double sidePrice = switch (sides) {
            case 4 -> 10.0;
            case 6 -> 20.0;
            case 8 -> 30.0;
            case 10 -> 40.0;
            case 12 -> 50.0;
            case 20 -> 100.0;
            default -> 10.0;
        };
        
        // Material tier multiplier ($10 per tier)
        int materialTier = getMaterialTier(material);
        double materialCost = materialTier * 10.0;
        
        return sidePrice + materialCost;
    }
    
    // Get material tier (0-indexed)
    public static int getMaterialTier(String material) {
        for (int i = 0; i < MATERIALS.length; i++) {
            if (MATERIALS[i].equals(material)) {
                return i;
            }
        }
        return 0; // Default to cheapest if not found
    }
    
    // Format material name for display
    private static String formatMaterialName(String material) {
        if (material == null || material.isEmpty()) return "Unknown";
        return material.substring(0, 1).toUpperCase() + material.substring(1);
    }
}
