package com.toastie01.casino;

import com.toastie01.casino.command.CasinoCommand;
import com.toastie01.casino.config.CasinoConfig;
import com.toastie01.casino.economy.EconomyManager;
import com.toastie01.casino.entity.data.PCDataSerializers;
import com.toastie01.casino.init.ModBlockEntities;
import com.toastie01.casino.init.ModBlocks;
import com.toastie01.casino.init.ModEntityTypes;
import com.toastie01.casino.init.ModItems;
import com.toastie01.casino.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CasinoMod implements ModInitializer {

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
