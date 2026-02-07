package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.LifeweaverHeart;
import com.maximumg9.shadow.util.*;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Paranoia extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("paranoia");
    private static final ItemStack ITEM_STACK;
    private static final List<Identifier> PASSIVE_TRIGGER = List.of(Paranoia.ID, SeeEnderEyesGlow.ID, NoStrength.ID);
    private static final List<Identifier> ITEM_TRIGGER    = List.of(LifeweaverHeart.ID, SheriffBow.ID, PoseidonsTrident.ID);
    private static final List<Identifier> NIGHT_TRIGGER   = List.of(SeeGlowing.ID, BloodMoon.ID);
    private static final List<Identifier> DAY_TRIGGER     = List.of(SunCurse.ID);
    private static final List<Identifier> TOGGLE_TRIGGER  = List.of(ToggleStrength.ID, SunCurse.ID, Haunt.ID);
    private static Set<IndirectPlayer> suspiciousPlayers  = new HashSet<>();
    private Set<IndirectPlayer> playersToPing = new HashSet<>();
    private boolean currentPing = false;
    private int pingDelay = 20 * 5 * 60;

    static {
        ITEM_STACK = new ItemStack(Items.RECOVERY_COMPASS, 1);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.blue("Paranoia")
        );
    }

    private void ping() {
        this.player.getShadow().addTickable(
            ConditionalDelay.of(() -> {
                currentPing = !playersToPing.isEmpty();
                if (currentPing) {
                    this.player.sendOverlay(
                        TextUtil.green("Ability seen in last ")
                            .append(String.valueOf(pingDelay/(20*60)))
                            .append(" minutes."),
                        CancelPredicates.cancelOnLostAbility(this)
                    );
                    this.player.sendMessage(
                        TextUtil.green("Ability seen in last ")
                            .append(String.valueOf(pingDelay/(20*60)))
                            .append(" minutes."),
                        CancelPredicates.cancelOnLostAbility(this)
                    );
                    this.player.playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 1, 1, CancelPredicates.cancelOnLostAbility(this));
                } else {
                    this.player.sendOverlay(
                        TextUtil.red("No ability seen in last ")
                            .append(String.valueOf(pingDelay/(20*60)))
                            .append(" minutes."),
                        CancelPredicates.cancelOnLostAbility(this)
                    );
                    this.player.playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 1, 0, CancelPredicates.cancelOnLostAbility(this));
                    this.player.sendMessage(
                        TextUtil.red("No ability seen in last ")
                            .append(String.valueOf(pingDelay/(20*60)))
                            .append(" minutes."),
                        CancelPredicates.cancelOnLostAbility(this)
                    );
                }

                pingDelay = (int) (20 * 3 * 60 + Math.random() * 20 * 2 * 60);
                playersToPing = new HashSet<>();
                ping();
            },
            pingDelay,
            ConditionalDelay.wrapCancelCondition(
                CancelPredicates.cancelOnLostAbility(this),
                this.player
            )
        ));
    }


    public Paranoia(IndirectPlayer player) {
        super(player);
    }


    @Override
    public void init() {
        ping();
        super.init();
    }

    @Override
    public void onDay() {
        suspiciousPlayers = new HashSet<>();
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ItemStack pingStack = ITEM_STACK.copy();
        pingStack.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("Every 3-5 minutes, learn if you encountered an Active ability (within ")
                    .append(String.valueOf(getShadow().config.fearRadius))
                    .append(" blocks)."),
                TextUtil.gray("Last Ping ")
                    .append(currentPing ? TextUtil.green("[HIT]") : TextUtil.red("[MISSED]"))
            )
        );
        return pingStack.copy();
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public AbilityResult apply() { return AbilityResult.NO_CLOSE; }

    @Override
    public void tick() {
        getShadow().getAllLivingPlayers()
            .filter(p -> p.role.hasAbility(MoonlitMark.ID))
            .flatMap(
                (p) -> p.role.getAbility(MoonlitMark.ID)
                    .flatMap(a -> ((MoonlitMark) a).getMarkedTarget())
                    .stream()
            ).forEach(
                p -> suspiciousPlayers.add(p)
            );

        Optional<ServerPlayerEntity> possiblePlayer = this.player.getPlayer();
        possiblePlayer.ifPresent( p ->
            p.getServerWorld().getPlayers(
                (player) -> {
                    IndirectPlayer indirect = getShadow().getIndirect(player);
                    return player.squaredDistanceTo(p) <= this.player.getShadow().config.fearRadius * this.player.getShadow().config.fearRadius
                        && currentlyPassivelyTriggers(indirect) && !player.equals(p);
                }
            ).stream()
            .map((serverPlayer) -> getShadow().getIndirect(serverPlayer))
            .forEach(this.playersToPing::add)
        );
    }

    public void addPing(IndirectPlayer p) {
        this.playersToPing.add(p);
    }

    private boolean currentlyPassivelyTriggers(IndirectPlayer other) {
        return other.role.hasAnyOfAbilities(PASSIVE_TRIGGER)
            || suspiciousPlayers.contains(other)
            || (NBTUtil.getID(other.getPlayerOrThrow().getMainHandStack()) != null && ITEM_TRIGGER.contains(NBTUtil.getID(other.getPlayerOrThrow().getMainHandStack())))
            || (other.getShadow().isNight() && other.role.hasAnyOfAbilities(NIGHT_TRIGGER))
            || (!other.getShadow().isNight() && other.role.hasAnyOfAbilities(DAY_TRIGGER))
            || (other.role.hasAnyOfAbilities(TOGGLE_TRIGGER) && other.role.getAbilities()
                .stream()
                .filter(ability -> TOGGLE_TRIGGER.contains(ability.getID()))
                .anyMatch(Ability::getToggled));
    }
}
