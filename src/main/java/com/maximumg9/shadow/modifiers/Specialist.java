package com.maximumg9.shadow.modifiers;

import com.maximumg9.shadow.abilities.SpecialistSelectKit;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

public class Specialist extends Modifier {
    public static final ModifierFactory<Specialist> FACTORY = new Specialist.Factory();

    private static final ItemStack ITEM_STACK = new ItemStack(Items.DAMAGED_ANVIL);
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.GRAY);

    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Specialist(null).getName());
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }

    Specialist(IndirectPlayer player) {
        super(player);
    }
    @Override
    public String getRawName() {
        return "Specialist";
    }
    @Override
    public boolean isStackable() { return false; }
    @Override
    public void init() {
        player.role.addAbility(SpecialistSelectKit::new);
    }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    private static class Factory implements ModifierFactory<Specialist> {
        @Override
        public Specialist makeModifier(@Nullable IndirectPlayer player) {
            return new Specialist(player);
        }
    }

}
