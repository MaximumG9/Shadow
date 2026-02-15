package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleSelect extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("role_select");
    private static final ItemStack ITEM_STACK;
    private static final Set<Role> SELECTABLE_ROLES = new HashSet<>();

    static {
        ITEM_STACK = new ItemStack(Items.TARGET, 1);
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

    public boolean takeRole(Role role) {
        if(!SELECTABLE_ROLES.contains(role)) return false;
        SELECTABLE_ROLES.remove(role);
        return true;
    }

    public RoleSelect(IndirectPlayer player) {
        super(player);
        //getShadow().getAllLivingPlayers()
        //    .filter(p -> p.role.getFaction() == this.player.role.getFaction())
        //    .map(p -> p.role)
        //    .forEach(SELECTABLE_ROLES::add);
        SELECTABLE_ROLES.add(player.role);
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

                    if (!takeRole(target)) {
                        actor.sendMessage(TextUtil.red("Role already taken."));
                        //     return;
                    }

                    this.player.role = target;
                    player.role.removeAbility(player.role.getAbility(ID).get()); //trust me bro
                },
                SELECTABLE_ROLES.stream().toList()
            )
        );
        return null;
    }
}
