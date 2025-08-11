package com.toastie01.casino.economy;

import com.toastie01.casino.PCReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * Economy manager that routes to appropriate implementation based on environment
 */
public class EconomyManager {
    
    public static void initialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            ServerEconomyManager.initialize();
        }
        PCReference.LOGGER.info("Economy Manager initialized");
    }
    
    // Server-side economy operations
    public static double getBalance(ServerPlayerEntity player) {
        return ServerEconomyManager.getBalance(player);
    }
    
    public static boolean hasBalance(ServerPlayerEntity player, double amount) {
        return ServerEconomyManager.hasBalance(player, amount);
    }
    
    public static boolean withdraw(ServerPlayerEntity player, double amount) {
        return ServerEconomyManager.withdraw(player, amount);
    }
    
    public static boolean deposit(ServerPlayerEntity player, double amount) {
        return ServerEconomyManager.deposit(player, amount);
    }
    
    public static void sendEconomyMessage(ServerPlayerEntity player, String message) {
        ServerEconomyManager.sendEconomyMessage(player, message);
    }
    
    public static void sendErrorMessage(ServerPlayerEntity player, String message) {
        ServerEconomyManager.sendErrorMessage(player, message);
    }
    
    // Client-side economy operations
    @Environment(EnvType.CLIENT)
    public static double getClientBalance(UUID playerId) {
        return ClientEconomyManager.getBalance(playerId);
    }
    
    @Environment(EnvType.CLIENT)
    public static boolean hasClientBalance(UUID playerId, double amount) {
        return ClientEconomyManager.hasBalance(playerId, amount);
    }
    
    @Environment(EnvType.CLIENT)
    public static void updateClientBalance(UUID playerId, double balance) {
        ClientEconomyManager.updateBalance(playerId, balance);
    }
    
    // Common utility methods
    public static String formatCurrency(double amount) {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER 
            ? ServerEconomyManager.formatCurrency(amount)
            : ClientEconomyManager.formatCurrency(amount);
    }
    
    public static boolean isImpactorAvailable() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER 
            && ServerEconomyManager.isImpactorAvailable();
    }
}
