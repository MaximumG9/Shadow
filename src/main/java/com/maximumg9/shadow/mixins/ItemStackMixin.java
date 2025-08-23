package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.items.ItemUseCallback;
import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.component.ComponentHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {
    @Unique
    private static ItemStack c(Object mixin) {
        return (ItemStack) mixin;
    }
    
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        Identifier id = NBTUtil.getID(c(this));
        ItemUseCallback callback = Shadow.ITEM_USE_CALLBACK_MAP.get(id);
        
        if (callback != null) {
            TypedActionResult<ItemStack> result = callback.use(world, user, hand);
            if (result != null) {
                cir.setReturnValue(result);
                cir.cancel();
            }
        }
    }
}
