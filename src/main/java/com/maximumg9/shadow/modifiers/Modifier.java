package com.maximumg9.shadow.modifiers;

import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Objects;

public abstract class Modifier implements ItemRepresentable {
    
    final IndirectPlayer player;
    
    Modifier(IndirectPlayer player) {
        this.player = player;
    }
    
    public static Modifier load(NbtCompound nbt, IndirectPlayer player) {
        String modifierName = nbt.getString("name");
        if (Objects.equals(modifierName, "")) return null;
        Modifiers modifier = Modifiers.getModifier(modifierName);
        
        return modifier.factory.fromNBT(nbt, player);
    }
    
    public abstract boolean isStackable();
    
    public abstract String getRawName();
    
    public abstract Style getStyle();
    
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putString("name", this.getRawName());
        return nbt;
    }
    
    public void readNbt(NbtCompound nbt) { }
    
    public void init() { }
    public void deInit() { }
    
    public Text getName() {
        return Text
            .literal(getRawName())
            .setStyle(getStyle());
    }
}
