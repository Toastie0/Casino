package com.ombremoon.playingcards.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ombremoon.playingcards.economy.EconomyManager;
import com.ombremoon.playingcards.gui.ChipShopGui;
import com.ombremoon.playingcards.init.ModItems;
import com.ombremoon.playingcards.item.ItemPokerChip;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ChipsCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("chips")
                .then(literal("buy")
                    .executes(ChipsCommand::openShopGui))
                .then(literal("sell")
                    .executes(ChipsCommand::sellAllChips))
                .then(literal("test")
                    .executes(ChipsCommand::testCommand))
        );
    }
    
    private static int openShopGui(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ChipShopGui gui = new ChipShopGui(player);
        gui.open();
        return 1;
    }
    
    private static int sellAllChips(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        double totalValue = 0.0;
        int chipsSold = 0;
        
        // Check entire inventory for poker chips (exclude armor slots)
        for (int i = 0; i < player.getInventory().main.size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof ItemPokerChip pokerChip) {
                double value = pokerChip.getValue() * stack.getCount();
                totalValue += value;
                chipsSold += stack.getCount();
                player.getInventory().setStack(i, ItemStack.EMPTY);
            }
        }
        
        if (chipsSold > 0) {
            EconomyManager.deposit(player, totalValue);
            EconomyManager.sendEconomyMessage(player, 
                String.format("Sold %d chip%s for $%.2f", 
                    chipsSold, chipsSold == 1 ? "" : "s", totalValue));
            return 1;
        }
        
        player.sendMessage(Text.literal("§cNo poker chips found in inventory!"), false);
        return 0;
    }
    
    private static int testCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        player.sendMessage(Text.literal("§aPlaying Cards mod is working!"), false);
        return 1;
    }
}
