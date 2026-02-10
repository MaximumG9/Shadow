package com.maximumg9.shadow.mixins.abilities.villager.lifeweaver_rework;

// i hate this organization

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.LifeShield;
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
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "damage", at= @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V",
        ordinal = 0
    ))
    public void deathPrevention(LivingEntity instance, DamageSource damageSource) {
        if (instance.isPlayer()) {
            Shadow shadow = getShadow(instance.getServer());
            IndirectPlayer indirect = shadow.getIndirect((ServerPlayerEntity) instance);

            if (
                shadow.getAllLivingPlayers()
                .filter(p -> p.role.hasAbility(LifeShield.ID))
                .flatMap(
                    (p) -> p.role.getAbility(LifeShield.ID)
                        .map(a -> ((LifeShield) a).isPlayerShielded(indirect))
                        .stream())
                .anyMatch((b) -> b)
                ) {
                instance.setHealth(1.0f);
                instance.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
                instance.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
                instance.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
                instance.getWorld().sendEntityStatus(this, EntityStatuses.USE_TOTEM_OF_UNDYING);
            } else {
                instance.onDeath(damageSource);
            }
        } else {
            instance.onDeath(damageSource);
        }
    }
}
