package com.maximumg9.shadow.roles.shadow;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.PoseidonsTrident;
import com.maximumg9.shadow.roles.RoleFactory;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.roles.SubFaction;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PoseidonsWrath extends AbstractShadow {
    public static final RoleFactory<PoseidonsWrath> FACTORY = new Factory();
    private static final ItemStack ITEM_STACK = new ItemStack(Items.TRIDENT);
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(PoseidonsTrident::new);
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.BLUE);

    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new PoseidonsWrath(null).getName());
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }

    public PoseidonsWrath(IndirectPlayer player) {
        super(player, ABILITY_FACTORIES);
    }

    @Override
    public SubFaction getSubFaction() {
        return SubFaction.SHADOW;
    }

    @Override
    public String getRawName() {
        return "Poseidon's Wrath";
    }

    @Override
    public Style getStyle() {
        return STYLE;
    }

    @Override
    public Roles getRole() {
        return Roles.POSEIDONS_WRATH;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    private static class Factory implements RoleFactory<PoseidonsWrath> {
        @Override
        public PoseidonsWrath makeRole(@Nullable IndirectPlayer player) {
            return new PoseidonsWrath(player);
        }
    }
}
