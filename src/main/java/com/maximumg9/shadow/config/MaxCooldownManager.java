package com.maximumg9.shadow.config;

import com.maximumg9.shadow.saving.Saveable;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class MaxCooldownManager implements Saveable {
    
    private final Object2LongOpenHashMap<Identifier> cooldownMap = new Object2LongOpenHashMap<>();
    
    public MaxCooldownManager() { }
    
    public void readNBT(NbtCompound nbt) {
        for (String key : nbt.getKeys()) {
            Identifier id = Identifier.tryParse(key);
            if (id == null) continue;
            if (!nbt.contains(key, NbtElement.INT_TYPE)) continue;
            cooldownMap.put(id, nbt.getLong(key));
        }
    }
    
    public long getMaxCooldown(Identifier id, long defaultMaxCooldown) {
        return cooldownMap.putIfAbsent(id, defaultMaxCooldown);
    }
    
    public NbtCompound writeNBT(NbtCompound nbt) {
        cooldownMap.object2LongEntrySet().fastForEach((entry) ->
            nbt.putLong(entry.getKey().toString(), entry.getLongValue())
        );
        return nbt;
    }
}
