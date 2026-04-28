package com.maximumg9.shadow.mixins.playervisuals;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(ServerScoreboard.class)
public class ServerScoreBoardMixin {
    @org.spongepowered.asm.mixin.Shadow
    @Final
    private MinecraftServer server;

    @WrapOperation(
        method = "updateScoreboardTeamAndPlayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"
        )
    )
    void modifyPacketUpdateTeam(PlayerManager instance, Packet<?> packet, Operation<Void> original, @Local(argsOnly = true) Team team) {
        Shadow shadow = getShadow(this.server);

        instance.getPlayerList().forEach(
            (player) -> {
                Collection<String> playerNames = team.getPlayerList();
                IndirectPlayer iPlayer = shadow.getIndirect(player);

                HashMap<String, Team> viewOverrides = iPlayer.getLiteralViewOverrides();
                Collection<String> overriddenNames = viewOverrides
                    .keySet()
                    .stream()
                    .filter(
                        (name) -> viewOverrides.get(name) == team
                    )
                    .toList();

                playerNames.removeAll(viewOverrides.keySet());
                playerNames.addAll(overriddenNames);

                player.networkHandler.sendPacket(
                    TeamS2CPacketAccessor.newTeamS2CPacket(
                        team.getName(),
                        0,
                        Optional.of(new TeamS2CPacket.SerializableTeam(team)),
                        playerNames
                    )
                );
            }
        );
    }

    @WrapOperation(
        method = "addScoreHolderToTeam",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"
        )
    )
    void modifyPacketAddPlayer(PlayerManager instance, Packet<?> packet, Operation<Void> original, @Local(argsOnly = true) Team team, @Local(argsOnly = true) String scoreHolderName) {
        Shadow shadow = getShadow(this.server);

        instance.getPlayerList().forEach(
            (player) -> {
                IndirectPlayer iPlayer = shadow.getIndirect(player);

                if (!iPlayer.getLiteralViewOverrides().containsKey(scoreHolderName)) {
                    player.networkHandler.sendPacket(
                        TeamS2CPacket.changePlayerTeam(team, scoreHolderName, TeamS2CPacket.Operation.ADD)
                    );
                }
            }
        );
    }

    @WrapOperation(
        method = "removeScoreHolderFromTeam",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"
        )
    )
    void modifyPacketRemovePlayer(PlayerManager instance, Packet<?> packet, Operation<Void> original, @Local(argsOnly = true) Team team, @Local(argsOnly = true) String scoreHolderName) {
        Shadow shadow = getShadow(this.server);

        instance.getPlayerList().forEach(
            (player) -> {
                IndirectPlayer iPlayer = shadow.getIndirect(player);

                if (!iPlayer.getLiteralViewOverrides().containsKey(scoreHolderName)) {
                    player.networkHandler.sendPacket(
                        TeamS2CPacket.changePlayerTeam(team, scoreHolderName, TeamS2CPacket.Operation.REMOVE)
                    );
                }
            }
        );
    }
}
