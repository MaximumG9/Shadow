package com.maximumg9.shadow.mixins;

import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Code theft from NCR

@Mixin(PlayerSessionC2SPacket.class)
public class PlayerSessionC2SPacketMixin {

    /**
     * @reason Ignore chat sessions from client, since we throw away signatures anyways.
     * @author Aizistral
     */

    @Inject(method = "apply(Lnet/minecraft/network/listener/ServerPlayPacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void onHandle(ServerPlayPacketListener listener, CallbackInfo info) {
        info.cancel();
    }
}
