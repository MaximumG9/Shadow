package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.abilities.filters.Filters;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class LifeShield extends Ability {
    public static final Identifier ATTR_ID = MiscUtil.shadowID("lifeweaver_rework_max_health");
    public static final Identifier ID = MiscUtil.shadowID("life_shield");
    private static final ItemStack ITEM_STACK;
    private IndirectPlayer shieldedPlayer = null;

    private static final int COOLDOWN_TIME = 5 * 60 * 20;

    private final List<Filter> FILTERS = List.of(
        new Filters.Cooldown(COOLDOWN_TIME)
    );

    static {
        ITEM_STACK = new ItemStack(Items.TOTEM_OF_UNDYING, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("Shield a selected player from death once, at the cost of ")
                    .append(TextUtil.hearts(5)),
                TextUtil.red("If you die, your shield is removed."),
                AbilityText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.blue("Life Shield")
        );
    }

    private void colorShieldedPlayers(List<IndirectPlayer> p) {
        this.player.spoofAddPlayersToTeamNow(p, getShadow().shieldedTeam);
    }

    public LifeShield(IndirectPlayer player) { super(player); }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    public List<Filter> getFilters() {
        return FILTERS;
    }

    public Optional<IndirectPlayer> getShieldedPlayer () {
        return Optional.ofNullable(shieldedPlayer);
    }

    public boolean isPlayerShielded(IndirectPlayer player, IndirectPlayer attacker) {
        if (shieldedPlayer == player) {
            this.player.sendMessage(TextUtil.withColour(
                shieldedPlayer.getName().getString(), Formatting.WHITE)
                    .append(" has lost their ")
                    .append(TextUtil.blue("shield"))
                    .append(TextUtil.withColour("!",Formatting.WHITE))
                ,CancelPredicates.cancelOnLostAbility(this));
            // this.player.sendMessage(
            //     TextUtil.red("Using your ability again will kill you if you have less than 5 max hearts.")
            //     ,CancelPredicates.cancelOnLostAbility(this));
            this.player.spoofAddPlayersToTeam(List.of(player),getShadow().playerTeam, CancelPredicates.cancelOnLostAbility(this));
            shieldedPlayer = null;
            return attacker != this.player;
        }
        return false;
    }

    @Override
    public void deInit() {
        if (shieldedPlayer != null) this.player.spoofAddPlayersToTeam(List.of(shieldedPlayer),getShadow().playerTeam, CancelPredicates.NEVER_CANCEL);
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public void onDay() {
        if (this.player.getPlayer().isPresent() && shieldedPlayer != null) getShadow().addTickable(Delay.instant(() -> colorShieldedPlayers(List.of(shieldedPlayer))));
    }

    public void onJoin() {
        if (shieldedPlayer != null
            && (!getShadow().isNight()
            || getShadow().getAllLivingPlayers()
                .filter(p -> p.role.hasAbility(MoonlitMark.ID))
                .flatMap(
                    (p) -> p.role.getAbility(MoonlitMark.ID)
                        .flatMap(a -> ((MoonlitMark) a).getMarkedTarget())
                        .stream()
                ).anyMatch((p) -> p != shieldedPlayer))
        ) colorShieldedPlayers(List.of(shieldedPlayer));
    }

    public void onNight() {
        if (shieldedPlayer != null && player.getPlayer().isPresent()
            && (!getShadow().isNight()
            || getShadow().getAllLivingPlayers()
            .filter(p -> p.role.hasAbility(MoonlitMark.ID))
            .flatMap(
                (p) -> p.role.getAbility(MoonlitMark.ID)
                    .flatMap(a -> ((MoonlitMark) a).getMarkedTarget())
                    .stream()
            ).anyMatch((p) -> p != shieldedPlayer))
        ) this.player.spoofAddPlayersToTeam(List.of(shieldedPlayer),
            getShadow().playerTeam,
            CancelPredicates.cancelOnLostRole(shieldedPlayer.role)
                .or(CancelPredicates.IS_DAY)
        );
    }

    @Override
    public AbilityResult apply() {
        ServerPlayerEntity player = this.player.getPlayerOrThrow();
        player.openHandledScreen(
            new DecisionScreenHandler.Factory<>(
                Text.literal("Person to shield"),
                (target, actor, _a, _b) -> {
                    if (target == null) {
                        actor.sendMessage(TextUtil.red("Failed to select player to shield"));
                        return;
                    }

                    if (shieldedPlayer == null) {
                        if (player.getMaxHealth() - 10 <= 0) {
                            actor.sendMessage(TextUtil.red("Cannot shield new player when you have less than ")
                                .append(TextUtil.hearts(5)));
                            return;
                        }

                        EntityAttributeInstance attr = actor
                            .getAttributes()
                            .getCustomInstance(
                                EntityAttributes.GENERIC_MAX_HEALTH
                            );

                        if (attr == null) {
                            getShadow().ERROR("Players don't have a max health attribute (we're so cooked).");
                            return;
                        }

                        EntityAttributeModifier modifier = attr.getModifier(ATTR_ID);
                        if (modifier == null) {
                            attr.addPersistentModifier(new EntityAttributeModifier(
                                ATTR_ID,
                                -10.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                            ));

                        } else {
                            double oldValue = modifier.value();
                            if (
                                modifier.operation() !=
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ) {
                                getShadow().ERROR("Existing Lifeweaver Rework attribute modifier is not add value");
                            }
                            attr.overwritePersistentModifier(new EntityAttributeModifier(
                                ATTR_ID,
                                oldValue - 10.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                            ));
                        }

                        this.player.sendMessageNow(
                            TextUtil.green(target.getName().getString())
                                .append(" was shielded. Your max health is now ")
                                .append(TextUtil.hearts((float) Math.floor(actor.getMaxHealth() / 2)))
                                .append(TextUtil.green("."))
                        );
                    } else {
                        this.player.spoofAddPlayersToTeamNow(List.of(shieldedPlayer), getShadow().playerTeam);
                        this.player.sendMessageNow(
                            TextUtil.green("Changed shield target from ")
                                .append(shieldedPlayer.getName().getString())
                                .append(" to ")
                                .append(target.getName().getString())
                                .append(".")
                        );
                    }
                    this.resetLastActivated();
                    shieldedPlayer = target;
                    colorShieldedPlayers(List.of(shieldedPlayer));
                },
            this.getShadow()
                .getAllLivingPlayers()
                .filter(p ->
                    p != this.player
                    && shieldedPlayer != p)
                .toList()
            )
        );
        return AbilityResult.NO_CLOSE;
    }
}
