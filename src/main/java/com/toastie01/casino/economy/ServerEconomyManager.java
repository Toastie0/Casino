package com.toastie01.casino.economy;

import com.toastie01.casino.PCReference;
import com.toastie01.casino.config.CasinoConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
    
    private static Boolean impactorAvailable = null; // null = not checked yet
    private static Object economyService = null;
    private static Object primaryCurrency = null;
    
    public static void initialize() {
        // Impactor integration will be checked when first needed
    }
    
    private static boolean checkImpactorAvailability() {
        if (impactorAvailable != null) {
            return impactorAvailable; // Already checked
        }
        
        // Check config first - if forcing dummy economy, don't even check for Impactor
        CasinoConfig config = CasinoConfig.getInstance();
        if (config.shouldForceDummyEconomy()) {
            impactorAvailable = false;
            PCReference.LOGGER.info("Economy mode forced to dummy - skipping Impactor detection");
            return false;
        }
        
        // If requiring Impactor, it must be available
        if (config.shouldRequireImpactor()) {
            boolean available = checkImpactorClasses();
            if (!available) {
                PCReference.LOGGER.error("Economy mode set to 'impactor' but Impactor is not available!");
                throw new RuntimeException("Impactor economy required but not found");
            }
            impactorAvailable = true;
            return true;
        }
        
        // Auto-detect mode (default)
        impactorAvailable = checkImpactorClasses();
        return impactorAvailable;
    }
    
    private static boolean checkImpactorClasses() {
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
                Object account = getImpactorAccount(player.getUuid());
                Method balanceMethod = account.getClass().getMethod("balance");
                BigDecimal balance = (BigDecimal) balanceMethod.invoke(account);
                return balance.doubleValue();
            } catch (Exception e) {
                PCReference.LOGGER.warn("Failed to get balance from Impactor: " + e.getMessage());
            }
        }
        
        // Dummy economy fallback for singleplayer/testing
        return playerBalances.getOrDefault(player.getUuid(), CasinoConfig.getInstance().startingBalance);
    }
    
    public static boolean hasBalance(ServerPlayerEntity player, double amount) {
        return getBalance(player) >= amount;
    }
    
    public static boolean withdraw(ServerPlayerEntity player, double amount) {
        if (checkImpactorAvailability()) {
            try {
                Object account = getImpactorAccount(player.getUuid());
                Method withdrawMethod = account.getClass().getMethod("withdraw", BigDecimal.class);
                Object transaction = withdrawMethod.invoke(account, new BigDecimal(amount));
                
                Method successfulMethod = transaction.getClass().getMethod("successful");
                return (Boolean) successfulMethod.invoke(transaction);
            } catch (Exception e) {
                PCReference.LOGGER.warn("Failed to withdraw from Impactor: " + e.getMessage());
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
                Object account = getImpactorAccount(player.getUuid());
                Method depositMethod = account.getClass().getMethod("deposit", BigDecimal.class);
                Object transaction = depositMethod.invoke(account, new BigDecimal(amount));
                
                Method successfulMethod = transaction.getClass().getMethod("successful");
                return (Boolean) successfulMethod.invoke(transaction);
            } catch (Exception e) {
                PCReference.LOGGER.warn("Failed to deposit to Impactor: " + e.getMessage());
            }
        }
        
        // Dummy economy fallback for singleplayer/testing
        double currentBalance = getBalance(player);
        playerBalances.put(player.getUuid(), currentBalance + amount);
        return true;
    }
    
    public static void sendEconomyMessage(ServerPlayerEntity player, String message) {
        Text text = Text.literal("[Economy] ").formatted(Formatting.GREEN)
                       .append(Text.literal(message).formatted(Formatting.WHITE));
        player.sendMessage(text, false);
    }
    
    public static void sendErrorMessage(ServerPlayerEntity player, String message) {
        Text text = Text.literal("[Economy] ").formatted(Formatting.RED)
                       .append(Text.literal(message).formatted(Formatting.WHITE));
        player.sendMessage(text, false);
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
    
    public static boolean isUsingImpactor() {
        return isImpactorAvailable();
    }
    
    public static void setBalance(ServerPlayerEntity player, double amount) {
        if (isImpactorAvailable()) {
            // Cannot set balance in Impactor - should not be called
            PCReference.LOGGER.warn("Attempted to set balance while using Impactor economy");
            return;
        }
        
        // For dummy economy, set the balance directly
        playerBalances.put(player.getUuid(), amount);
    }
    
    public static void getBalance(ServerPlayerEntity player, java.util.function.Consumer<Double> callback) {
        double balance = getBalance(player);
        callback.accept(balance);
    }
    
    /**
     * Helper method to get Impactor account, reducing code duplication
     */
    private static Object getImpactorAccount(UUID playerId) throws Exception {
        Class<?> currencyInterface = Class.forName("net.impactdev.impactor.api.economy.currency.Currency");
        Method accountMethod = economyService.getClass().getMethod("account", currencyInterface, UUID.class);
        Object accountFuture = accountMethod.invoke(economyService, primaryCurrency, playerId);
        
        // Wait for the CompletableFuture to complete
        Method joinMethod = accountFuture.getClass().getMethod("join");
        return joinMethod.invoke(accountFuture);
    }
}
