package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    
    @Redirect(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;areItemsDifferent(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean areItemsDifferent(LivingEntity instance, ItemStack oldStack, ItemStack newStack) {
        if (NBTUtil.getCustomData(newStack).getBoolean(NBTUtil.INVISIBLE_KEY)) {
            return false;
        }
        return instance.areItemsDifferent(oldStack, newStack);
    }
}
