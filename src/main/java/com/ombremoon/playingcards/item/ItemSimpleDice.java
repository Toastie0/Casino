package com.ombremoon.playingcards.item;

import com.ombremoon.playingcards.entity.EntityDice;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Simple dice item that rolls 1-6 and broadcasts the result in chat.
 * This is a traditional casino dice without 3D rendering.
 */
public class ItemSimpleDice extends Item {

    public ItemSimpleDice() {
        super(new Item.Settings().maxCount(8));
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.literal("Traditional Dice").formatted(Formatting.GOLD);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            ItemStack stack = player.getStackInHand(hand);
            
            // Spawn dice entity in front of player
            Vec3d spawnPos = player.getPos()
                .add(0, player.getEyeHeight(player.getPose()), 0)
                .add(player.getRotationVector().multiply(1.5));
            
            EntityDice diceEntity = new EntityDice(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
            
            // Set dice properties (6-sided, no material, simple dice, owner)
            diceEntity.setDiceProperties(6, "", true, player);
            
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

        return TypedActionResult.success(player.getStackInHand(hand), world.isClient);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        // Add tooltip for inventory display
        tooltip.add(Text.literal("Right-click to throw!").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal("Rolls 1-6").formatted(Formatting.GRAY));
    }
}
