package com.maximumg9.shadow.screens;

import com.maximumg9.shadow.util.MiscUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class DecisionScreenHandler<V extends ItemRepresentable> extends ShadowScreenHandler {

    public final HashMap<Integer, V> decisionResultHashMap = new HashMap<>();

    @Nullable
    public final Callback<V> resultCallback;

    private final SimpleInventory inventory;

    private final int inventorySize;

    private final boolean autoClose;

    protected DecisionScreenHandler(int syncId, @Nullable Callback<V> resultCallback, List<V> values, PlayerInventory playerInventory, ScreenHandlerContext context, boolean autoClose) {
        super(getTypeForSize(values.size()), syncId, playerInventory);

        inventorySize = Math.ceilDiv(Math.max(values.size(), 1), 9) * 9;
        this.resultCallback = resultCallback;

        this.inventory = new SimpleInventory(inventorySize);

        initSlots();

        int i = 0;
        for (V value : values) {
            this.inventory.setStack(i, MiscUtil.getItemWithContext(value, context));
            decisionResultHashMap.put(i, value);
            i++;
        }
        
        this.autoClose = autoClose;
    }

    private static ScreenHandlerType<?> getTypeForSize(int size) {
        if (size <= 9) {
            return ScreenHandlerType.GENERIC_9X1;
        } else if (size <= 9 * 2) {
            return ScreenHandlerType.GENERIC_9X2;
        } else if (size <= 9 * 3) {
            return ScreenHandlerType.GENERIC_9X3;
        } else if (size <= 9 * 4) {
            return ScreenHandlerType.GENERIC_9X4;
        } else if (size <= 9 * 5) {
            return ScreenHandlerType.GENERIC_9X5;
        } else if (size <= 9 * 6) {
            return ScreenHandlerType.GENERIC_9X6;
        } else {
            throw new IllegalArgumentException("Cannot create an inventory with a size of more than" + (9 * 6));
        }
    }

    void initSlots() {
        for (int k = 0; k < inventorySize; ++k) {
            this.addSlot(
                new Slot(
                    inventory,
                    k,
                    0,
                    0
                )
            );
        }
        super.initSlots();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= this.decisionResultHashMap.size()) return;
        if (player instanceof ServerPlayerEntity sPlayer) {
            this.syncState();
            V value = decisionResultHashMap.get(slotIndex);
            if (this.autoClose) {
                sPlayer.closeHandledScreen();
            }
            if (this.resultCallback != null) {
                this.resultCallback.accept(value, sPlayer, button, actionType);
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @FunctionalInterface
    public interface Callback<V> {
        void accept(@Nullable V value, ServerPlayerEntity player, int button, SlotActionType actionType);
    }

    public static class Factory<V extends ItemRepresentable> implements NamedScreenHandlerFactory {

        public final Callback<V> resultCallback;
        private final Text name;
        private final List<V> values;
        private final boolean autoClose;

        public Factory(Text name, Callback<V> resultCallback, List<V> values, boolean autoClose) {
            this.name = name;
            this.resultCallback = resultCallback;
            this.values = values;
            this.autoClose = autoClose;
        }

        public Factory(Text name, Callback<V> resultCallback, List<V> values) {
            this.name = name;
            this.resultCallback = resultCallback;
            this.values = values;
            this.autoClose = true;
        }

        @Override
        public Text getDisplayName() {
            return name;
        }

        @Override
        public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new DecisionScreenHandler<>(syncId, this.resultCallback, values, playerInventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()), autoClose);
        }

    }
}
