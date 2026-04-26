package com.maximumg9.shadow.mixins.abilities.villager.lifeweaver_rework;

// i hate this organization

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.villager.LifeShield;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @org.spongepowered.asm.mixin.Shadow
    public abstract boolean damage(DamageSource source, float amount);

    @org.spongepowered.asm.mixin.Shadow public abstract void setHealth(float health);

    @org.spongepowered.asm.mixin.Shadow public abstract boolean addStatusEffect(StatusEffectInstance effect);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tryUseTotem", at= @At("HEAD"), cancellable = true)
    public void deathPrevention(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (this.isPlayer()) {
            Shadow shadow = getShadow(this.getServer());
            //noinspection DataFlowIssue
            IndirectPlayer indirect = shadow.getIndirect((ServerPlayerEntity) (Object) this);
            Entity attacker = source.getAttacker();
            IndirectPlayer indirectAttacker =
                attacker != null && attacker.isPlayer() ?
                    shadow.getIndirect((ServerPlayerEntity) attacker)
                    :
                    null;

            if (
                shadow.getAllLivingPlayers()
                .filter(p -> p.role.hasAbility(LifeShield.ID))
                .flatMap(
                    (p) -> p.role.getAbility(LifeShield.ID)
                        .map(a -> ((LifeShield) a).isPlayerShielded(indirect, indirectAttacker))
                        .stream())
                .anyMatch((b) -> b)
            ) {
                this.setHealth(1.0f);
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
                this.getWorld().sendEntityStatus(this, EntityStatuses.USE_TOTEM_OF_UNDYING);
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}
