package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.TimeUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, SignedMessage message, CallbackInfo ci) throws CommandSyntaxException {
        if (!source.isExecutedByPlayer()) return;
        ServerPlayerEntity p = source.getPlayerOrThrow();
        Shadow shadow = getShadow(source.getServer());
        
        if (shadow.state.phase != GamePhase.PLAYING) return;
        if (shadow.config.disableChat) ci.cancel();
        
        IndirectPlayer player = shadow.getIndirect(p);
        if ((player.role == null || player.role.getFaction() == Faction.SPECTATOR) && !p.hasPermissionLevel(3)) {
            player.sendMessageNow(
                TextUtil.withColour(
                    "You are a spectator so you cannot message",
                    Formatting.YELLOW
                )
            );
            ci.cancel();
            return;
        }
        
        if (player.chatMessageCooldown > 0) {
            player.sendMessageNow(
                TextUtil.gray("You are still on chat cooldown for ")
                    .append(
                        TextUtil.withColour(
                            TimeUtil.ticksToText(player.chatMessageCooldown, false),
                            Formatting.YELLOW
                        )
                    )
                    .append(" seconds.")
            );
            ci.cancel();
            return;
        }
        
        player.chatMessageCooldown = shadow.config.chatMessageCooldown;
    }
}
