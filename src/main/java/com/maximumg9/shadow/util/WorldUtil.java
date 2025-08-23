package com.maximumg9.shadow.util;

import com.maximumg9.shadow.Shadow;
import com.mojang.logging.LogUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class WorldUtil {
    public static void regenerateEnd(Shadow shadow) {
        // @TODO fix this
    }
    
    public static int getTopYForBoundingBox(ServerWorld world, Box bb, Heightmap.Type heightMap) {
        int minX = MathHelper.floor(bb.minX);
        int maxX = MathHelper.floor(bb.maxX);
        int minZ = MathHelper.floor(bb.minZ);
        int maxZ = MathHelper.floor(bb.maxZ);
        
        LogUtils.getLogger().info("minX: {},maxX: {},minZ: {},maxZ: {}", bb.minX, bb.maxX, bb.minZ, bb.maxZ);
        
        int highest = world.getChunk(ChunkSectionPos.getSectionCoord(minX), ChunkSectionPos.getSectionCoord(minZ)).sampleHeightmap(heightMap, minX, minZ);
        int nhighest;
        
        if (minX != maxX) {
            nhighest = world.getChunk(ChunkSectionPos.getSectionCoord(maxX), ChunkSectionPos.getSectionCoord(minZ)).sampleHeightmap(heightMap, maxX, minZ);
            if (nhighest > highest) {
                highest = nhighest;
            }
        }
        if (minZ != maxZ) {
            nhighest = world.getChunk(ChunkSectionPos.getSectionCoord(minX), ChunkSectionPos.getSectionCoord(maxZ)).sampleHeightmap(heightMap, minX, maxZ);
            if (nhighest > highest) {
                highest = nhighest;
            }
            
            if (minX != maxX) {
                nhighest = world.getChunk(ChunkSectionPos.getSectionCoord(maxX), ChunkSectionPos.getSectionCoord(maxZ)).sampleHeightmap(heightMap, maxX, maxZ);
                if (nhighest > highest) {
                    highest = nhighest;
                }
            }
        }
        
        LogUtils.getLogger().info("y: {}", highest);
        
        return highest;
    }
}
    
    
        
