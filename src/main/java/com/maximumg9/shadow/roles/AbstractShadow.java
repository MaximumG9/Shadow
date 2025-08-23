package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.SeeGlowing;
import com.maximumg9.shadow.abilities.ToggleStrength;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractShadow extends Role {
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(ToggleStrength::new, SeeGlowing::new);
    
    AbstractShadow(IndirectPlayer player, List<Ability.Factory> abilityFactories) {
        super(
            player,
            Stream.concat(
                abilityFactories.stream(),
                ABILITY_FACTORIES.stream()
            ).toList()
        );
    }
    
    @Override
    public void init() {
        super.init();
        if (this.player.getShadow()
            .getAllPlayers()
            .stream()
            .noneMatch(
                (player) ->
                    player.role != null &&
                        player.role.getFaction() == Faction.SHADOW &&
                        player.playerUUID != this.player.playerUUID
            )) {
            this.player.sendMessage(
                Text.literal("The are no other shadows (good luck!)"),
                CancelPredicates.cancelOnPhaseChange(this.player.getShadow().state.phase)
            );
        } else {
            this.player.sendMessage(
                Text.literal("The other shadows are: ")
                    .styled(style -> style.withColor(Formatting.RED))
                    .append(
                        Texts.join(
                            this.player.getShadow()
                                .getAllPlayers()
                                .stream()
                                .filter(
                                    (player) ->
                                        player.role != null &&
                                            player.role.getFaction() == Faction.SHADOW &&
                                            player.playerUUID != this.player.playerUUID
                                ).map(
                                    (player) -> player.getName().copy().setStyle(player.role.getStyle())
                                ).toList(),
                            Text.literal(", ").styled((style -> style.withColor(Formatting.GRAY)))
                        )
                    ),
                CancelPredicates.cancelOnPhaseChange(this.player.getShadow().state.phase)
            );
        }
    }
    
    @Override
    public Faction getFaction() { return Faction.SHADOW; }
    
    @Override
    public void onNight() {
        this.player.sendOverlay(
            Text.literal("It is now night, your opportunity to kill")
                .styled(style -> style.withColor(Formatting.GOLD)),
            CancelPredicates.IS_DAY
        );
        super.onNight();
    }
    
    @Override
    public void onDay() {
        this.player.sendOverlay(
            Text.literal("It's now day")
                .styled(style -> style.withColor(Formatting.YELLOW)),
            CancelPredicates.IS_NIGHT
        );
        super.onDay();
    }
}
