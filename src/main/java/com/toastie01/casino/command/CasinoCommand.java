package com.toastie01.casino.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.toastie01.casino.config.CasinoConfig;
import net.minecraft.util.Formatting;
import com.toastie01.casino.economy.ServerEconomyManager;
import com.toastie01.casino.gui.CasinoMainGui;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

/**
 * Main casino command system providing /casino with GUI interface
 */
public class CasinoCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        CasinoConfig config = CasinoConfig.getInstance();
        
        var casinoCommand = literal("casino")
                .executes(CasinoCommand::openMainGui);
        
        // Add balance command with conditional OP commands
        var balanceCommand = literal("balance").executes(CasinoCommand::showBalance);
        if (config.enableBalanceCommands) {
            balanceCommand = balanceCommand
                .then(argument("amount", DoubleArgumentType.doubleArg(0.0))
                    .requires(source -> source.hasPermissionLevel(2)) // OP only
                    .executes(CasinoCommand::setBalance)
                    .then(argument("player", StringArgumentType.word())
                        .executes(CasinoCommand::setPlayerBalance)));
        }
        
        casinoCommand = casinoCommand
                .then(balanceCommand)
                .then(literal("help").executes(CasinoCommand::showHelp));
        
        dispatcher.register(casinoCommand);
    }
    
    private static int openMainGui(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        new CasinoMainGui(player).open();
        return 1;
    }
    
    private static int showBalance(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerEconomyManager.getBalance(player, balance -> {
            player.sendMessage(Text.literal("Your balance: ").formatted(Formatting.GREEN).append(Text.literal("$" + String.format("%.2f", balance)).formatted(Formatting.GOLD)), false);
        });
        return 1;
    }
    
    private static int setBalance(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        double amount = DoubleArgumentType.getDouble(context, "amount");
        
        // Only allow for dummy economy (not Impactor)
        if (ServerEconomyManager.isUsingImpactor()) {
            player.sendMessage(Text.literal("Balance management is not available when using Impactor economy.").formatted(Formatting.RED), false);
            return 0;
        }
        
        ServerEconomyManager.setBalance(player, amount);
        player.sendMessage(Text.literal("Your balance has been set to: ").formatted(Formatting.GREEN).append(Text.literal("$" + String.format("%.2f", amount)).formatted(Formatting.GOLD)), false);
        return 1;
    }

    private static int setPlayerBalance(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String targetPlayerName = StringArgumentType.getString(context, "player");
        double amount = DoubleArgumentType.getDouble(context, "amount");
        ServerCommandSource source = context.getSource();
        
        // Only allow for dummy economy (not Impactor)
        if (ServerEconomyManager.isUsingImpactor()) {
            source.sendMessage(Text.literal("Balance management is not available when using Impactor economy.").formatted(Formatting.RED));
            return 0;
        }
        
        ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            source.sendMessage(Text.literal("Player '" + targetPlayerName + "' not found.").formatted(Formatting.RED));
            return 0;
        }
        
        ServerEconomyManager.setBalance(targetPlayer, amount);
        source.sendMessage(Text.literal("Set " + targetPlayerName + "'s balance to: ").formatted(Formatting.GREEN).append(Text.literal("$" + String.format("%.2f", amount)).formatted(Formatting.GOLD)));
        targetPlayer.sendMessage(Text.literal("Your balance has been set to: ").formatted(Formatting.GREEN).append(Text.literal("$" + String.format("%.2f", amount)).formatted(Formatting.GOLD)), false);
        return 1;
    }
    
    private static int showHelp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        CasinoConfig config = CasinoConfig.getInstance();
        
        player.sendMessage(Text.literal("§6=== Casino Commands ==="), false);
        player.sendMessage(Text.literal("§e/casino §7- Open main casino GUI"), false);
        
        player.sendMessage(Text.literal("§e/casino balance §7- Check your balance"), false);
        
        if (config.enableBalanceCommands && context.getSource().hasPermissionLevel(2)) {
            player.sendMessage(Text.literal("§e/casino balance <amount> §7- Set your balance (OP only, dummy economy only)"), false);
            player.sendMessage(Text.literal("§e/casino balance <amount> <player> §7- Set player's balance (OP only, dummy economy only)"), false);
        }
        
        player.sendMessage(Text.literal("§e/casino help §7- Show this help"), false);
        return 1;
    }
}
