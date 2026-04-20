package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.CancellableDelay;
import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class RoleSelect extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("role_select");
    private static final ItemStack ITEM_STACK;
    private ArrayList<Role> POTENTIAL_ROLES = null;
    private Predicate<Role> AVAILABLE_CHECK;



    static {
        ITEM_STACK = new ItemStack(Items.ENDER_PEARL, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("Select a role to become from a list of roles."),
                AbilityText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.gold("Role Selector")
        );
    }

    boolean selectRole(Role role) {
        if (!POTENTIAL_ROLES.stream()
            .filter(AVAILABLE_CHECK)
            .toList()
            .contains(role)) return false;
        this.player.originalRole = role.getRole();
        this.player.role = role.getRole().factory.makeRole(player);
        return true;
    }

    public RoleSelect(IndirectPlayer player) {
        super(player);
    }

    public void setupSelecting(ArrayList<Role> potentialRoles, Predicate<Role> availablePredicate) {
        POTENTIAL_ROLES = potentialRoles;
        AVAILABLE_CHECK = availablePredicate;
    }

    public void setupSelecting(ArrayList<Role> potentialRoles, Predicate<Role> availablePredicate, int forceSelectionTimer) {
        POTENTIAL_ROLES = potentialRoles;
        AVAILABLE_CHECK = availablePredicate;

        getShadow().addTickable(
            CancellableDelay.of(
                () -> {
                    List<Role> availableRoles = POTENTIAL_ROLES.stream()
                        .filter(AVAILABLE_CHECK)
                        .toList();
                    Role targetRole = availableRoles.get(Random.createLocal().nextBetween(0, availableRoles.size()-1));
                    selectRole(targetRole);
                },
                forceSelectionTimer,
                CancellableDelay.wrapCancelCondition(CancelPredicates.cancelOnLostAbility(this), this.player)
            )

        );
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public AbilityResult apply() {
        this.player.getPlayerOrThrow().openHandledScreen(
            new DecisionScreenHandler.Factory<>(
                Text.literal("Role to select"),
                (target, actor, _a, _b) -> {
                    if (target == null) {
                        actor.sendMessage(TextUtil.red("Failed to select role"));
                        return;
                    }

                    if (!selectRole(target)) {
                        actor.sendMessage(TextUtil.red("Role not available."));
                    }
                },
                POTENTIAL_ROLES.stream()
                    .filter(AVAILABLE_CHECK)
                    .toList()
            )
        );
        return AbilityResult.NO_CLOSE;
    }
}
