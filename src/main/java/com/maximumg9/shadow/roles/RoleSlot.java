package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.saving.Saveable;
import com.maximumg9.shadow.screens.ItemRepresentable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class RoleSlot implements ItemRepresentable, Saveable {
    private final int[] weights = new int[Roles.values().length];
    
    private final int index;
    
    public RoleSlot(int index) {
        this.index = index;
        reset();
    }
    
    public void reset() {
        Arrays.fill(weights, 0);
    }
    
    public int weightSum() {
        int sum = 0;
        for (int weight : this.weights) {
            sum += weight;
        }
        return sum;
    }
    
    public void setWeight(Roles role, int weight) {
        weights[role.ordinal()] = weight;
    }
    
    public int getWeight(Roles role) {
        return weights[role.ordinal()];
    }
    
    public Roles pickRandomRole(Random random) {
        if (weightSum() == 0) return Roles.SPECTATOR;
        
        int value = random.nextBetween(1, weightSum());
        
        int currentSum = weights[0];
        int i = 0;
        while (currentSum < value) {
            if (i > weights.length) return null;
            currentSum += weights[i];
            i++;
        }
        i--;
        
        return Roles.values()[i];
    }
    
    public void readNBT(NbtCompound nbt) {
        int index = nbt.getInt("index");
        
        if (index != this.index)
            throw new IllegalStateException("Indexes in saved role slots do not match (maybe they weren't saved correctly?)");
        NbtCompound weights = nbt.getCompound("weights");
        
        weights.getKeys().stream()
            .map(
                (roleName) ->
                    new Pair<>(
                        Roles.getRole(roleName),
                        weights.getInt(roleName)
                    )
            ).forEach((weightPair) ->
                this.weights[weightPair.getLeft().ordinal()] = weightPair.getRight()
            );
    }
    
    public NbtCompound writeNBT(NbtCompound nbt) {
        NbtCompound weights = new NbtCompound();
        
        for (int i = 0; i < this.weights.length; i++) {
            weights.putInt(Roles.values()[i].name, this.weights[i]);
        }
        
        nbt.put("weights", weights);
        nbt.putInt("index", this.index);
        
        return nbt;
    }
    
    public Text getName() {
        return Text.literal("Slot #" + (this.index + 1));
    }
    
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ItemStack stack = new ItemStack(Items.BARRIER);
        for (int i = 0; i < this.weights.length; i++) {
            if (this.weights[i] == 0) continue;
            
            if (stack.getItem().equals(Items.BARRIER)) {
                stack = Roles.values()[i].factory.makeRole(null).getAsItem(registries);
            } else {
                stack = new ItemStack(Items.STRUCTURE_VOID);
                break;
            }
        }
        
        // @TODO: If the weights match a predefined bucket, show the ItemStack for that bucket instead of the structure void.
        // Haha bucket lover!!!!!
        
        stack.set(DataComponentTypes.ITEM_NAME, getName());
        ArrayList<Text> loreList = new ArrayList<>();
        
        for (int i = 0; i < this.weights.length; i++) {
            if (this.weights[i] == 0) continue;
            loreList.add(
                Roles.values()[i]
                    .factory.makeRole(null)
                    .getName().copy()
                    .append(": " + this.weights[i])
            );
        }
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(loreList, loreList));
        return stack;
    }
    
    @Override
    public String toString() {
        return "RoleSlot={index=" + index + ",weights=" + Arrays.toString(weights) + "}";
    }
}
