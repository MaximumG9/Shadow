package com.maximumg9.shadow.abilities.shadow;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.AbilityResult;
import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.abilities.filters.Filters;
import com.maximumg9.shadow.config.InternalTeam;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MoonlitMark extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("moonlit_mark");
    private static final ItemStack ITEM_STACK;
    private boolean usedToday = false;
    @Nullable
    private IndirectPlayer markedTarget;
    private boolean targetKilled = false;

    static {
        ITEM_STACK = new ItemStack(Items.SPECTRAL_ARROW, 1);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.blue("Moonlit Mark")
        );
    }

    public MoonlitMark(IndirectPlayer player) { super(player); }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ItemStack stack = ITEM_STACK.copy();
        stack.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.withColour("DURING THE DAY", Formatting.WHITE),
                TextUtil.gray("Mark the Nearest Player (Within ")
                    .append(String.valueOf(getShadow().config.markRadius))
                    .append(" blocks) to mark as a ")
                    .append(TextUtil.withColour("Target", Formatting.WHITE)),
                TextUtil.gray("During Night, if a target is killed by shadow or natural causes,"),
                TextUtil.gray("you gain ")
                    .append(TextUtil.red("Strength 2"))
                    .append(" for the remainder of the night."),
                TextUtil.withColour("Additionally, Targets glow red to ALL PLAYERS during Night.", Formatting.WHITE),
                AbilityText()
            )
        );
        return stack.copy();
    }

    @Override
    public void deInit() {
        this.player.removeFromTeamViewOverrides(markedTarget);
        super.deInit();
    }

    public Optional<IndirectPlayer> getMarkedTarget() {
        return Optional.ofNullable(markedTarget);
    }

    private void colorMarkedPlayer(IndirectPlayer p) {
        this.player.addTeamViewOverrides(List.of(p), InternalTeam.MARKED);
    }

    public void confirmTargetKill(boolean validKill) {
        targetKilled = validKill;
        markedTarget = null;
    }

    @Override
    public void onDay() {
        super.onDay();
        if(markedTarget != null) {
            markedTarget.addToTeamNow(InternalTeam.PLAYER);
        }
        markedTarget = null;
        usedToday = false;
        targetKilled = false;
    }

    @Override
    public void onNight() {
        super.onNight();
        if(markedTarget != null) {
            this.markedTarget.scheduleUntil(
                (p) -> {
                    p.setGlowing(true);
                    p.getDataTracker().set(
                        Entity.FLAGS,
                        (byte) (p.getDataTracker().get(Entity.FLAGS) |
                            (1 << Entity.GLOWING_FLAG_INDEX)),
                        true
                    );
                },
                CancelPredicates.cancelOnLostRole(markedTarget.role).or(CancelPredicates.IS_DAY)
            );
            markedTarget.addToTeamNow(InternalTeam.MARKED);
        }
    }

    @Override
    public void onAnyDeath(DamageSource damageSource, @NotNull IndirectPlayer deadPlayer) {
        if (deadPlayer == markedTarget) {
            if (getShadow().isNight()) {
                markedTarget.addToTeamNow(InternalTeam.PLAYER);
                if(damageSource.getAttacker() == null || !damageSource.getAttacker().isPlayer()) {
                    confirmTargetKill(true);
                } else {
                    IndirectPlayer indirectSource = getShadow().getIndirect((ServerPlayerEntity) damageSource.getAttacker());
                    confirmTargetKill(indirectSource.role.getFaction() == this.player.role.getFaction());
                }
            }
            this.player.removeFromTeamViewOverrides(deadPlayer);
        }
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public List<Filter> getFilters() {
        return List.of(
            new Filters.NotGracePeriod(),
            new Filters.Day()
        );
    }

    @Override
    public AbilityResult apply() {
        ServerPlayerEntity p = this.player.getPlayerOrThrow();

        if(usedToday) {
            this.player.sendMessageOrThrow(TextUtil.red("Cannot mark more than one player per day."));
            return AbilityResult.CLOSE;
        }

        ServerPlayerEntity target = null;

        double maxDistance = this.player.getShadow().config.markRadius;
        double targetDistance = maxDistance;
        for (ServerPlayerEntity serverPlayerEntity : p.getServerWorld().getPlayers(
            (player) -> {
                IndirectPlayer indirect = getShadow().getIndirect(player);
                return player.squaredDistanceTo(p) <= maxDistance * maxDistance
                    && indirect != this.player
                    && indirect.role.getFaction() != Faction.SPECTATOR;
            }
        )) {
            if (serverPlayerEntity.squaredDistanceTo(p) < targetDistance * targetDistance) {
                targetDistance = serverPlayerEntity.squaredDistanceTo(p);
                target = serverPlayerEntity;
            }
        }

        if (target == null) {
            this.player.sendMessageOrThrow(TextUtil.red("No players in range."));
            return AbilityResult.CLOSE;
        }


        IndirectPlayer indirectTarget = this.getShadow().getIndirect(target);
        colorMarkedPlayer(indirectTarget);
        markedTarget = indirectTarget;

        this.player.sendMessageOrThrow(TextUtil.green(markedTarget.getLiteralName()).append(" was successfully marked."));
        usedToday = true;
        return AbilityResult.CLOSE;
    }

    @Override
    public void tick() {
        super.tick();
        Optional<ServerPlayerEntity> possiblePlayer = this.player.getPlayer();

        if (targetKilled && possiblePlayer.isPresent()) {
            StatusEffectInstance possibleStrength = possiblePlayer.get().getStatusEffect(StatusEffects.STRENGTH);
            if(possibleStrength != null && possibleStrength.getAmplifier() == 0) {
                this.player.giveEffectOrThrow(
                    new StatusEffectInstance(
                        StatusEffects.STRENGTH,
                        -1,
                        1,
                        false,
                        false,
                        true
                    )
                );
            }
        }
    }
}
