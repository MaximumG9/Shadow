package com.maximumg9.shadow.config;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public enum Food {
    BREAD((count) -> new ItemStack(Items.BREAD, count)),
    BAKED_POTATO((count) -> new ItemStack(Items.BAKED_POTATO, count)),
    COOKED_COD((count) -> new ItemStack(Items.COOKED_COD, count)),
    COOKED_RABBIT((count) -> new ItemStack(Items.COOKED_RABBIT, count)),
    COOKED_SALMON((count) -> new ItemStack(Items.COOKED_SALMON, count)),
    COOKED_CHICKEN((count) -> new ItemStack(Items.COOKED_CHICKEN, count)),
    CHICKEN((count) -> new ItemStack(Items.CHICKEN, count)),
    RANDOM((count) -> {
        List<Food> nonRandomFoods = Arrays.stream(Food.values()).filter((food) -> !food.name().equals("RANDOM")).toList();
        return nonRandomFoods.get(Random.createLocal().nextBetween(0, nonRandomFoods.size())).foodGiver.apply(count);
    });
    
    public final Function<Integer, ItemStack> foodGiver;
    
    Food(Function<Integer, ItemStack> foodGiver) {
        this.foodGiver = foodGiver;
    }
}
