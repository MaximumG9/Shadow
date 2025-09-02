package com.maximumg9.shadow.modifiers;

import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

public class ShadowTheHedgehog extends Modifier {
    public static final ModifierFactory<ShadowTheHedgehog> FACTORY = new Factory();
    
    private static final ItemStack ITEM_STACK = new ItemStack(Items.WIND_CHARGE);
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.BLACK);
    
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new ShadowTheHedgehog(null).getName());
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }
    
    boolean triggered = false;
    
    ShadowTheHedgehog(IndirectPlayer player) {
        super(player);
    }
    @Override
    public String getRawName() {
        return "Shadow (The Hedgehog)";
    }
    @Override
    public boolean isStackable() { return false; }
    @Override
    public void init() {
        if (triggered) return;
        triggered = true;
        
        player.giveEffectNow(new StatusEffectInstance(
            StatusEffects.SPEED,
            -1,
            2,
            true,
            false,
            true
        ));
    }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
    
    private static class Factory implements ModifierFactory<ShadowTheHedgehog> {
        @Override
        public ShadowTheHedgehog makeModifier(@Nullable IndirectPlayer player) {
            return new ShadowTheHedgehog(player);
        }
    }
    
}
