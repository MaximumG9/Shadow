package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public interface RoleFactory<T extends Role> {
    T makeRole(@Nullable IndirectPlayer player);
    
    default T fromNBT(NbtCompound nbt, @Nullable IndirectPlayer player) {
        T role = makeRole(player);
        role.readNbt(nbt);
        return role;
    }
}
