package com.maximumg9.shadow.config;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.RoleSlot;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.roles.neutral.Spectator;
import com.maximumg9.shadow.saving.Saveable;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.screens.RoleSlotScreenHandler;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RoleManager implements Saveable {
    private final RoleSlot[] roleSlots;
    
    private final Shadow shadow;

    public RoleManager(Shadow shadow, Config config) {
        roleSlots = new RoleSlot[config.roleSlotCount];
        
        for (int i = 0; i < roleSlots.length; i++) {
            roleSlots[i] = new RoleSlot(i);
        }
        this.shadow = shadow;
    }
    
    public void readNBT(NbtCompound nbt) {
        NbtList list = nbt.getList("roleSlots", NbtElement.COMPOUND_TYPE);
        
        int length = Math.min(list.size(), roleSlots.length);
        
        for (int i = 0; i < length; i++) {
            this.roleSlots[i].readNBT(list.getCompound(i));
        }
        for (int i = length; i < roleSlots.length; i++) {
            this.roleSlots[i].reset();
        }
    }
    
    public NbtCompound writeNBT(NbtCompound nbt) {
        NbtList list = new NbtList();
        list.addAll(
            Arrays.stream(this.roleSlots)
                .map(
                    (slot) -> slot.writeNBT(new NbtCompound())
                ).toList()
        );
        
        nbt.put("roleSlots", list);
        
        return nbt;
    }
    
    public void clearRoles() {
        shadow.getAllPlayers().forEach(
            (player) -> {
                player.role.deInit();
                player.role = new Spectator(player);
                player.role.init();
            }
        );
    }
    
    public boolean pickRoles() {
        clearRoles();
        
        List<IndirectPlayer> participatingPlayers = new ArrayList<>(
            shadow.getOnlinePlayers().stream()
                .filter((player -> player.participating))
                .unordered()
                .toList()
        );
        
        Collections.shuffle(participatingPlayers);
        
        if (participatingPlayers.size() > this.roleSlots.length) {
            shadow.ERROR("More players than role slots, consider increasing the number of role slots in the config");
            return false;
        }
        
        for (int i = 0; i < participatingPlayers.size(); i++) {
            IndirectPlayer player = participatingPlayers.get(i);
            
            Roles role = this.roleSlots[i].pickRandomRole(shadow.random);
            
            player.originalRole = role;
            player.role = role.factory.makeRole(player);
        }
        
        // Set non participating players to spectators
        shadow.getOnlinePlayers().stream()
            .filter((player -> !player.participating))
            .forEach(player -> {
                player.originalRole = Roles.SPECTATOR;
                player.role = Roles.SPECTATOR.factory.makeRole(player);
            });
        
        shadow.getAllPlayers().stream()
            .filter(player -> player.getPlayer().isEmpty())
            .forEach(player -> {
                player.originalRole = Roles.SPECTATOR;
                player.role = Roles.SPECTATOR.factory.makeRole(player);
            });
        
        return true;
    }
    
    public void showRoleListIndex(ServerPlayerEntity player, boolean editable) {
        player.openHandledScreen(new DecisionScreenHandler.Factory<>(
            Text.literal("Pick a Role Slot to edit"),
            editable ? this::openRoleMenu : (slot, clicker, _b, _a) -> { },
            Arrays.asList(this.roleSlots)
        ));
    }
    
    private void openRoleMenu(RoleSlot slot, ServerPlayerEntity clicker, int button, SlotActionType actionType) {
        System.out.println("tried to open role menu");
        if (slot == null) return;
        
        clicker.openHandledScreen(new RoleSlotScreenHandler.Factory(
            slot.getName(),
            slot
        ));
        
        LogUtils.getLogger().info(slot.toString());
    }
    
}
