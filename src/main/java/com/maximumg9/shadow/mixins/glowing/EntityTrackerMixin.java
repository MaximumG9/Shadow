package com.maximumg9.shadow.mixins.glowing;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.MoonlitMark;
import com.maximumg9.shadow.abilities.SeeEnderEyesGlow;
import com.maximumg9.shadow.abilities.SeeGlowing;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.Set;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(ServerChunkLoadingManager.EntityTracker.class)
public class EntityTrackerMixin {
    @org.spongepowered.asm.mixin.Shadow
    @Final
    Entity entity;
    
    @Redirect(
        method = "sendToOtherNearbyPlayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/PlayerAssociatedNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
        )
    )
    public void sendToOtherNearbyPlayers(PlayerAssociatedNetworkHandler instance, Packet<?> packet) {
        if (packet.getPacketId() != PlayPackets.SET_ENTITY_DATA) {
            instance.sendPacket(packet);
            return;
        }
        Shadow shadow = getShadow(instance.getPlayer().getServer());
        Set<IndirectPlayer> markedPlayers = new HashSet<>();
        IndirectPlayer player = shadow.getIndirect(instance.getPlayer());
        
        if (!(packet instanceof EntityTrackerUpdateS2CPacket originalPacket)) {
            instance.sendPacket(packet);
            return;
        }
        
        if (shadow.isNight() && this.entity.getType() == EntityType.PLAYER) {
            shadow.getAllLivingPlayers()
                .filter(p -> p.role.hasAbility(MoonlitMark.ID))
                .flatMap(
                    (p) -> p.role.getAbility(MoonlitMark.ID)
                        .flatMap(a -> ((MoonlitMark) a).getMarkedTarget())
                        .stream()
                ).forEach(
                    markedPlayers::add
                );
            if (!player.role.hasAbility(SeeGlowing.ID) && !markedPlayers.contains(shadow.getIndirect((ServerPlayerEntity) this.entity))) {
                EntityTrackerUpdateS2CPacket noGlowingPacket = new EntityTrackerUpdateS2CPacket(
                    originalPacket.id(),
                    originalPacket.trackedValues()
                        .stream()
                        .map(
                            (entry) -> {
                                if (entry.id() == Entity.FLAGS.id()) {
                                    @SuppressWarnings("unchecked")
                                    DataTracker.SerializedEntry<Byte> bEntry = (DataTracker.SerializedEntry<Byte>) entry;

                                    return new DataTracker.SerializedEntry<>(
                                        bEntry.id(),
                                        bEntry.handler(),
                                        (byte) (bEntry.value() & ~(1 << Entity.GLOWING_FLAG_INDEX))
                                    );
                                }
                                return entry;
                            }
                        ).toList()
                );
                instance.sendPacket(noGlowingPacket);
                return;
            }
        }
        
        if (!player.role.hasAbility(SeeEnderEyesGlow.ID)) {
            if (shadow.state.eyes.stream().anyMatch(eye -> eye.display().equals(this.entity.getUuid()))) {
                EntityTrackerUpdateS2CPacket eyeGlowingPacket = new EntityTrackerUpdateS2CPacket(
                    originalPacket.id(),
                    originalPacket.trackedValues()
                        .stream()
                        .map(
                            (entry) -> {
                                if (entry.id() == Entity.FLAGS.id()) {
                                    @SuppressWarnings("unchecked")
                                    DataTracker.SerializedEntry<Byte> bEntry = (DataTracker.SerializedEntry<Byte>) entry;
                                    
                                    return new DataTracker.SerializedEntry<>(
                                        bEntry.id(),
                                        bEntry.handler(),
                                        (byte) (bEntry.value() & ~(1 << Entity.GLOWING_FLAG_INDEX))
                                    );
                                }
                                return entry;
                            }
                        ).toList()
                );
                instance.sendPacket(eyeGlowingPacket);
                return;
            }
        }
        
        instance.sendPacket(originalPacket);
        
    }
}
