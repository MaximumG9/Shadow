package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleSelect extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("role_select");
    private static final ItemStack ITEM_STACK;
    private final Set<Role> POTENTIAL_ROLES = new HashSet<>();
    private boolean roleTaken = false;

    static {
        ITEM_STACK = new ItemStack(Items.ENDER_PEARL, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("During Grace Period, select your role."),
                TextUtil.gray("At the end of grace, you get the role you selected."),
                AbilityText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.gold("Role Selector")
        );
    }

    public boolean notTaken() {
        return !roleTaken;
    }

    public boolean takeRole(Role role) {
        if(!POTENTIAL_ROLES.contains(role)) return false;
        POTENTIAL_ROLES.remove(role);
        roleTaken = true;
        return true;
    }

    public RoleSelect(IndirectPlayer player) {
        super(player);
        getShadow().getAllLivingPlayers()
            .filter(p -> p.role.getFaction() == this.player.role.getFaction())
            .map(p -> p.role)
            .forEach(POTENTIAL_ROLES::add);
    }
    @Override
    public void deInit() {
        POTENTIAL_ROLES.remove(this.player.role);
        super.deInit();
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

                    //if(SELECTABLE_ROLES.stream()
                    //    .map(r -> (RoleSelect) r.getAbility(ID).get())
                    //    .allMatch(r -> !r.takeRole(target))) {
                    //     actor.sendMessage(TextUtil.red("Role already taken."));
                    //     return;
                    //}

                    if (!((RoleSelect) target.getAbility(ID).get()).takeRole(target)) {
                        actor.sendMessage(TextUtil.red("Role already taken."));
                        return;
                    }

                    this.player.role = target.getRole().factory.makeRole(this.player);
                },
                POTENTIAL_ROLES.stream()
                    .filter(r -> ((RoleSelect) r.getAbility(ID).get()).notTaken())
                    .toList()
            )
        );
        return AbilityResult.NO_CLOSE;
    }

    @Override
    public void tick() {
        if (!getShadow().isGracePeriod()) {
            List<Role> availableRoles = POTENTIAL_ROLES.stream()
                .filter(r -> ((RoleSelect) r.getAbility(ID).get()).notTaken())
                .toList();
            Role targetRole = availableRoles.get(Random.createLocal().nextBetween(0, availableRoles.size()-1));

            ((RoleSelect) targetRole.getAbility(ID).get()).takeRole(targetRole);
            this.player.role = targetRole.getRole().factory.makeRole(this.player);
        }
    }
}
