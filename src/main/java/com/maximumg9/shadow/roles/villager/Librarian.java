package com.maximumg9.shadow.roles.villager;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.villager.LibrarianInfo;
import com.maximumg9.shadow.roles.RoleFactory;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.roles.SubFaction;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Librarian extends AbstractVillager {
    public static final RoleFactory<Librarian> FACTORY = new Factory();
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(LibrarianInfo::new);
    public static final Style STYLE = Style.EMPTY.withColor(
        ColorHelper.Argb.getArgb(
            144,
            85,
            16
        ));
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = new ItemStack(Items.BOOK);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            new Librarian(null).getName()
        );
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
    }

    public Librarian(@Nullable IndirectPlayer player) {
        super(player, ABILITY_FACTORIES);
    }
    @Override
    public SubFaction getSubFaction() { return SubFaction.VILLAGER_INFORMATION; }
    @Override
    public String getRawName() {
        return "Librarian";
    }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public Roles getRole() {
        return Roles.LIBRARIAN;
    }
    
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
    
    private static class Factory implements RoleFactory<Librarian> {
        @Override
        public Librarian makeRole(@Nullable IndirectPlayer player) {
            return new Librarian(player);
        }
    }
}
