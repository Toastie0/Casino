package com.ombremoon.playingcards.item;

import com.ombremoon.playingcards.util.CardHelper;
import com.ombremoon.playingcards.util.ItemHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemCard extends ItemCardCovered {

    public ItemCard() {
        super();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // Show the card name (e.g., "Ace of Spades")
        int cardId = getCardId(stack);
        tooltip.add(CardHelper.getCardName(cardId).formatted(Formatting.GOLD));
        
        // Show skin information (from parent, but skip the covered status)
        NbtCompound nbt = ItemHelper.getNBT(stack);
        
        // Show card skin information
        byte skinId = nbt.getByte("SkinID");
        if (skinId >= 0 && skinId < CardHelper.CARD_SKIN_NAMES.length) {
            tooltip.add(Text.translatable("lore.cover").append(" ")
                    .formatted(Formatting.GRAY)
                    .append(Text.translatable(CardHelper.CARD_SKIN_NAMES[skinId])
                            .formatted(Formatting.AQUA)));
        }
        
        // Show flip instruction for face-up cards
        tooltip.add(Text.translatable("lore.card.flip").formatted(Formatting.DARK_GRAY));
    }
}
