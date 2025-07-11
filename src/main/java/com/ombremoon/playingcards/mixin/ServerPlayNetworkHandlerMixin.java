package com.ombremoon.playingcards.mixin;

import com.ombremoon.playingcards.util.SellUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to handle shift-clicking casino items to sell them.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    
    @Shadow
    public ServerPlayerEntity player;
    
    @Inject(method = "onClickSlot", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        // Check if this is a shift-click action
        if (packet.getActionType() == SlotActionType.QUICK_MOVE) {
            ScreenHandler screenHandler = this.player.currentScreenHandler;
            
            // Only process for player inventory slots (not GUI slots)
            if (packet.getSlot() >= 0 && packet.getSlot() < screenHandler.slots.size()) {
                ItemStack clickedStack = screenHandler.getSlot(packet.getSlot()).getStack();
                
                // Check if it's a casino item that can be sold
                if (SellUtils.isCasinoItem(clickedStack)) {
                    // Sell the item
                    double value = SellUtils.sellItemStack(this.player, clickedStack.copy());
                    
                    if (value > 0) {
                        // Remove the item from the slot
                        screenHandler.getSlot(packet.getSlot()).setStack(ItemStack.EMPTY);
                        
                        // Cancel the normal shift-click behavior
                        ci.cancel();
                    }
                }
            }
        }
    }
}
