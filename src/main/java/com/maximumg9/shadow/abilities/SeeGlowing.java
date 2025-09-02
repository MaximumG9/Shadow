package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SeeGlowing extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("see_glowing");
    private static final ItemStack ITEM_STACK;
    
    static {
        ITEM_STACK = new ItemStack(Items.ENDER_EYE);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("See Glowing During Night")
                .styled(style -> style.withColor(Formatting.GOLD))
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("See glowing during the night, when others can't."),
                PassiveText()
            )
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }
    
    public SeeGlowing(IndirectPlayer player) {
        super(player);
    }
    @Override
    public Identifier getID() { return ID; }
    
    @Override
    public AbilityResult apply() {
        return AbilityResult.NO_CLOSE;
    }
    
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
}
