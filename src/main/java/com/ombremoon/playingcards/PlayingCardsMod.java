package com.ombremoon.playingcards;

import com.ombremoon.playingcards.command.CasinoCommand;
import com.ombremoon.playingcards.config.CasinoConfig;
import com.ombremoon.playingcards.economy.EconomyManager;
import com.ombremoon.playingcards.entity.data.PCDataSerializers;
import com.ombremoon.playingcards.init.ModBlockEntities;
import com.ombremoon.playingcards.init.ModBlocks;
import com.ombremoon.playingcards.init.ModEntityTypes;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class PlayingCardsMod implements ModInitializer {

    @Override
    public void onInitialize() {
        PCReference.LOGGER.info("Loading Casino mod...");
        
        // Load configuration first
        CasinoConfig.load();
        
        // Initialize core systems
        PCDataSerializers.register();
        EconomyManager.initialize();
        
        // Initialize content
        ModEntityTypes.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        ModItems.initialize();
        
        // Register commands and networking
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> 
            CasinoCommand.register(dispatcher));
        ModNetworking.registerServerPackets();
        
        PCReference.LOGGER.info("Casino mod loaded successfully!");
    }
}
