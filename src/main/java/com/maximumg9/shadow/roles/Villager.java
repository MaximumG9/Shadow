package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Villager extends AbstractVillager {
    public static final RoleFactory<Villager> FACTORY = new Factory();
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.GREEN);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.EMERALD);
    
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Villager(null).getName());
    }
    
    public Villager(@Nullable IndirectPlayer player) {
        super(player, List.of());
    }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) { return ITEM_STACK.copy(); }
    @Override
    public SubFaction getSubFaction() { return SubFaction.VILLAGER_OUTLIER; }
    @Override
    public String getRawName() {
        return "Villager";
    }
    @Override
    public Roles getRole() {
        return Roles.VILLAGER;
    }
    @Override
    public Style getStyle() { return STYLE; }
    
    private static class Factory implements RoleFactory<Villager> {
        @Override
        public Villager makeRole(@Nullable IndirectPlayer player) {
            return new Villager(player);
        }
    }
}
