package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Inject(method = "canInsertIntoSlot(Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;)Z", at = @At("HEAD"), cancellable = true)
    public void canInsertIntoSlot(ItemStack stack, Slot slot, CallbackInfoReturnable<Boolean> cir) {
        if (NBTUtil.getCustomData(stack).getBoolean(NBTUtil.RESTRICT_MOVEMENT_KEY)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
