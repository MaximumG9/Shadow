package com.maximumg9.shadow.items;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.Delay;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record Eye(RegistryKey<World> worldKey, UUID item, UUID display, BlockPos position) {
    public void destroy(Shadow shadow) {
        ServerWorld world = shadow.getServer().getWorld(worldKey);
        
        if (world == null) return;
        
        world.getChunkManager().setChunkForced(new ChunkPos(position), true);
        
        shadow.addTickable(Delay.of(() -> {
            Entity itemEntity = world.getEntity(item);
            if (itemEntity != null) {
                itemEntity.remove(Entity.RemovalReason.DISCARDED);
            } else {
                // shadow.LOG("Tried to remove eye item that doesn't exist @" + position.toShortString());
            }
            
            Entity displayEntity = world.getEntity(display);
            if (displayEntity != null) {
                displayEntity.remove(Entity.RemovalReason.DISCARDED);
            } else {
                // shadow.LOG("Tried to remove eye display that doesn't exist @" + position.toShortString());
            }
            
            world.getChunkManager().setChunkForced(new ChunkPos(position), false);
        }, 5));
    }
    
    @Override
    public @NotNull String toString() {
        return "Eye{" +
            "worldKey=" + worldKey +
            ", item=" + item +
            ", display=" + display +
            ", position=" + position +
            '}';
    }
}
