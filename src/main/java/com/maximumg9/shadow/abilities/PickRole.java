package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.screens.RolePickingScreenHandler;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class PickRole extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("pick_role");
    private static final ItemStack ITEM_STACK = new ItemStack(Items.ENDER_EYE);

    static {
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.red("Pick Role")
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("Open the menu to pick a shadow role"),
                AbilityText()
            )
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }

    public PickRole(IndirectPlayer player) {
        super(player);
    }

    @Override
    public Identifier getID() {
        return ID;
    }

    @Override
    public AbilityResult apply() {
        this.player.getPlayerOrThrow().openHandledScreen(
            new RolePickingScreenHandler.Factory()
        );

        return AbilityResult.NO_CLOSE;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
}
