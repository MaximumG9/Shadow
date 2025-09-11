package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
            TextUtil.green("It is now night. The power of the shadows grow."),
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
            TextUtil.withColour("It is now day.",Formatting.YELLOW),
            CancelPredicates.IS_NIGHT
        );
        this.player.removeEffect(
                StatusEffects.DARKNESS,
                CancelPredicates.IS_NIGHT
        );
        super.onDay();
    }
}
