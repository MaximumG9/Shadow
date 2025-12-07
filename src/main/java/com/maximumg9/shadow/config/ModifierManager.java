package com.maximumg9.shadow.config;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.modifiers.AddModifier;
import com.maximumg9.shadow.modifiers.ModifierSlot;
import com.maximumg9.shadow.modifiers.Modifiers;
import com.maximumg9.shadow.saving.Saveable;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.screens.ModifierSlotScreenHandler;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ModifierManager implements Saveable {
    private final List<ModifierSlot> modifierSlots;
    
    private final Shadow shadow;
    
    public ModifierManager(Shadow shadow) {
        modifierSlots = new ArrayList<>();
        this.shadow = shadow;
    }
    
    public void readNBT(NbtCompound nbt) {
        NbtList list = nbt.getList("modifierSlots", NbtElement.COMPOUND_TYPE);
        
        for (int i = 0; i < list.size(); i++) {
            if (modifierSlots.size() <= i) modifierSlots.add(new ModifierSlot(i));
            this.modifierSlots.get(i).readNBT(list.getCompound(i));
        }
    }
    
    public NbtCompound writeNBT(NbtCompound nbt) {
        NbtList list = new NbtList();
        modifierSlots.forEach(slot -> list.add(slot.writeNBT(new NbtCompound())));
        
        nbt.put("modifierSlots", list);
        
        return nbt;
    }
    
    public void clearModifiers() {
        shadow.getAllPlayers().forEach(
            (player) ->
                player.modifiers.clear()
        );
    }
    
    public boolean pickModifiers() {
        clearModifiers();
        HashMap<IndirectPlayer, List<Modifiers>> modifierTypes = new HashMap<>();
        shadow.getOnlinePlayers().stream()
            .filter((player -> player.participating))
            .toList()
            .forEach(player -> modifierTypes.put(
                player, new ArrayList<>()
            ));
        
        for (ModifierSlot modifierSlot : modifierSlots) {
            if (!modifierSlot.shouldAppear()) continue;
            
            boolean stackable = modifierSlot.modifier.factory.makeModifier(null).isStackable();
            List<IndirectPlayer> candidates = new ArrayList<>(shadow.getOnlinePlayers().stream()
                .filter((player -> stackable || !modifierTypes.get(player).contains(modifierSlot.modifier)))
                .unordered()
                .toList());
            
            Collections.shuffle(candidates);
            
            for (int i = 0; i < Math.min(modifierSlot.count, candidates.size()); i++) {
                modifierTypes.get(candidates.get(i)).add(modifierSlot.modifier);
            }
        }
        
        for (IndirectPlayer p : modifierTypes.keySet()) {
            modifierTypes.get(p).forEach(
                modifier -> p.modifiers.add(modifier.factory.makeModifier(p))
            );
        }
        
        return true;
    }
    
    public void showModifierListIndex(ServerPlayerEntity player, boolean editable) {
        List<ItemRepresentable> slots = new ArrayList<>(modifierSlots);
        slots.add(new AddModifier());
        
        player.openHandledScreen(new DecisionScreenHandler.Factory<>(
            Text.literal("Pick a Modifier Slot to edit"),
            (slot, clicker, button, action) -> {
                if (!editable) return;
                if (slot instanceof AddModifier) {
                    modifierSlots.add(new ModifierSlot(modifierSlots.size()));
                    showModifierListIndex(player, true);
                } else if (slot instanceof ModifierSlot) {
                    if (button == 1) {
                        this.modifierSlots.remove(slot);
                        for (int i = 0; i < modifierSlots.size(); i++) {
                            modifierSlots.get(i).reindex(i);
                        }
                        
                        this.showModifierListIndex(player, true);
                        return;
                    }
                    openModifierMenu((ModifierSlot) slot, clicker);
                } else
                    shadow.ERROR("Tried to open a modifier slot that is not a modifier slot or the add modifier slot");
            },
            slots
        ));
    }
    
    private void openModifierMenu(ModifierSlot slot, ServerPlayerEntity clicker) {
        System.out.println("tried to open modifier menu");
        if (slot == null) return;
        
        clicker.openHandledScreen(new ModifierSlotScreenHandler.Factory(
            slot.getName(),
            slot
        ));
        
        LogUtils.getLogger().info(slot.toString());
    }
    
}
