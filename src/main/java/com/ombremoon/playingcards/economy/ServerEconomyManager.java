package com.ombremoon.playingcards.economy;

import com.ombremoon.playingcards.PCReference;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side economy manager that handles Impactor integration
 * Clients get balance info via networking packets from the server
 */
public class ServerEconomyManager {
    private static final Map<UUID, Double> playerBalances = new HashMap<>();
    private static final double STARTING_BALANCE = 10000.0; // $10,000 starting money for testing
    
    private static Boolean impactorAvailable = null; // null = not checked yet
    private static Object economyService = null;
    private static Object primaryCurrency = null;
    
    public static void initialize() {
        PCReference.LOGGER.info("Server Economy Manager initialized - will check for Impactor dynamically");
    }
    
    private static boolean checkImpactorAvailability() {
        if (impactorAvailable != null) {
            return impactorAvailable; // Already checked
        }
        
        try {
            // Check if Impactor classes are available
            Class<?> impactorClass = Class.forName("net.impactdev.impactor.api.Impactor");
            Class<?> economyServiceClass = Class.forName("net.impactdev.impactor.api.economy.EconomyService");
            
            // Get Impactor instance
            Method instanceMethod = impactorClass.getMethod("instance");
            Object impactorInstance = instanceMethod.invoke(null);
            
            // Get services from Impactor instance
            Method servicesMethod = impactorInstance.getClass().getMethod("services");
            Object services = servicesMethod.invoke(impactorInstance);
            
            // Get EconomyService from services
            Method provideMethod = services.getClass().getMethod("provide", Class.class);
            economyService = provideMethod.invoke(services, economyServiceClass);
            
            // Get primary currency
            Method currenciesMethod = economyService.getClass().getMethod("currencies");
            Object currencies = currenciesMethod.invoke(economyService);
            Method primaryMethod = currencies.getClass().getMethod("primary");
            primaryCurrency = primaryMethod.invoke(currencies);
            
            impactorAvailable = true;
            PCReference.LOGGER.info("Server Impactor Economy successfully connected!");
            return true;
            
        } catch (Exception e) {
            impactorAvailable = false;
            PCReference.LOGGER.info("Server Impactor Economy not available, using fallback: " + e.getMessage());
            return false;
        }
    }
    
    public static double getBalance(ServerPlayerEntity player) {
        if (checkImpactorAvailability()) {
            try {
                // Get account using reflection - interface method signature
                Class<?> currencyInterface = Class.forName("net.impactdev.impactor.api.economy.currency.Currency");
                Method accountMethod = economyService.getClass().getMethod("account", currencyInterface, UUID.class);
                Object accountFuture = accountMethod.invoke(economyService, primaryCurrency, player.getUuid());
                
                // Wait for the CompletableFuture to complete
                Method joinMethod = accountFuture.getClass().getMethod("join");
                Object account = joinMethod.invoke(accountFuture);
                
                // Get balance
                Method balanceMethod = account.getClass().getMethod("balance");
                BigDecimal balance = (BigDecimal) balanceMethod.invoke(account);
                
                return balance.doubleValue();
                
            } catch (Exception e) {
                PCReference.LOGGER.warn("Failed to get balance from Impactor: " + e.getMessage());
                // Fall back to dummy economy
            }
        }
        
        // Dummy economy fallback for singleplayer/testing
        return playerBalances.getOrDefault(player.getUuid(), STARTING_BALANCE);
    }
    
    public static boolean hasBalance(ServerPlayerEntity player, double amount) {
        return getBalance(player) >= amount;
    }
    
    public static boolean withdraw(ServerPlayerEntity player, double amount) {
        if (checkImpactorAvailability()) {
            try {
                // Get account using reflection - interface method signature
                Class<?> currencyInterface = Class.forName("net.impactdev.impactor.api.economy.currency.Currency");
                Method accountMethod = economyService.getClass().getMethod("account", currencyInterface, UUID.class);
                Object accountFuture = accountMethod.invoke(economyService, primaryCurrency, player.getUuid());
                
                // Wait for the CompletableFuture to complete
                Method joinMethod = accountFuture.getClass().getMethod("join");
                Object account = joinMethod.invoke(accountFuture);
                
                // Withdraw money
                Method withdrawMethod = account.getClass().getMethod("withdraw", BigDecimal.class);
                Object transaction = withdrawMethod.invoke(account, new BigDecimal(amount));
                
                // Check if transaction was successful
                Method successfulMethod = transaction.getClass().getMethod("successful");
                boolean success = (Boolean) successfulMethod.invoke(transaction);
                
                return success;
                
            } catch (Exception e) {
                PCReference.LOGGER.warn("Failed to withdraw from Impactor: " + e.getMessage());
                // Fall back to dummy economy
            }
        }
        
        // Dummy economy fallback for singleplayer/testing
        double currentBalance = getBalance(player);
        if (currentBalance >= amount) {
            playerBalances.put(player.getUuid(), currentBalance - amount);
            return true;
        }
        return false;
    }
    
    public static boolean deposit(ServerPlayerEntity player, double amount) {
        if (checkImpactorAvailability()) {
            try {
                // Get account using reflection - interface method signature
                Class<?> currencyInterface = Class.forName("net.impactdev.impactor.api.economy.currency.Currency");
                Method accountMethod = economyService.getClass().getMethod("account", currencyInterface, UUID.class);
                Object accountFuture = accountMethod.invoke(economyService, primaryCurrency, player.getUuid());
                
                // Wait for the CompletableFuture to complete
                Method joinMethod = accountFuture.getClass().getMethod("join");
                Object account = joinMethod.invoke(accountFuture);
                
                // Deposit money
                Method depositMethod = account.getClass().getMethod("deposit", BigDecimal.class);
                Object transaction = depositMethod.invoke(account, new BigDecimal(amount));
                
                // Check if transaction was successful
                Method successfulMethod = transaction.getClass().getMethod("successful");
                boolean success = (Boolean) successfulMethod.invoke(transaction);
                
                return success;
                
            } catch (Exception e) {
                PCReference.LOGGER.warn("Failed to deposit to Impactor: " + e.getMessage());
                // Fall back to dummy economy
            }
        }
        
        // Dummy economy fallback for singleplayer/testing
        double currentBalance = getBalance(player);
        playerBalances.put(player.getUuid(), currentBalance + amount);
        return true;
    }
    
    public static void sendEconomyMessage(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal("§a[Economy] " + message), false);
    }
    
    public static void sendErrorMessage(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal("§c[Economy] " + message), false);
    }
    
    public static String formatCurrency(double amount) {
        if (impactorAvailable && primaryCurrency != null) {
            try {
                // Try to use Impactor currency formatting
                Method symbolMethod = primaryCurrency.getClass().getMethod("symbol");
                String symbol = (String) symbolMethod.invoke(primaryCurrency);
                return symbol + String.format("%.2f", amount);
            } catch (Exception e) {
                // Fall back to default formatting
            }
        }
        return String.format("$%.2f", amount);
    }
    
    public static boolean isImpactorAvailable() {
        return checkImpactorAvailability();
    }
}
