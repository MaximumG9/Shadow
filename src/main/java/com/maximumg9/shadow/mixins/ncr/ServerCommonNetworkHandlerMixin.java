package com.maximumg9.shadow.mixins.ncr;

import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Code theft from NCR

@Mixin(ServerCommonNetworkHandler.class)
public class ServerCommonNetworkHandlerMixin {

    /**
     * @reason Convert player message to system message if mod is configured respectively.
     * This allows to circumvent signature check on client, as it only checks player messages.
     * @author JFronny (original implementation)
     * @author Aizistral
     */

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        Object self = this;

        if (self instanceof ServerPlayNetworkHandler listener) {
            if (packet instanceof ChatMessageS2CPacket chat) {
                packet = new GameMessageS2CPacket(
                    chat.serializedParameters().applyChatDecoration(
                        chat.unsignedContent() != null ? chat.unsignedContent()
                            : Text.literal(chat.body().content())
                    ),
                    false
                );

                ci.cancel();
                listener.sendPacket(packet);
            }
        }
    }
}
