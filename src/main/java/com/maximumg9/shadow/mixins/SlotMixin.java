package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {
    @Shadow
    @Final
    public Inventory inventory;
    
    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.inventory instanceof PlayerInventory) return;
        if (!NBTUtil.getCustomData(stack).getBoolean(NBTUtil.RESTRICT_MOVEMENT_KEY)) return;
        cir.setReturnValue(false);
        cir.cancel();
    }
}
