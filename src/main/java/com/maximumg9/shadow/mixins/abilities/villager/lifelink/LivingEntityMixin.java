package com.maximumg9.shadow.mixins.abilities.villager.lifelink;

import com.llamalad7.mixinextras.sugar.Local;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "setHealth",cancellable = true,at=@At("HEAD"))
    public void hookSetHealth(float health, CallbackInfo ci) {
        if((Object) this instanceof ServerPlayerEntity player) {
            Shadow shadow = getShadow(this.getServer());
            IndirectPlayer iP = shadow.getIndirect(player);
            if(iP.link != null) {
                ci.cancel();
                iP.link.update(
                    null,
                    health,
                    player.getHealth(),
                    player
                );
            }
        }
    }

    // Hooked separately to get damage source
    @Redirect(
        method = "applyDamage",
        at = @At(
            value="INVOKE",
            target="Lnet/minecraft/entity/LivingEntity;setHealth(F)V"
        )
    )
    public void hookDamage(LivingEntity instance, float health, @Local(argsOnly = true)DamageSource source) {
        if((Object) instance instanceof ServerPlayerEntity player) {
            Shadow shadow = getShadow(this.getServer());
            IndirectPlayer iP = shadow.getIndirect(player);
            if(iP.link != null) {
                iP.link.update(
                    source,
                    health,
                    player.getHealth(),
                    player
                );
                return;
            }
        }
        instance.setHealth(health);
    }
}
