package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.SheriffBow;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileMixin extends ProjectileEntity {
    public PersistentProjectileMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }
    
    
    @Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;F)F"))
    public float sheriffBowDamage(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseDamage) {
        Entity attacker = damageSource.getAttacker();
        if (attacker == null) return EnchantmentHelper.getDamage(world, stack, target, damageSource, baseDamage);
        if (!(attacker instanceof ServerPlayerEntity player))
            return EnchantmentHelper.getDamage(world, stack, target, damageSource, baseDamage);
        
        if (NBTUtil.hasID(stack, SheriffBow.ID)) {
            UUID ownerUUID = NBTUtil.getCustomData(stack).getUuid("owner");
            Shadow shadow = getShadow(player.getServer());
            IndirectPlayer owner = shadow.indirectPlayerManager.get(ownerUUID);
            IndirectPlayer iPlayer = shadow.getIndirect(player);
            
            if (target instanceof ServerPlayerEntity pTarget) {
                Role targetRole = shadow.getIndirect(pTarget).role;
                if (targetRole != null && owner.role != null && targetRole.getFaction() == owner.role.getFaction()) {
                    owner.scheduleUntil(
                        LivingEntity::kill,
                        CancelPredicates.cancelOnPhaseChange(shadow.state.phase)
                    );
                    iPlayer.scheduleUntil(
                        LivingEntity::kill,
                        CancelPredicates.cancelOnPhaseChange(shadow.state.phase)
                    );
                }
            }
            
            // Bow removal, if no bow in inventory, then no damage :)
            int val = player.getInventory().remove(
                (item) ->
                    NBTUtil.getCustomData(item)
                        .containsUuid("owner") &&
                        NBTUtil.getCustomData(item)
                            .getUuid("owner")
                            .equals(ownerUUID),
                1,
                player.playerScreenHandler.getCraftingInput()
            );
            
            if (val == 0) {
                boolean removedItem = false;
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack item = player.getInventory().getStack(i);
                    
                    if (NBTUtil.hasID(item, SheriffBow.ID)) {
                        removedItem = true;
                        item.decrement(1);
                        break;
                    }
                }
                if (!removedItem) {
                    var craftingInput = player.playerScreenHandler.getCraftingInput();
                    for (int i = 0; i < craftingInput.size(); i++) {
                        ItemStack item = player.getInventory().getStack(i);
                        
                        if (NBTUtil.hasID(item, SheriffBow.ID)) {
                            removedItem = true;
                            item.decrement(1);
                            break;
                        }
                    }
                }
                if (!removedItem) {
                    ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
                    if (NBTUtil.hasID(cursorStack, SheriffBow.ID)) {
                        removedItem = true;
                        cursorStack.decrement(1);
                    }
                }
                
                if (!removedItem) {
                    return 0;
                }
            }
            
            System.out.println("A");
            player.playSoundToPlayer(
                SoundEvent.of(Identifier.of("minecraft", "entity.item.break")),
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
            );
            return Float.MAX_VALUE;
        }
        
        
        return baseDamage;
    }
    
    @ModifyArg(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    public DamageSource entityHit(DamageSource source) {
        Entity attacker = source.getAttacker();
        if (attacker == null) return source;
        if (!(attacker instanceof ServerPlayerEntity player)) return source;
        ItemStack weaponStack = source.getWeaponStack();
        if (weaponStack == null) return source;
        if (!NBTUtil.getCustomData(weaponStack).containsUuid("owner")) return source;
        if (
            !player.getInventory().containsAny(
                (stack) -> NBTUtil.hasID(stack, SheriffBow.ID)
            )
        ) return source;
        
        return new DamageSource(
            this.getDamageSources().genericKill().getTypeRegistryEntry(),
            this,
            attacker
        );
    }
}
