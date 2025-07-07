package com.ombremoon.playingcards.economy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side economy manager that stores balance information received from the server
 * Does not directly interact with Impactor - all economy operations go through the server
 */
public class ClientEconomyManager {
    private static final Map<UUID, Double> cachedBalances = new HashMap<>();
    private static final double DEFAULT_BALANCE = 0.0; // Default when no server data
    
    public static void updateBalance(UUID playerId, double balance) {
        cachedBalances.put(playerId, balance);
    }
    
    public static double getBalance(UUID playerId) {
        return cachedBalances.getOrDefault(playerId, DEFAULT_BALANCE);
    }
    
    public static boolean hasBalance(UUID playerId, double amount) {
        return getBalance(playerId) >= amount;
    }
    
    public static String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }
    
    public static void clearCache() {
        cachedBalances.clear();
    }
}
