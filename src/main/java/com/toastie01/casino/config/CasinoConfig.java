package com.toastie01.casino.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.toastie01.casino.PCReference;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration system for the Casino mod.
 * Handles loading, saving, and providing access to config values.
 */
public class CasinoConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "casino.json";
    private static CasinoConfig INSTANCE = null;
    
    // Economy Settings
    public double startingBalance = 10000.0;
    public String forceEconomyMode = "auto"; // "auto", "dummy", "impactor"
    
    // Poker Chip Values
    public Map<String, Double> chipValues = new HashMap<>();
    
    // Feature Toggles  
    public boolean enableSellAllCommand = true;
    public boolean enableBalanceCommands = true;
    
    // GUI Settings
    public String shopGuiTitle = "Chip Shop";
    public String mainGuiTitle = "Casino";
    
    public CasinoConfig() {
        // Initialize default chip values
        chipValues.put("white", 1.0);
        chipValues.put("red", 5.0);
        chipValues.put("green", 25.0);
        chipValues.put("blue", 50.0);
        chipValues.put("black", 100.0);
        chipValues.put("purple", 500.0);
        chipValues.put("yellow", 1000.0);
        chipValues.put("pink", 5000.0);
        chipValues.put("orange", 25000.0);
    }
    
    /**
     * Gets the singleton config instance, loading it if necessary.
     */
    public static CasinoConfig getInstance() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }
    
    /**
     * Loads the config from file, creating default if it doesn't exist.
     */
    public static void load() {
        Path configPath = getConfigPath();
        
        if (Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath);
                INSTANCE = GSON.fromJson(content, CasinoConfig.class);
                
                // Validate and fix any missing values
                if (INSTANCE.chipValues == null || INSTANCE.chipValues.isEmpty()) {
                    PCReference.LOGGER.warn("Missing chip values in config, using defaults");
                    INSTANCE.chipValues = new CasinoConfig().chipValues;
                }
                
                // Validate starting balance
                if (INSTANCE.startingBalance < 0) {
                    PCReference.LOGGER.warn("Invalid starting balance {} in config, using default", INSTANCE.startingBalance);
                    INSTANCE.startingBalance = 10000.0;
                }
                
                // Validate chip values are positive
                validateChipValues(INSTANCE);
                
                // Validate economy mode
                if (!isValidEconomyMode(INSTANCE.forceEconomyMode)) {
                    PCReference.LOGGER.warn("Invalid economy mode '{}' in config, using 'auto'", INSTANCE.forceEconomyMode);
                    INSTANCE.forceEconomyMode = "auto";
                }
                
                PCReference.LOGGER.info("Loaded Casino config from {}", configPath);
            } catch (Exception e) {
                PCReference.LOGGER.error("Failed to load Casino config, using defaults", e);
                INSTANCE = new CasinoConfig();
                save(); // Save default config
            }
        } else {
            INSTANCE = new CasinoConfig();
            save(); // Create default config file
            PCReference.LOGGER.info("Created default Casino config at {}", configPath);
        }
    }
    
    /**
     * Saves the current config to file.
     */
    public static void save() {
        if (INSTANCE == null) return;
        
        try {
            Path configPath = getConfigPath();
            Files.createDirectories(configPath.getParent());
            
            String json = GSON.toJson(INSTANCE);
            Files.writeString(configPath, json);
            
            PCReference.LOGGER.info("Saved Casino config to {}", configPath);
        } catch (IOException e) {
            PCReference.LOGGER.error("Failed to save Casino config", e);
        }
    }
    
    /**
     * Gets the path to the config file.
     */
    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    }
    
    /**
     * Validates if the economy mode string is valid.
     */
    private static boolean isValidEconomyMode(String mode) {
        return "auto".equals(mode) || "dummy".equals(mode) || "impactor".equals(mode);
    }
    
    /**
     * Validates and fixes chip values to ensure they are positive.
     */
    private static void validateChipValues(CasinoConfig config) {
        if (config.chipValues == null) return;
        
        boolean hasInvalidValues = false;
        for (var entry : config.chipValues.entrySet()) {
            if (entry.getValue() <= 0) {
                PCReference.LOGGER.warn("Invalid chip value {} for {}, must be positive", entry.getValue(), entry.getKey());
                hasInvalidValues = true;
            }
        }
        
        if (hasInvalidValues) {
            // Reset to defaults if any invalid values found
            config.chipValues = new CasinoConfig().chipValues;
            PCReference.LOGGER.info("Reset chip values to defaults due to invalid values");
        }
    }
    
    /**
     * Gets the value of a specific poker chip by color name.
     */
    public double getChipValue(String color) {
        return chipValues.getOrDefault(color.toLowerCase(), 1.0);
    }
    
    /**
     * Checks if the economy mode should force dummy economy.
     */
    public boolean shouldForceDummyEconomy() {
        return "dummy".equals(forceEconomyMode);
    }
    
    /**
     * Checks if the economy mode should require Impactor.
     */
    public boolean shouldRequireImpactor() {
        return "impactor".equals(forceEconomyMode);
    }
    
    /**
     * Checks if the economy mode should auto-detect.
     */
    public boolean shouldAutoDetectEconomy() {
        return "auto".equals(forceEconomyMode);
    }
}
