package com.maximumg9.shadow.roles.villager;

import com.maximumg9.shadow.abilities.Paranoia;
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

public class Paranoid extends AbstractVillager {
    public static final RoleFactory<Paranoid> FACTORY = new Paranoid.Factory();
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.DARK_BLUE);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.RECOVERY_COMPASS);

    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Paranoid(null).getName());
    }

    public Paranoid(@Nullable IndirectPlayer player) {
        super(player, List.of(Paranoia::new));
    }
    @Override
    public SubFaction getSubFaction() {
        return SubFaction.VILLAGER_INFORMATION;
    }

    @Override
    public String getRawName() {
        return "Paranoid";
    }

    @Override
    public Style getStyle() {
        return STYLE;
    }

    @Override
    public Roles getRole() {
        return Roles.PARANOID;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    private static class Factory implements RoleFactory<Paranoid> {
        @Override
        public Paranoid makeRole(@Nullable IndirectPlayer player) {
            return new Paranoid(player);
        }
    }
}
