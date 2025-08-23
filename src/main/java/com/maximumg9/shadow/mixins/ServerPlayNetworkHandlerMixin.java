package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.TimeUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@SuppressWarnings("SameReturnValue")
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @org.spongepowered.asm.mixin.Shadow
    public ServerPlayerEntity player;
    @org.spongepowered.asm.mixin.Shadow
    private static double clampHorizontal(double d) {
        return 0;
    }
    @org.spongepowered.asm.mixin.Shadow
    private static double clampVertical(double d) {
        return 0;
    }
    @org.spongepowered.asm.mixin.Shadow
    public abstract void requestTeleport(double x, double y, double z, float yaw, float pitch);
    @Inject(
        method = "onPlayerMove",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    public void restrictMovementOnLocationSelect(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        Shadow shadow = getShadow(this.player.server);
        
        if (!shadow.getIndirect(this.player).frozen) return;
        if (this.player.hasPermissionLevel(3) && (this.player.isInCreativeMode() || this.player.isSpectator())) return;
        
        double x = clampHorizontal(packet.getX(this.player.getX()));
        double y = clampVertical(packet.getY(this.player.getY()));
        double z = clampHorizontal(packet.getZ(this.player.getZ()));
        
        float yaw = MathHelper.wrapDegrees(packet.getYaw(this.player.getYaw()));
        float pitch = MathHelper.wrapDegrees(packet.getPitch(this.player.getPitch()));
        
        if (
            this.player.getX() == x &&
                this.player.getY() == y &&
                this.player.getZ() == z
        ) return;
        
        this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), yaw, pitch);
        ci.cancel();
    }
    
    @Redirect(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getAbilities()Lnet/minecraft/entity/player/PlayerAbilities;"))
    public PlayerAbilities getAbilities(ServerPlayerEntity instance) {
        Shadow shadow = getShadow(instance.server);
        
        PlayerAbilities abilities = instance.getAbilities();
        if (shadow.getIndirect(instance).frozen) {
            abilities.allowFlying = true;
        }
        
        return abilities;
    }
    
    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void restrictChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        Shadow shadow = getShadow(this.player.server);
        
        if (shadow.state.phase != GamePhase.PLAYING) return;
        
        if (shadow.config.disableChat) ci.cancel();
        
        IndirectPlayer p = shadow.getIndirect(this.player);
        if ((p.role == null || p.role.getFaction() == Faction.SPECTATOR) && !this.player.hasPermissionLevel(3)) {
            p.sendMessageNow(
                Text.literal("You are a spectator so you cannot chat").styled(style -> style.withColor(Formatting.YELLOW))
            );
            ci.cancel();
            return;
        }
        if (p.chatMessageCooldown > 0) {
            p.sendMessageNow(
                Text.literal("You are still on chat cooldown for ")
                    .styled(style -> style.withColor(Formatting.GRAY))
                    .append(
                        Text.literal(TimeUtil.ticksToText(p.chatMessageCooldown, false))
                            .styled(style -> style.withColor(Formatting.YELLOW))
                    )
                    .append(" seconds.")
            );
            ci.cancel();
            return;
        }
        p.chatMessageCooldown = shadow.config.chatMessageCooldown;
    }
}
