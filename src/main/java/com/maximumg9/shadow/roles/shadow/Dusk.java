package com.maximumg9.shadow.roles.shadow;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.BloodMoon;
import com.maximumg9.shadow.abilities.MoonlitMark;
import com.maximumg9.shadow.abilities.SunCurse;
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

public class Dusk extends AbstractShadow {
    public static final RoleFactory<Dusk> FACTORY = new Dusk.Factory();
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.BLACK);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.BLACK_DYE);
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(MoonlitMark::new, BloodMoon::new, SunCurse::new);

    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Dusk(null).getName());
    }

    public Dusk(@Nullable IndirectPlayer player) {
        super(player, ABILITY_FACTORIES);
    }
    @Override
    public SubFaction getSubFaction() {
        return SubFaction.SHADOW;
    }

    @Override
    public String getRawName() {
        return "Dusk";
    }

    @Override
    public Style getStyle() {
        return STYLE;
    }

    @Override
    public Roles getRole() {
        return Roles.DUSK;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    private static class Factory implements RoleFactory<Dusk> {
        @Override
        public Dusk makeRole(@Nullable IndirectPlayer player) {
            return new Dusk(player);
        }
    }
}
