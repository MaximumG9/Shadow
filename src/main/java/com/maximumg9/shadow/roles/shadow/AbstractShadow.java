package com.maximumg9.shadow.roles.shadow;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.shadow.SeeGlowing;
import com.maximumg9.shadow.abilities.shadow.ToggleStrength;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractShadow extends Role {
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(ToggleStrength::new, SeeGlowing::new);
    
    AbstractShadow(IndirectPlayer player, List<Ability.Factory> abilityFactories, boolean overrideShadowAbilities) {
        super(
            player,
            Stream.concat(
                abilityFactories.stream(),
                overrideShadowAbilities ? Stream.of() : ABILITY_FACTORIES.stream()
            ).toList()
        );
    }

    AbstractShadow(IndirectPlayer player, List<Ability.Factory> abilityFactories) {
        super(
            player,
            Stream.concat(
                abilityFactories.stream(),
                ABILITY_FACTORIES.stream()
            ).toList()
        );
    }

    public static void announceShadowPartners(IndirectPlayer player) {
        if (player.getShadow()
            .getAllLivingPlayers()
            .noneMatch(
                (p) ->
                    p.role.getFaction() == Faction.SHADOW &&
                        p.playerUUID != player.playerUUID
            )) {
            player.sendMessage(
                Text.literal("There are no other shadows (good luck!)"),
                CancelPredicates.cancelOnPhaseChange(player.getShadow().state.phase)
            );
        } else {
            player.sendMessage(
                TextUtil.red("The other shadows are: ")
                    .append(
                        Texts.join(
                            player.getShadow()
                                .getAllPlayers()
                                .stream()
                                .filter(
                                    (p) ->
                                        p.role.getFaction() == Faction.SHADOW &&
                                            p != player
                                ).map(
                                    (p) -> p.getName().copy().setStyle(p.role.getStyle())
                                ).toList(),
                            TextUtil.gray(", ")
                        )
                    ),
                CancelPredicates.cancelOnPhaseChange(player.getShadow().state.phase)
            );
        }
    }

    @Override
    public void roleInit() {
        super.roleInit();
        announceShadowPartners(this.player);
    }
    
    @Override
    public Faction getFaction() { return Faction.SHADOW; }
    
    @Override
    public void onNight() {
        this.player.sendOverlay(
            TextUtil.gold("It is now night, your opportunity to kill"),
            CancelPredicates.IS_DAY
        );
        super.onNight();
    }
    
    @Override
    public void onDay() {
        this.player.sendOverlay(
            TextUtil.withColour("It's now day", Formatting.YELLOW),
            CancelPredicates.IS_NIGHT
        );
        super.onDay();
    }
}
