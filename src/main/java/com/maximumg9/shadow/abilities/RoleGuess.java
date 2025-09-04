package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.abilities.filters.Filters;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public class RoleGuess extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("role_guess");
    private static final ItemStack ITEM_STACK;
    private static final Text NAME = Text.literal("Role Guess")
        .styled(style -> style.withColor(Formatting.RED));
    
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
                Text.literal("⌛ 8 minute cooldown").styled(style -> style.withColor(Formatting.BLUE)),
                Text.literal("IF YOU GUESS INCORRECTLY, YOU DIE")
                    .styled(style -> style.withColor(Formatting.RED).withBold(true)),
                Ability.AbilityText()
            )
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }
    
    private final List<Roles> unguessableRoles;
    private final List<Faction> unguessableFactions;
    
    public RoleGuess(IndirectPlayer player) {
        super(player);
        if (player.role != null) {
            this.unguessableRoles = List.of(player.role.getRole());
        } else {
            this.unguessableRoles = List.of();
        }
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
                        actor.sendMessage(
                            Text.literal("Failed to select player to guess")
                                .styled(style -> style.withColor(Formatting.RED))
                        );
                        return;
                    }
                    actor.openHandledScreen(
                        new DecisionScreenHandler.Factory<>(
                            Text.literal("Role to guess"),
                            (guessedRole, pl, __a, __b) -> {
                                if (guessedRole == null) {
                                    pl.sendMessage(TextUtil.error("Failed to select role to guess"));
                                    return;
                                }
                                if (target.role == null) {
                                    pl.sendMessage(TextUtil.error("That person's role is null."));
                                    return;
                                }
                                
                                this.resetLastActivated();
                                
                                if (guessedRole.getRole() == target.role.getRole()) {
                                    pl.sendMessage(TextUtil.success("You successfully guessed your target's role."));
                                    
                                    RegistryEntry<DamageType> magicDamage = target
                                        .getShadow()
                                        .getServer()
                                        .getRegistryManager()
                                        .get(RegistryKeys.DAMAGE_TYPE)
                                        .entryOf(DamageTypes.MAGIC);
                                    target.damage(
                                        new DamageSource(magicDamage),
                                        Float.MAX_VALUE,
                                        CancelPredicates.cancelOnPhaseChange(
                                            this.getShadow().state.phase
                                        )
                                    );
                                } else {
                                    pl.sendMessage(TextUtil.error("You guessed your target's role incorrectly!"));
                                    
                                    RegistryEntry<DamageType> magicDamage = target
                                        .getShadow()
                                        .getServer()
                                        .getRegistryManager()
                                        .get(RegistryKeys.DAMAGE_TYPE)
                                        .entryOf(DamageTypes.MAGIC);
                                    this.player.damage(
                                        new DamageSource(magicDamage),
                                        Float.MAX_VALUE,
                                        CancelPredicates.cancelOnPhaseChange(
                                            this.getShadow().state.phase
                                        )
                                    );
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
                    .filter((p) -> p.role != null && !unguessableFactions.contains(p.role.getFaction()))
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
