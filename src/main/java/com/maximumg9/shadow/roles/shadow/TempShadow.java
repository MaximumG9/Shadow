package com.maximumg9.shadow.roles.shadow;

import com.maximumg9.shadow.abilities.PickRole;
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

public class TempShadow extends AbstractShadow {
    public static final RoleFactory<TempShadow> FACTORY = new TempShadow.Factory();
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.RED);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.NETHERITE_SWORD);

    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new TempShadow(null).getName());
    }

    public TempShadow(@Nullable IndirectPlayer player) {
        super(player, List.of(PickRole::new));
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

    private static class Factory implements RoleFactory<TempShadow> {
        @Override
        public TempShadow makeRole(@Nullable IndirectPlayer player) {
            return new TempShadow(player);
        }
    }
}
