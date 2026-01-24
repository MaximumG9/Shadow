package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

// UNFINISHED

public class PinataHit extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("pinata_gifting");
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = new ItemStack(Items.TARGET, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("When hit for more than 3 hearts, the player that hits you is given special gifts."),
                PassiveText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.blue("Piñata Bonk")
        );
    }

    public PinataHit(IndirectPlayer player) { super(player); }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public AbilityResult apply() { return null; }
}
