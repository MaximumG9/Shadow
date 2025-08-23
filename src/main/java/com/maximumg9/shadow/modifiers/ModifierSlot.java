package com.maximumg9.shadow.modifiers;

import com.maximumg9.shadow.screens.ItemRepresentable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ModifierSlot implements ItemRepresentable {
    private int index;
    public Modifiers modifier;
    public int count;
    public float chance;
    
    public ModifierSlot(int index) {
        this.index = index;
        reset();
    }
    
    public void reset() {
        this.chance = 1;
        this.count = 1;
        this.modifier = Modifiers.QUICK_START;
    }
    
    public boolean shouldAppear() { return Math.random() < chance; }
    
    public void reindex(int newIndex) {
        this.index = newIndex;
    }
    
    public void readNbt(NbtCompound nbt) {
        int index = nbt.getInt("index");
        
        if (index != this.index)
            throw new IllegalStateException("Indexes in saved modifier slots do not match (maybe they weren't saved correctly?)");
        
        this.modifier = Modifiers.getModifier(nbt.getString("modifier"));
        this.count = nbt.getInt("count");
        this.chance = nbt.getFloat("chance");
    }
    
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("index", this.index);
        nbt.putString("modifier", this.modifier.name);
        nbt.putInt("count", this.count);
        nbt.putFloat("chance", this.chance);
        
        return nbt;
    }
    
    public Text getName() {
        return Text.literal("Slot #" + (this.index + 1));
    }
    
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ItemStack stack = modifier.factory.makeModifier(null).getAsItem(registries);
        List<Text> loreList = List.of(
            Text.literal(count + "x ").styled(style -> style.withColor(Formatting.BLUE))
                .append(modifier.factory.makeModifier(null).getName()),
            Text.literal("with " + String.format("%.0f%%", chance * 100) + " chance").styled(style -> style.withColor(Formatting.GRAY))
        );
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(loreList, loreList));
        return stack;
    }
    
    @Override
    public String toString() {
        return "ModifierSlot={index=" + index + ",modifier=" + modifier.name() + ",chance=" + chance + "}";
    }
}
