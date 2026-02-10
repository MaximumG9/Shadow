package com.maximumg9.shadow.roles.villager;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.GetHeart;
import com.maximumg9.shadow.abilities.LifeShield;
import com.maximumg9.shadow.roles.RoleFactory;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.roles.SubFaction;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LifeweaverRework extends AbstractVillager {
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(
        LifeShield::new
    );

    public static final Style STYLE = Style.EMPTY.withColor(0x0ae8fcff);

    LifeweaverRework(IndirectPlayer player) {
        super(player, ABILITY_FACTORIES);
    }

    @Override
    public SubFaction getSubFaction() {
        return SubFaction.VILLAGER_SUPPORT;
    }

    @Override
    public String getRawName() {
        return "Lifeweaver (Rework)";
    }

    @Override
    public Style getStyle() {
        return STYLE;
    }

    @Override
    public Roles getRole() {
        return Roles.LIFEWEAVER_REWORK;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    public static final RoleFactory<LifeweaverRework> FACTORY = new Factory();
    private static class Factory implements RoleFactory<LifeweaverRework> {
        @Override
        public LifeweaverRework makeRole(@Nullable IndirectPlayer player) {
            return new LifeweaverRework(player);
        }
    }

    private static final ItemStack ITEM_STACK = new ItemStack(Items.TOTEM_OF_UNDYING);
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new LifeweaverRework(null).getName());
    }
}
