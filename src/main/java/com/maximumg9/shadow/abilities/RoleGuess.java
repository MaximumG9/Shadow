package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.abilities.filters.Filters;
import com.maximumg9.shadow.abilities.shadow.ToggleStrength;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.CancellableDelay;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public class RoleGuess extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("role_guess");
    private static final ItemStack ITEM_STACK;
    private static final Text NAME = TextUtil.withColour("Role Guess",Formatting.DARK_PURPLE);
    private static final Identifier HP_ATTR_ID = MiscUtil.shadowID("guess_role_max_health");
    private static final double healthIncrease = -6;
    
    private static final int COOLDOWN_TIME = 8 * 60 * 20;
    
    static {
        ITEM_STACK = new ItemStack(Items.WRITABLE_BOOK, 1);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            NAME
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("Guess the role of a player to kill them."),
                TextUtil.gray("This ability cannot be used if the"),
                TextUtil.gray("number of non-shadows alive is less than"),
                TextUtil.gray("or equal to the number of shadows alive."),
                TextUtil.blue("⌛ 8 minute cooldown"),
                TextUtil.red("IF YOU GUESS INCORRECTLY, YOU LOSE STRENGTH FOR 8 MINUTES.")
                    .styled(style -> style.withBold(true)),
                TextUtil.red("ADDITIONALLY, YOU WILL LOSE ").append(TextUtil.hearts(6))
                    .styled(style -> style.withBold(true)),
                AbilityText()
            )
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }
    
    private final List<Roles> unguessableRoles;
    private final List<Faction> unguessableFactions;
    
    public RoleGuess(IndirectPlayer player) {
        super(player);
        this.unguessableRoles = List.of(player.role.getRole());
        this.unguessableFactions = List.of(Faction.SPECTATOR);
    }
    
    public RoleGuess(IndirectPlayer player, List<Roles> unguessableRoles, List<Faction> unguessableFactions) {
        super(player);
        this.unguessableRoles = unguessableRoles;
        this.unguessableFactions = unguessableFactions;
    }
    
    public List<Filter> getFilters() {
        return List.of(
            new Filters.NotGracePeriod(),
            new Filters.Cooldown(RoleGuess.COOLDOWN_TIME),
            new Filters.NonShadowsGreaterThanShadows(
                "You cannot guess when the number of shadows alive meets or exceeds the number of non-shadows alive."
            )
        );
    }
    
    @Override
    public AbilityResult apply() {
        this.player.getPlayerOrThrow().openHandledScreen(
            new DecisionScreenHandler.Factory<>(
                Text.literal("Person to guess"),
                (target, actor, _a, _b) -> {
                    if (target == null) {
                        actor.sendMessage(TextUtil.red("Failed to select player to guess"));
                        return;
                    }
                    actor.openHandledScreen(
                        new DecisionScreenHandler.Factory<>(
                            Text.literal("Role to guess"),
                            (guessedRole, pl, __a, __b) -> {
                                if (guessedRole == null) {
                                    pl.sendMessage(TextUtil.red("Failed to select role to guess"));
                                    return;
                                }

                                this.resetLastActivated();
                                
                                if (guessedRole.getRole() == target.role.getRole()) {
                                    pl.sendMessage(TextUtil.green("You successfully guessed your target's role."));

                                    target.damage(
                                        pl.getServerWorld()
                                            .getDamageSources()
                                            .magic(),
                                        Float.MAX_VALUE,
                                        CancelPredicates.cancelOnPhaseChange(
                                            this.getShadow().state.phase
                                        )
                                    );
                                } else {
                                    pl.sendMessage(TextUtil.red("You guessed your target's role incorrectly!"));
                                    if (pl.getMaxHealth() <= 6) {
                                        this.player.damageOrThrow(
                                            pl.getServerWorld()
                                                .getDamageSources()
                                                .magic(),
                                            Float.MAX_VALUE
                                        );
                                    } else if (this.player.role.hasAbility(ToggleStrength.ID)) {
                                        this.player.role.removeAbility(this.player.role.getAbility(ToggleStrength.ID).get());
                                        EntityAttributeInstance instance = pl.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                                        if (instance != null) {

                                            EntityAttributeModifier healthModifier = instance.getModifier(HP_ATTR_ID);
                                            if (healthModifier == null) {
                                                instance.addPersistentModifier(
                                                    new EntityAttributeModifier(
                                                        HP_ATTR_ID,
                                                        healthIncrease,
                                                        EntityAttributeModifier.Operation.ADD_VALUE
                                                    )
                                                );
                                            } else {
                                                if (healthModifier.operation() != EntityAttributeModifier.Operation.ADD_VALUE) {
                                                    MiscUtil.getShadow(this.player.getShadow().getServer()).ERROR("Guess Role Operation isn't ADD_VALUE (how did you MANAGE this)");
                                                    return;
                                                }
                                                instance.overwritePersistentModifier(
                                                    new EntityAttributeModifier(
                                                        HP_ATTR_ID,
                                                        healthIncrease + healthModifier.value(),
                                                        EntityAttributeModifier.Operation.ADD_VALUE
                                                    )
                                                );
                                            }
                                        }
                                        CancellableDelay.of(
                                            () -> {
                                                this.player.role.addAbility(ToggleStrength::new);
                                            },
                                            COOLDOWN_TIME,
                                            CancellableDelay.wrapCancelCondition(CancelPredicates.cancelOnLostRole(this.player.role), this.player)
                                        );
                                    }
                                }
                            },
                            Arrays.stream(Roles.values())
                                .filter(role -> !this.unguessableRoles.contains(role))
                                .<Role>map(role -> role.factory.makeRole(null))
                                .toList()
                        )
                    );
                },
                this.getShadow().indirectPlayerManager
                    .getAllPlayers()
                    .stream()
                    .filter((p) -> !unguessableFactions.contains(p.role.getFaction()))
                    .toList()
            )
        );
        return AbilityResult.NO_CLOSE;
    }
    
    @Override
    public Identifier getID() {
        return ID;
    }
    
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
}
