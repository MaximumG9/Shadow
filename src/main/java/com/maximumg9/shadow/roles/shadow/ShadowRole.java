package com.maximumg9.shadow.roles.shadow;

import com.maximumg9.shadow.abilities.Cull;
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

public class ShadowRole extends AbstractShadow {
    public static final RoleFactory<ShadowRole> FACTORY = new Factory();
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.RED);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.NETHERITE_SWORD);
    
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new ShadowRole(null).getName());
    }
    
    public ShadowRole(@Nullable IndirectPlayer player) {
        super(player, List.of(Cull::new));
    }
    @Override
    public SubFaction getSubFaction() { return SubFaction.SHADOW; }
    @Override
    public String getRawName() { return "Shadow"; }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public Roles getRole() {
        return Roles.SHADOW;
    }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) { return ITEM_STACK.copy(); }
    
    private static class Factory implements RoleFactory<ShadowRole> {
        @Override
        public ShadowRole makeRole(@Nullable IndirectPlayer player) {
            return new ShadowRole(player);
        }
    }
}
