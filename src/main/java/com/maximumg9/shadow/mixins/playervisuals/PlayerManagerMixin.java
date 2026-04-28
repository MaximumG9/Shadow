package com.maximumg9.shadow.mixins.playervisuals;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    // Player Connect
    @WrapOperation(
        method = "sendScoreboard",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/packet/s2c/play/TeamS2CPacket;updateTeam(Lnet/minecraft/scoreboard/Team;Z)Lnet/minecraft/network/packet/s2c/play/TeamS2CPacket;"
        )
    )
    TeamS2CPacket updateTeam(Team team, boolean updatePlayers, Operation<TeamS2CPacket> original, @Local(argsOnly = true) ServerPlayerEntity player) {
        // temp method to get shadow to get indirect

        Collection<String> playerNames = updatePlayers ? team.getPlayerList() : ImmutableList.of();

        if (updatePlayers) {
            Shadow shadow = getShadow(player.server);

            IndirectPlayer iPlayer = shadow.indirectPlayerManager.getIndirect(player);
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
        }

        return TeamS2CPacketAccessor.newTeamS2CPacket(
            team.getName(),
            updatePlayers ? 0 : 2,
            Optional.of(new TeamS2CPacket.SerializableTeam(team)),
            playerNames
        );
    }

}
