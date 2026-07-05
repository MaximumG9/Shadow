package com.maximumg9.shadow.abilities.shadow;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.AbilityResult;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class NoStrength extends ToggleStrength {
    private static final ItemStack ITEM_STACK;

    public static final Identifier ID = MiscUtil.shadowID("no_strength");

    static {
        ITEM_STACK = new ItemStack(Items.GLASS_BOTTLE, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("You cannot enable Strength."),
                Ability.PassiveText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.withColour("Strength Debuff", Formatting.WHITE)
        );
    }


    public NoStrength(IndirectPlayer player) {
        super(player);
    }

    @Override
    public void init() {
    }

    @Override
    public Identifier getID() {
        return ID;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    @Override
    public AbilityResult apply() { return AbilityResult.NO_CLOSE; }
}
