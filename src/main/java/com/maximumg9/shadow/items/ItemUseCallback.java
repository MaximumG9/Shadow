package com.maximumg9.shadow.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@FunctionalInterface
public interface ItemUseCallback {
    TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand);
}
