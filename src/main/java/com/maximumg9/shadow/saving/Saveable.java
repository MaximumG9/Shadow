package com.maximumg9.shadow.saving;

import com.maximumg9.shadow.util.IllegalSaveException;
import net.minecraft.nbt.NbtCompound;

public interface Saveable {

    NbtCompound writeNBT(NbtCompound nbt);

    // Overwrite the data in the object or throw an IllegalSaveException
    void readNBT(NbtCompound nbt) throws IllegalSaveException;
}
