package com.maximumg9.shadow.mixins.ncr;

import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Code theft from NCR

@Mixin(ChatCommandSignedC2SPacket.class)
public class ChatCommandSignedC2SPacketMixin {

    /**
     * @reason Same as [REDACTED].
     * @author Aizistral
     */

    @Inject(method = "argumentSignatures", at = @At("RETURN"), cancellable = true)
    private void onGetSignatures(CallbackInfoReturnable<ArgumentSignatureDataMap> info) {
        info.setReturnValue(ArgumentSignatureDataMap.EMPTY);
    }
}
