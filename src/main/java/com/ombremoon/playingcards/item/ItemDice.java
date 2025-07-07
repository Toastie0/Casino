package com.ombremoon.playingcards.item;

import com.ombremoon.playingcards.entity.EntityDice;
import com.ombremoon.playingcards.item.base.ItemBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemDice extends ItemBase {

    public ItemDice() {
        super(new Item.Settings().maxCount(5));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient) {
            EntityDice dice = new EntityDice(world, player.getPos(), player.getYaw());
            world.spawnEntity(dice);
            stack.decrement(1);
        }

        return TypedActionResult.success(stack, world.isClient);
    }
}
