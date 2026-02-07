package com.maximumg9.shadow.mixins.glowing;

import com.llamalad7.mixinextras.sugar.Local;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.MoonlitMark;
import com.maximumg9.shadow.abilities.SeeEnderEyesGlow;
import com.maximumg9.shadow.abilities.SeeGlowing;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @org.spongepowered.asm.mixin.Shadow
    @Final
    private Entity entity;
    
    @ModifyArg(method = "sendPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntityTrackerUpdateS2CPacket;<init>(ILjava/util/List;)V"))
    public List<DataTracker.SerializedEntry<?>> newEntityTrackerPacket(List<DataTracker.SerializedEntry<?>> changedEntries, @Local(argsOnly = true) ServerPlayerEntity player) {
        Shadow shadow = getShadow(player.getServer());
        Set<IndirectPlayer> markedPlayers = new HashSet<>();
        // Reminder to future self: make glowing not be here
        
        IndirectPlayer Iplayer = shadow.getIndirect(player);
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
            if (!Iplayer.role.hasAbility(SeeGlowing.ID) && !markedPlayers.contains(shadow.getIndirect((ServerPlayerEntity) this.entity))) {
                changedEntries.replaceAll(
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
                );
            }
        }
        
        if (!Iplayer.role.hasAbility(SeeEnderEyesGlow.ID)) {
            if (shadow.state.eyes.stream().anyMatch(eye -> eye.display().equals(this.entity.getUuid()))) {
                changedEntries.replaceAll(
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
                );
            }
        }
        
        return changedEntries;
    }
}
