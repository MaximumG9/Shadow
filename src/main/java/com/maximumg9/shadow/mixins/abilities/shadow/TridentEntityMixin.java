package com.maximumg9.shadow.mixins.abilities.shadow;

import com.maximumg9.shadow.abilities.PoseidonsTrident;
import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin {

    @Redirect(
        method = "onEntityHit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/enchantment/EnchantmentHelper;getDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;F)F"
        )
    )
    public float tridentBreakShield(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseDamage) {
        if(NBTUtil.hasID(stack, PoseidonsTrident.ID)) {
            if(target instanceof PlayerEntity p) {
                if(p.isBlocking()) {
                    p.disableShield();
                }
            }
            float strengthAdditionalDamage = 0;
            if(damageSource.getAttacker() instanceof LivingEntity liver) {
                StatusEffectInstance effect = liver.getStatusEffect(StatusEffects.STRENGTH);
                if(effect != null) {
                    strengthAdditionalDamage = 3 * (
                        effect.getAmplifier() + 1
                    );
                }
            }
            return baseDamage + strengthAdditionalDamage;
        }
        return baseDamage;
    }
}
