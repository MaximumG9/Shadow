package com.maximumg9.shadow.roles.neutral;

import com.maximumg9.shadow.abilities.neutral.PinataGift;
import com.maximumg9.shadow.abilities.neutral.PinataHit;
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

public class Pinata extends AbstractNeutral {
    public static final RoleFactory<Pinata> FACTORY = new Pinata.Factory();
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.DARK_AQUA);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.CANDLE);


    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Pinata(null).getName());
    }

    public Pinata(@Nullable IndirectPlayer player) {
        // Fuck you too :)
        super(player, player == null ? List.of(PinataGift::new) : !player.getShadow().config.pinataHittable ? List.of(PinataGift::new) : List.of(PinataGift::new, PinataHit::new));
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {return ITEM_STACK.copy(); }

    @Override
    public SubFaction getSubFaction() {
        return SubFaction.NEUTRAL_CHAOS; // Change Later
    }

    @Override
    public String getRawName() { return "Piñata"; } //ñ

    @Override
    public Style getStyle() { return STYLE; }

    @Override
    public Roles getRole() { return Roles.PINATA; }

    private static class Factory implements RoleFactory<Pinata> {

        @Override
        public Pinata makeRole(@Nullable IndirectPlayer player) {
            return new Pinata(player);
        }
    }
}
