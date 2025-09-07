package com.maximumg9.shadow.roles.villager;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractVillager extends Role {
    protected AbstractVillager(@Nullable IndirectPlayer player, List<Ability.Factory> additionalAbilities) {
        super(player, additionalAbilities);
    }

    @Override
    public Faction getFaction() {
        return Faction.VILLAGER;
    }

    @Override
    public void onNight() {
        this.player.sendOverlay(
                Text.literal("It is now night. The power of the shadows grow.")
                        .styled(style -> style.withColor(Formatting.GREEN)),
                CancelPredicates.IS_DAY
        );
        this.player.giveEffect(
                new StatusEffectInstance(
                        StatusEffects.DARKNESS,
                        -1,0,
                        true,false,
                        true
                ),
                CancelPredicates.IS_DAY
        );
        super.onNight();
    }

    @Override
    public void onDay() {
        this.player.sendOverlay(
                Text.literal("It is now day.")
                        .styled(style -> style.withColor(Formatting.YELLOW)),
                CancelPredicates.IS_NIGHT
        );
        this.player.removeEffect(
                StatusEffects.DARKNESS,
                CancelPredicates.IS_NIGHT
        );
        super.onDay();
    }
}
