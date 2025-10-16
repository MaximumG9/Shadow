package com.maximumg9.shadow.mixins.mechanics;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Faction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(EnderDragonEntity.class)
public class DragonDeathMixin extends MobEntity {
    protected DragonDeathMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void onDeath(DamageSource damageSource) {
        Shadow shadow = getShadow(Objects.requireNonNull(this.getServer()));
        
        shadow.endGame(
            shadow
                .indirectPlayerManager
                .getRecentlyOnlinePlayers(shadow.config.disconnectTime)
                .stream()
                .filter((player) -> {
                    if(player.originalRole == null) {
                        return false;
                    }
                    return player.originalRole.faction == Faction.VILLAGER;
                }).toList(),
            Faction.VILLAGER,
            null
        );
    }
}
