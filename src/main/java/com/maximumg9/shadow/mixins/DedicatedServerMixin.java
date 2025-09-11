package com.maximumg9.shadow.mixins;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Code theft from NCR

@Mixin(MinecraftDedicatedServer.class)
public class DedicatedServerMixin {
    /**
     * @reason If mod is installed on server - it does the exact opposite of what this option is
     * designed to enforce, so there's no reason to have it enabled.
     * @author Aizistral
     */

    @Inject(method = "shouldEnforceSecureProfile", at = @At("RETURN"), cancellable = true)
    private void onEnforceSecureProfile(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
