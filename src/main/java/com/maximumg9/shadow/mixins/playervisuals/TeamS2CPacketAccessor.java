package com.maximumg9.shadow.mixins.playervisuals;

import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;
import java.util.Optional;

@Mixin(TeamS2CPacket.class)
public interface TeamS2CPacketAccessor {
    @Invoker("<init>")
    static TeamS2CPacket newTeamS2CPacket(String teamName, int packetType, Optional<TeamS2CPacket.SerializableTeam> team, Collection<String> playerNames) {
        return null;
    }
}
