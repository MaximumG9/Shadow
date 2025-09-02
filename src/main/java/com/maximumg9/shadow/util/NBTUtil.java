package com.maximumg9.shadow.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public abstract class NBTUtil {
    public static final String ID_NAME = "id";
    public static final String INVISIBLE_KEY = "invisible";
    public static final String RESTRICT_MOVEMENT_KEY = "invisible";
    public static NbtCompound getCustomData(@NotNull ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return new NbtCompound();
        return component.copyNbt();
    }
    public static ItemStack applyCustomDataToStack(@NotNull ItemStack stack, UnaryOperator<NbtCompound> operator) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()), (component) -> {
            NbtCompound compound = component.copyNbt();
            return NbtComponent.of(operator.apply(compound));
        });
        return stack;
    }
    public static ItemStack flagRestrictMovement(@NotNull ItemStack stack) {
        return applyCustomDataToStack(stack, nbt -> {
            nbt.putBoolean(RESTRICT_MOVEMENT_KEY, true);
            return nbt;
        });
    }
    public static ItemStack flagAsInvisible(@NotNull ItemStack stack) {
        return applyCustomDataToStack(stack, nbt -> {
            nbt.putBoolean(INVISIBLE_KEY, true);
            return nbt;
        });
    }
    public static boolean hasID(@NotNull ItemStack stack, Identifier id) {
        return Objects.equals(getID(stack), id);
    }
    public static @Nullable Identifier getID(@NotNull ItemStack stack) {
        String id = getCustomData(stack).getString(ID_NAME);
        return id.isEmpty() ? null : Identifier.tryParse(id);
    }
    public static ItemStack addID(@NotNull ItemStack stack, Identifier id) {
        return applyCustomDataToStack(stack, (nbt) -> {
            nbt.putString(ID_NAME, id.toString());
            return nbt;
        });
    }

    public static ItemStack removeAttributeModifiers(ItemStack stack) {
        stack.set(
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            new AttributeModifiersComponent(List.of(), true)
        );
        return stack;
    }
}
