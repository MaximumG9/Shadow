package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.SheriffBow;
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

public class Sheriff extends AbstractVillager {
    public static final RoleFactory<Sheriff> FACTORY = new Sheriff.Factory();
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(SheriffBow::new);
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.GOLD);
    private static final ItemStack ITEM_STACK;
    
    static {
        ITEM_STACK = new ItemStack(Items.BOW);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            new Sheriff(null).getName()
        );
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
    }
    
    public Sheriff(@Nullable IndirectPlayer player) {
        super(player, ABILITY_FACTORIES);
    }
    @Override
    public SubFaction getSubFaction() { return SubFaction.VILLAGER_KILLING; }
    @Override
    public String getRawName() {
        return "Sheriff";
    }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public Roles getRole() {
        return Roles.SHERIFF;
    }
    
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
    
    private static class Factory implements RoleFactory<Sheriff> {
        @Override
        public Sheriff makeRole(@Nullable IndirectPlayer player) {
            return new Sheriff(player);
        }
    }
}
