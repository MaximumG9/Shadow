package com.maximumg9.shadow.roles.villager;

import com.maximumg9.shadow.abilities.villager.SendLoveLetter;
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

public class Lover extends AbstractVillager {
    public Lover(@Nullable IndirectPlayer player) {
        super(player, List.of(SendLoveLetter::new));
    }

    @Override
    public SubFaction getSubFaction() {
        return SubFaction.VILLAGER_INFORMATION;
    }

    @Override
    public String getRawName() {
        return "Lover";
    }

    @Override
    public Style getStyle() {
        return Style.EMPTY.withColor(Formatting.RED);
    }

    @Override
    public Roles getRole() {
        return Roles.LOVER;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    public static final RoleFactory<Lover> FACTORY = new Lover.Factory();
    private static class Factory implements RoleFactory<Lover> {
        @Override
        public Lover makeRole(@Nullable IndirectPlayer player) {
            return new Lover(player);
        }
    }

    private static final ItemStack ITEM_STACK = new ItemStack(Items.POPPY);
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Lover(null).getName());
    }
}
