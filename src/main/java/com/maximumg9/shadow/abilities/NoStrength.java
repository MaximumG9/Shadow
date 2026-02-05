package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class NoStrength extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("strength_debuff");
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = new ItemStack(Items.GLASS_BOTTLE, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("You cannot enable Strength."),
                PassiveText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.withColour("Strength Debuff", Formatting.WHITE)
        );
    }

    public NoStrength(IndirectPlayer player) { super(player); }

    @Override
    public void init() {
        if (player.role.hasAbility(ToggleStrength.ID))
            getShadow().addTickable(Delay.instant(() ->
                player.role.removeAbility(player.role.getAbility(ToggleStrength.ID).get())
            ));
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public AbilityResult apply() { return AbilityResult.NO_CLOSE; }
}
