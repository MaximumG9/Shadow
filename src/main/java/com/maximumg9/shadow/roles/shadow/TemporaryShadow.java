package com.maximumg9.shadow.roles.shadow;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.Cull;
import com.maximumg9.shadow.abilities.PoseidonsTrident;
import com.maximumg9.shadow.abilities.RoleSelect;
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

public class TemporaryShadow extends AbstractShadow {
    public static final RoleFactory<TemporaryShadow> FACTORY = new Factory();
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.RED);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.NETHERITE_SWORD);
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(RoleSelect::new);

    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new TemporaryShadow(null).getName());
    }

    public TemporaryShadow(@Nullable IndirectPlayer player) {
        super(player, ABILITY_FACTORIES);
    }
    @Override
    public SubFaction getSubFaction() { return SubFaction.SHADOW; }
    @Override
    public String getRawName() { return "Temporary Shadow"; }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public Roles getRole() {
        return Roles.TEMP_SHADOW;
    }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) { return ITEM_STACK.copy(); }

    private static class Factory implements RoleFactory<TemporaryShadow> {
        @Override
        public TemporaryShadow makeRole(@Nullable IndirectPlayer player) {
            return new TemporaryShadow(player);
        }
    }
}
