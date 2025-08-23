package com.maximumg9.shadow.config;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class MaxCooldownManager {
    
    private final Object2IntOpenHashMap<Identifier> cooldownMap = new Object2IntOpenHashMap<>();
    
    public MaxCooldownManager() { }
    
    void readNbt(NbtCompound nbt) {
        for (String key : nbt.getKeys()) {
            Identifier id = Identifier.tryParse(key);
            if (id == null) continue;
            if (!nbt.contains(key, NbtElement.INT_TYPE)) continue;
            cooldownMap.put(id, nbt.getInt(key));
        }
    }
    
    public int getMaxCooldown(Identifier id, int defaultMaxCooldown) {
        return cooldownMap.putIfAbsent(id, defaultMaxCooldown);
    }
    
    NbtCompound writeNbt(NbtCompound nbt) {
        cooldownMap.object2IntEntrySet().fastForEach((entry) ->
            nbt.putInt(entry.getKey().toString(), entry.getIntValue())
        );
        return nbt;
    }
}
