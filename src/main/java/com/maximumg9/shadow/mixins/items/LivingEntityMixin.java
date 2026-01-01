package com.maximumg9.shadow.mixins.items;

import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public abstract @NotNull ItemStack getWeaponStack();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // Increase order so trident mixin applies first
    @Inject(method = "getEquipmentChanges", at = @At(value = "TAIL"), order = 1100)
    private void areItemsDifferent(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if(cir.getReturnValue() == null) return;
        cir.getReturnValue().entrySet().removeIf((entry) -> {
            ItemStack stack = entry.getValue();
            return NBTUtil.getCustomData(stack).getBoolean(NBTUtil.INVISIBLE_KEY);
        });
    }

    @Redirect(method = "getEquipmentChanges",at= @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",ordinal = 1))
    public boolean dontUpdateAttributeModifiers(ItemStack oldStack) {
        return oldStack.isEmpty() || NBTUtil.getCustomData(oldStack).getBoolean(NBTUtil.DISABLE_ATTRIBUTES_KEY);
    }

}
