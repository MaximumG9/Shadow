package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.SeeEnderEyesGlow;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Looker extends AbstractVillager {
    public static final RoleFactory<Looker> FACTORY = new Factory();
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(SeeEnderEyesGlow::new);
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.DARK_GREEN);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.ENDER_EYE);
    
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Looker(null).getName());
    }
    
    public Looker(@Nullable IndirectPlayer player) {
        super(player, ABILITY_FACTORIES);
    }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) { return ITEM_STACK.copy(); }
    @Override
    public SubFaction getSubFaction() { return SubFaction.VILLAGER_SUPPORT; }
    @Override
    public String getRawName() {
        return "Looker";
    }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public Roles getRole() {
        return Roles.LOOKER;
    }
    
    private static class Factory implements RoleFactory<Looker> {
        @Override
        public Looker makeRole(@Nullable IndirectPlayer player) {
            return new Looker(player);
        }
    }
}
