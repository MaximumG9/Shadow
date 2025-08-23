package com.maximumg9.shadow.screens;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;

public interface ItemRepresentable {
    ItemStack getAsItem(RegistryWrapper.WrapperLookup registries);
}
