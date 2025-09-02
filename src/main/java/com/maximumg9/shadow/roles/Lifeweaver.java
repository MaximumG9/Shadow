package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.GetHeart;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Lifeweaver extends AbstractVillager {
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(
        GetHeart::new
    );

    public static final Style STYLE = Style.EMPTY.withColor(0x0ae8fcff);

    Lifeweaver(IndirectPlayer player) {
        super(player, ABILITY_FACTORIES);
    }

    @Override
    public SubFaction getSubFaction() {
        return SubFaction.VILLAGER_SUPPORT;
    }

    @Override
    public String getRawName() {
        return "Lifeweaver";
    }

    @Override
    public Style getStyle() {
        return STYLE;
    }

    @Override
    public Roles getRole() {
        return Roles.LIFEWEAVER;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    public static final RoleFactory<Lifeweaver> FACTORY = new Lifeweaver.Factory();
    private static class Factory implements RoleFactory<Lifeweaver> {
        @Override
        public Lifeweaver makeRole(@Nullable IndirectPlayer player) {
            return new Lifeweaver(player);
        }
    }

    private static final ItemStack ITEM_STACK = new ItemStack(Items.GOLDEN_APPLE);
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Lifeweaver(null).getName());
        ITEM_STACK.remove(DataComponentTypes.FOOD);
    }
}
