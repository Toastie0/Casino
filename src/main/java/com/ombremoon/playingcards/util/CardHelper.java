package com.ombremoon.playingcards.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class CardHelper {

    public static final String[] CARD_SKIN_NAMES = {"card.skin.blue", "card.skin.red", "card.skin.black", "card.skin.pig"};

    /**
     * Get the display name for a card based on its ID (0-51)
     * Cards are organized as: Ace-King for each suit (Spades, Clubs, Diamonds, Hearts)
     */
    public static MutableText getCardName(int id) {
        int value = getValue(id);
        int suit = getSuit(id);
        
        String type = switch (value) {
            case 1 -> "card.ace";
            case 11 -> "card.jack";
            case 12 -> "card.queen";
            case 13 -> "card.king";
            default -> String.valueOf(value);
        };

        String suitName = switch (suit) {
            case 0 -> "card.spades";
            case 1 -> "card.clubs";
            case 2 -> "card.diamonds";
            case 3 -> "card.hearts";
            default -> "card.spades";
        };

        return Text.translatable(type)
            .append(" ")
            .append(Text.translatable("card.of"))
            .append(" ")
            .append(Text.translatable(suitName));
    }

    /**
     * Get the suit of a card (0-3)
     * @param id Card ID (0-51)
     * @return Suit ID (0=Spades, 1=Clubs, 2=Diamonds, 3=Hearts)
     */
    public static int getSuit(int id) {
        return id % 4;
    }

    /**
     * Get the value of a card (1-13)
     * @param id Card ID (0-51)
     * @return Card value (1=Ace, 11=Jack, 12=Queen, 13=King)
     */
    public static int getValue(int id) {
        return id / 4 + 1;
    }

    /**
     * Get card ID from suit and value
     * @param suit Suit ID (0-3)
     * @param value Card value (1-13)
     * @return Card ID (0-51)
     */
    public static int getCardId(int suit, int value) {
        return (value - 1) * 4 + suit;
    }

    /**
     * Render an item stack at the specified position
     * @param stack Item stack to render
     * @param world World context
     * @param offsetX X offset
     * @param offsetY Y offset
     * @param offsetZ Z offset
     * @param matrices Matrix stack for transformations
     * @param vertexConsumers Vertex consumer provider
     * @param light Light level
     */
    public static void renderItem(ItemStack stack, World world, double offsetX, double offsetY, double offsetZ, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(offsetX, offsetY, offsetZ);
        
        var renderer = MinecraftClient.getInstance().getItemRenderer();
        
        // Use GROUND transformation like the original
        renderer.renderItem(stack, ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        
        matrices.pop();
    }
}
