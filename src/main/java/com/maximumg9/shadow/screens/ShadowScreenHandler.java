package com.maximumg9.shadow.screens;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public abstract class ShadowScreenHandler extends ScreenHandler {
    private final PlayerInventory playerInventory;
    
    public ShadowScreenHandler(ScreenHandlerType<?> type, int syncID, PlayerInventory playerInventory) {
        super(type, syncID);
        
        this.playerInventory = playerInventory;
    }
    
    void initSlots() {
        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 9; k++) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 0, 0));
            }
        }
        
        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 0, 0));
        }
    }
}
