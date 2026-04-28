package com.maximumg9.shadow.mixins.mechanics;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;
import java.util.function.Predicate;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(EnderDragonEntity.class)
public class DragonDeathMixin extends MobEntity {
    protected DragonDeathMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void onDeath(DamageSource damageSource) {
        Shadow shadow = getShadow(Objects.requireNonNull(this.getServer()));

        final Predicate<IndirectPlayer> V_VICTOR_CONDITION =
            (player) ->
                player.originalRole != null &&
                    (player.originalRole.faction == Faction.VILLAGER
                        || player.role.getRole() == Roles.PINATA);

        shadow.endGame(
            shadow
                .indirectPlayerManager
                .getRecentlyOnlinePlayers(shadow.config.disconnectTime)
                .stream()
                .filter(
                    V_VICTOR_CONDITION
                ).toList(),
            Faction.VILLAGER,
            null
        );
        super.onDeath(damageSource);
    }
}
