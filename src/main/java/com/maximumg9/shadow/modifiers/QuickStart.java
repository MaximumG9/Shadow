package com.maximumg9.shadow.modifiers;

import com.maximumg9.shadow.util.MiscUtil;
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

public class QuickStart extends Modifier {
    public static final ModifierFactory<QuickStart> FACTORY = new Factory();
    
    private static final ItemStack ITEM_STACK = new ItemStack(Items.STONE_AXE);
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.GREEN);
    
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new QuickStart(null).getName());
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }
    
    boolean triggered = false;
    
    QuickStart(IndirectPlayer player) {
        super(player);
    }
    @Override
    public String getRawName() {
        return "Quick Start";
    }
    @Override
    public boolean isStackable() { return false; }
    @Override
    public void init() {
        if (triggered) return;
        triggered = true;
        
        ItemStack pickaxe = new ItemStack(Items.STONE_PICKAXE);
        player.giveItemNow(pickaxe, MiscUtil.DELETE_WARN);
        
        ItemStack axe = new ItemStack(Items.STONE_AXE);
        player.giveItemNow(axe, MiscUtil.DELETE_WARN);
        
        ItemStack shovel = new ItemStack(Items.STONE_SHOVEL);
        player.giveItemNow(shovel, MiscUtil.DELETE_WARN);
    }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
    
    private static class Factory implements ModifierFactory<QuickStart> {
        @Override
        public QuickStart makeModifier(@Nullable IndirectPlayer player) {
            return new QuickStart(player);
        }
    }
    
}
