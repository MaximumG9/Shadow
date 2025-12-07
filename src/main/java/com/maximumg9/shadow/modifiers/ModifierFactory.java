package com.maximumg9.shadow.modifiers;

import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public interface ModifierFactory<T extends Modifier> {
    T makeModifier(@Nullable IndirectPlayer player);
    
    default T fromNBT(NbtCompound nbt, @Nullable IndirectPlayer player) {
        T modifier = makeModifier(player);
        modifier.readNBT(nbt);
        return modifier;
    }
}
