package com.maximumg9.shadow.mixins.abilities.shadow;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.PoseidonsTrident;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(ServerChunkLoadingManager.EntityTracker.class)
public class EntityTrackerMixin {
    @org.spongepowered.asm.mixin.Shadow @Final
    Entity entity;

    @Inject(
        method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
        at=@At("HEAD"),
        cancellable = true
    )
    public void updateTrackedStatus(ServerPlayerEntity player, CallbackInfo ci) {
        if(!(this.entity instanceof TridentEntity trident)) return;
        if(!PoseidonsTrident.ID.equals(NBTUtil.getID(trident.getItemStack()))) return;

        Shadow shadow = getShadow(this.entity.getServer());
        IndirectPlayer iPlayer = shadow.getIndirect(player);

        if(iPlayer.role.getFaction() != Faction.SHADOW) {
            ci.cancel();
        }
    }
}
