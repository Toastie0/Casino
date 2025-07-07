package com.ombremoon.playingcards;

import com.ombremoon.playingcards.command.ChipsCommand;
import com.ombremoon.playingcards.economy.EconomyManager;
import com.ombremoon.playingcards.entity.data.PCDataSerializers;
import com.ombremoon.playingcards.init.ModBlocks;
import com.ombremoon.playingcards.init.ModEntityTypes;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class PlayingCardsMod implements ModInitializer {

    @Override
    public void onInitialize() {
        PCReference.LOGGER.info("Loading Playing Cards mod...");
        
        // Register custom data serializers
        PCDataSerializers.register();
        
        // Initialize economy system
        EconomyManager.initialize();
        
        // Initialize entities
        ModEntityTypes.initialize();
        
        // Initialize blocks
        ModBlocks.initialize();
        
        // Initialize items
        ModItems.initialize();
        
        // Initialize blocks
        ModBlocks.initialize();
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ChipsCommand.register(dispatcher);
        });
        
        // Initialize Impactor integration when player joins a world
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // This runs when a player joins - Impactor will be checked dynamically in EconomyManager
            PCReference.LOGGER.info("Player joined - Impactor integration will be checked on first economy operation");
        });
        
        // Initialize networking
        ModNetworking.registerServerPackets();
        
        PCReference.LOGGER.info("Playing Cards mod loaded successfully!");
    }
}
