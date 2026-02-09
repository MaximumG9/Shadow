package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.abilities.filters.Filters;
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
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
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
                TextUtil.withColour("AT ANY TIME", Formatting.WHITE),
                TextUtil.gray("Mark the Nearest Player (Within ")
                    .append(String.valueOf(getShadow().config.markRadius))
                    .append(" blocks) to mark as a ")
                    .append(TextUtil.withColour("Target", Formatting.WHITE)),
                TextUtil.gray("During Night Time, if a target is killed by shadow or natural causes,"),
                TextUtil.gray("you gain ")
                    .append(TextUtil.red("Strength 2"))
                    .append(" for the remainder of the night."),
                TextUtil.withColour("Additionally, Targets glow red to ALL PLAYERS during Night.", Formatting.WHITE),
                AbilityText()
            )
        );
        return stack.copy();
    }

    public Optional<IndirectPlayer> getMarkedTarget() {
        return Optional.ofNullable(markedTarget);
    }

    private void glowMarkedPlayer(IndirectPlayer p) {
        this.player.getPlayerOrThrow().networkHandler.sendPacket(
            TeamS2CPacket.changePlayerTeam(
                Objects.requireNonNull(
                    getShadow()
                        .getServer()
                        .getScoreboard()
                        .getTeam("DuskMarked")
                ),
                p.getName().getString(),
                TeamS2CPacket.Operation.ADD
            )
        );
    }

    public void confirmTargetKill(boolean validKill) {
        if (validKill) {
            targetKilled = true;
            markedTarget = null;
        } else {
            targetKilled = false;
            markedTarget = null;
        }
    }

    @Override
    public void onDay() {
        super.onDay();
        if(markedTarget != null) {
            markedTarget.addToTeam(this.getShadow().playerTeam, CancelPredicates.cancelOnLostRole(markedTarget.role));
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
                //when using instead CancelPredicates.cancelOnLostAbility(this), schedule was cancelled without this ability
                //being lost.
                CancelPredicates.cancelOnLostRole(markedTarget.role).or(CancelPredicates.IS_DAY)
            );
            this.player.getPlayer().ifPresent(
                (p) ->
                    p.networkHandler.sendPacket(
                    TeamS2CPacket.changePlayerTeam(
                        this.player.getShadow().playerTeam,
                        markedTarget.getName().getString(),
                        TeamS2CPacket.Operation.ADD
                    )
                )
            );
            markedTarget.addToTeam(this.getShadow().markedTeam,
                CancelPredicates.cancelOnLostRole(markedTarget.role).or(CancelPredicates.IS_DAY));
        }
    }

    @Override
    public void onAnyDeath(DamageSource damageSource, IndirectPlayer deadPlayer) {
        if (deadPlayer == markedTarget) {
            this.player.getPlayer().ifPresent(
                (p) ->
                    p.networkHandler.sendPacket(
                        TeamS2CPacket.changePlayerTeam(
                            this.player.getShadow().playerTeam,
                            markedTarget.getName().getString(),
                            TeamS2CPacket.Operation.ADD
                        )
                    )
            );
            markedTarget.addToTeamNow(this.getShadow().playerTeam);

            if (getShadow().isNight()) {
                if(damageSource.getAttacker() == null || !damageSource.getAttacker().isPlayer()) {
                    confirmTargetKill(true);
                } else {
                    IndirectPlayer indirectSource = getShadow().getIndirect((ServerPlayerEntity) damageSource.getAttacker());
                    // note to self to make not Faction.VILLAGER forced
                    confirmTargetKill(indirectSource.role.getFaction() != Faction.VILLAGER);
                }
            }
        }
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public List<Filter> getFilters() {
        return List.of(
            new Filters.NotGracePeriod()
        );
    }

    @Override
    public AbilityResult apply() {
        ServerPlayerEntity p = this.player.getPlayerOrThrow();

        if(usedToday) {
            this.player.sendMessageNow(TextUtil.red("Cannot mark more than one player per day."));
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
            this.player.sendMessageNow(TextUtil.red("No players in range."));
            return AbilityResult.CLOSE;
        }

        if (getShadow().isNight()) {
            this.markedTarget.addToTeam(this.getShadow().markedTeam, CancelPredicates.cancelOnLostAbility(this).or(CancelPredicates.IS_DAY));

            this.markedTarget.scheduleUntil(
                (player) -> {
                    player.setGlowing(true);
                    player.getDataTracker().set(
                        Entity.FLAGS,
                        (byte) (player.getDataTracker().get(Entity.FLAGS) |
                            (1 << Entity.GLOWING_FLAG_INDEX)),
                        true
                    );
                },
                CancelPredicates.cancelOnLostAbility(this).or(CancelPredicates.IS_DAY)
            );
        }

        IndirectPlayer indirectTarget = this.getShadow().getIndirect(target);
        markedTarget = indirectTarget;
        glowMarkedPlayer(indirectTarget);

        this.player.sendMessageNow(TextUtil.green(markedTarget.getName().getString()).append(" was successfully marked."));
        usedToday = true;
        return AbilityResult.CLOSE;
    }

    @Override
    public void onJoin() {
        super.onJoin();
        if (markedTarget != null) {
            this.player.getPlayerOrThrow().networkHandler.sendPacket(
                TeamS2CPacket.changePlayerTeam(
                    Objects.requireNonNull(
                        getShadow()
                            .getServer()
                            .getScoreboard()
                            .getTeam("DuskMarked")
                    ),
                    markedTarget.getName().getString(),
                    TeamS2CPacket.Operation.ADD
                )
            );
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (targetKilled && this.player.getPlayer().isPresent() && this.player.getPlayerOrThrow().getStatusEffect(StatusEffects.STRENGTH) != null && this.player.getPlayerOrThrow().getStatusEffect(StatusEffects.STRENGTH).getAmplifier() == 0) {
            this.player.giveEffectNow(
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
