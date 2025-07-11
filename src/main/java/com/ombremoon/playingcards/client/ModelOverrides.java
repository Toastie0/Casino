package com.ombremoon.playingcards.client;

import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.util.ItemHelper;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class ModelOverrides {
    
    public static void initialize() {
        // Card model override - shows different card faces based on damage value (0-51)
        ModelPredicateProviderRegistry.register(ModItems.CARD, new Identifier("value"), 
            (stack, world, entity, seed) -> stack.getDamage());
        
        // Covered card model override - shows different card backs based on skin ID (0-3)  
        ModelPredicateProviderRegistry.register(ModItems.CARD_COVERED, new Identifier("skin"), 
            (stack, world, entity, seed) -> ItemHelper.getNBT(stack).getByte("SkinID"));
            
        // Card deck model override - shows different deck designs based on skin ID (0-3)
        ModelPredicateProviderRegistry.register(ModItems.CARD_DECK, new Identifier("skin"), 
            (stack, world, entity, seed) -> ItemHelper.getNBT(stack).getByte("SkinID"));
    }
}
