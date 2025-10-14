package com.maximumg9.shadow.roles.villager;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.AddHealthLink;
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
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Lifelink extends AbstractVillager {
    public static final RoleFactory<Lifelink> FACTORY = new Lifelink.Factory();
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(AddHealthLink::new);
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.DARK_GREEN);
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = new ItemStack(Items.GOLDEN_CARROT);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            new Lifelink(null).getName()
        );
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
    }

    public Lifelink(@Nullable IndirectPlayer player) {
        super(player, ABILITY_FACTORIES);
    }
    @Override
    public SubFaction getSubFaction() { return SubFaction.VILLAGER_SUPPORT; }
    @Override
    public String getRawName() {
        return "Lifelink";
    }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public Roles getRole() {
        return Roles.LIFELINK;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    private static class Factory implements RoleFactory<Lifelink> {
        @Override
        public Lifelink makeRole(@Nullable IndirectPlayer player) {
            return new Lifelink(player);
        }
    }
}
