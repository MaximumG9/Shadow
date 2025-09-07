package com.maximumg9.shadow.roles.shadow;

import com.maximumg9.shadow.abilities.RoleGuess;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.RoleFactory;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.roles.SubFaction;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Oracle extends AbstractShadow {
    public static final RoleFactory<Oracle> FACTORY = new Factory();
    private static final List<Roles> UNGUESSABLE_ROLES = List.of(Roles.VILLAGER);
    private static final List<Faction> UNGUESSABLE_FACTIONS = List.of(Faction.SHADOW, Faction.SPECTATOR);
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.DARK_PURPLE);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.WRITABLE_BOOK);
    
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Oracle(null).getName());
    }
    public Oracle(@Nullable IndirectPlayer player) {
        super(player, List.of((p) -> new RoleGuess(p, UNGUESSABLE_ROLES, UNGUESSABLE_FACTIONS)));
    }
    @Override
    public SubFaction getSubFaction() { return SubFaction.SHADOW; }
    @Override
    public String aOrAn() { return "an"; }
    @Override
    public String getRawName() { return "Oracle"; }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public Roles getRole() {
        return Roles.ORACLE;
    }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) { return ITEM_STACK.copy(); }
    
    private static class Factory implements RoleFactory<Oracle> {
        @Override
        public Oracle makeRole(@Nullable IndirectPlayer player) {
            return new Oracle(player);
        }
    }
}
