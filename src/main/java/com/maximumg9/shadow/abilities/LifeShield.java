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
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class LifeShield extends Ability {
    public static final Identifier ATTR_ID = MiscUtil.shadowID("lifeweaver_rework_max_health");
    public static final Identifier ID = MiscUtil.shadowID("life_shield");
    private static final ItemStack ITEM_STACK;
    private Set<IndirectPlayer> shieldedPlayers = new HashSet<>();

    private final List<Filter> FILTERS = List.of(
        new Filters.RequiredMaxHealth(10f,"You don't have enough health to shield another player!")
    );

    static {
        ITEM_STACK = new ItemStack(Items.TOTEM_OF_UNDYING, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("Shield a selected player from death once, at the cost of ")
                    .append(TextUtil.hearts(5)),
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

    public boolean isPlayerShielded(IndirectPlayer player) {
        if (shieldedPlayers.contains(player)) {
            this.player.spoofAddPlayersToTeam(List.of(player),getShadow().playerTeam, CancelPredicates.cancelOnLostAbility(this));
            shieldedPlayers.remove(player);
            return true;
        }
        return false;
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public void onDay() {
        getShadow().addTickable(Delay.instant(() -> colorShieldedPlayers(shieldedPlayers.stream().toList())));
    }

    public void onJoin() {
        colorShieldedPlayers(shieldedPlayers.stream().toList());
    }

    @Override
    public AbilityResult apply() {
        this.player.getPlayerOrThrow().openHandledScreen(
            new DecisionScreenHandler.Factory<>(
                Text.literal("Person to shield"),
                (target, actor, _a, _b) -> {
                    if (target == null) {
                        actor.sendMessage(TextUtil.red("Failed to select player to guess"));
                        return;
                    }

                    this.shieldedPlayers.add(target);

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
                            .append(TextUtil.hearts(actor.getMaxHealth() / 2))
                            .append(TextUtil.green("."))
                    );
                    colorShieldedPlayers(shieldedPlayers.stream().toList());
                },
            this.getShadow()
                .getAllLivingPlayers()
                .filter(player ->
                    player != this.player
                    && !this.shieldedPlayers.contains(player))
                .toList()
            )
        );
        return AbilityResult.NO_CLOSE;
    }
}
