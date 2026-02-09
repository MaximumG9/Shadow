package com.maximumg9.shadow.screens;

import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RolePickingScreenHandler extends DecisionScreenHandler<RolePickingScreenHandler.RoleOption> {
    protected RolePickingScreenHandler(
        int syncId,
        @Nullable Callback<RolePickingScreenHandler.RoleOption> resultCallback,
        List<RolePickingScreenHandler.RoleOption> values, PlayerInventory playerInventory,
        ScreenHandlerContext context,
        boolean autoClose
    ) {
        super(syncId, resultCallback, values, playerInventory, context, autoClose);
    }


    public static class Factory implements NamedScreenHandlerFactory {
        @Override
        public Text getDisplayName() {
            return null;
        }

        @Override
        public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return null;
        }
    }

    public static class RoleOption implements ItemRepresentable {
        private final Roles role;
        private IndirectPlayer claimer;

        public RoleOption(Roles role) {
            this.role = role;
            claimer = null;
        }

        @Override
        public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
            return claimer != null ?
                claimer.getAsItem(registries)
                : role.factory.makeRole(null).getAsItem(registries);
        }
    }
}
