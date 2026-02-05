package com.maximumg9.shadow.roles.shadow;

import com.maximumg9.shadow.abilities.Haunt;
import com.maximumg9.shadow.abilities.NoStrength;
import com.maximumg9.shadow.roles.RoleFactory;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.roles.SubFaction;
import com.maximumg9.shadow.roles.villager.Paranoid;
import com.maximumg9.shadow.roles.villager.Villager;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Spectre extends AbstractShadow {
    public static final RoleFactory<Spectre> FACTORY = new Spectre.Factory();
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.DARK_GRAY);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.GRAY_STAINED_GLASS);

    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Spectre(null).getName());
    }

    public Spectre(@Nullable IndirectPlayer player) {
        super(player, List.of(Haunt::new, NoStrength::new));
    }
    @Override
    public SubFaction getSubFaction() {
        return SubFaction.SHADOW;
    }

    @Override
    public String getRawName() {
        return "Spectre";
    }

    @Override
    public Style getStyle() {
        return STYLE;
    }

    @Override
    public Roles getRole() {
        return Roles.SPECTRE;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    private static class Factory implements RoleFactory<Spectre> {
        @Override
        public Spectre makeRole(@Nullable IndirectPlayer player) {
            return new Spectre(player);
        }
    }
}
