package com.maximumg9.shadow.mixins.mechanics;

import com.maximumg9.shadow.abilities.Haunt;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Redirect(method = "changeGameMode", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V",
        ordinal = 0
    ))
    public void sendPackets(PlayerManager instance, Packet<?> packet) {
        IndirectPlayer indirectPlayer = getShadow(instance.getServer()).getIndirect(player);

        if(indirectPlayer.role.hasAbility(Haunt.ID) && indirectPlayer.role.getAbility(Haunt.ID).get().getToggled()) {
            player.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_GAME_MODE, player));
        } else {
            instance.sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_GAME_MODE, player));
        }
    }
}
