package com.maximumg9.shadow.screens;

import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandlerContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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



    public static class RoleOption implements ItemRepresentable {
        private final Roles role;
        private Optional<IndirectPlayer> claimer;
        public RoleOption(Roles role) {
            this.role = role;
            claimer = Optional.empty();
        }
        @Override
        public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
            return role.factory.makeRole(null).getAsItem(registries);
        }
    }
}
