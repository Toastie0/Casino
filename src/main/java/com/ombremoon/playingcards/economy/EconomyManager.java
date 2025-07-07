package com.ombremoon.playingcards.economy;

import com.ombremoon.playingcards.PCReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

/**
 * Main economy manager that routes to server-side or client-side implementation
 */
public class EconomyManager {
    
    public static void initialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            ServerEconomyManager.initialize();
        }
        PCReference.LOGGER.info("Economy Manager initialized");
    }
    
    /**
     * Server-side methods - only called on the server
     */
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
    
    /**
     * Client-side methods - only called on the client
     */
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
    
    /**
     * Common methods - available on both sides
     */
    public static String formatCurrency(double amount) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return ServerEconomyManager.formatCurrency(amount);
        } else {
            return ClientEconomyManager.formatCurrency(amount);
        }
    }
    
    public static boolean isImpactorAvailable() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return ServerEconomyManager.isImpactorAvailable();
        }
        return false; // Client doesn't check Impactor directly
    }
}
