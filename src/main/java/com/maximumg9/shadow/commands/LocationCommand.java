package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.FakeStructureWorldAccess;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.WorldUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;
import static net.minecraft.server.command.CommandManager.literal;

public class LocationCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("$location")
                .requires((source) -> source.hasPermissionLevel(3))
                .then(
                    literal("force")
                        .executes((ctx) -> {
                            MinecraftServer server = ctx.getSource().getServer();
                            Shadow shadow = getShadow(server);
                            try {
                                shadow.resetState();
                                
                                shadow.state.currentLocation = BlockPos.ofFloored(ctx.getSource().getPosition());
                            } catch (Throwable t) {
                                shadow.ERROR(t);
                            }
                            
                            return 1;
                        })
                )
                .then(
                    literal("skip")
                        .executes((ctx) -> {
                            MinecraftServer server = ctx.getSource().getServer();
                            Shadow shadow = getShadow(server);
                            try {
                                shadow.state.playedStrongholdPositions.add(shadow.state.strongholdChunkPosition);
                                
                                shadow.resetState();
                                
                                shadow.saveAsync();
                                
                                return findAndGotoLocation(ctx);
                            } catch (Throwable t) {
                                LogUtils.getLogger().error("error while forcing a location", t);
                                shadow.ERROR(t);
                            }
                            return 0;
                        })
                )
                .executes((ctx) -> {
                    int ret = 0;
                    try {
                        ret = LocationCommand.findAndGotoLocation(ctx);
                    } catch (Exception e) {
                        getShadow(ctx.getSource().getServer()).ERROR(e);
                    }
                    return ret;
                })
        );
    }
    
    public static int findAndGotoLocation(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource src = ctx.getSource();
        ServerWorld overworld = src.getServer().getOverworld();
        MinecraftServer server = src.getServer();
        Shadow shadow = getShadow(server);
        
        if (!shadow.state.phase.canSelectLocation) {
            src.sendFeedback(
                () ->
                    TextUtil.red("Cannot select location in phase " + shadow.state.phase),
                false
            );
            return -1;
        }
        
        BlockBox pathBounding = findLocation(ctx);
        
        if (pathBounding == null) {
            shadow.ERROR("Path through stronghold not found");
            return -1;
        }
        
        int xRange = shadow.config.worldBorderSize / 2;
        int zRange = shadow.config.worldBorderSize / 2;
        
        pathBounding = pathBounding.expand(
            Math.max(xRange - pathBounding.getBlockCountX(), -pathBounding.getBlockCountX()/2),
            0,
            Math.max(zRange - pathBounding.getBlockCountZ(), -pathBounding.getBlockCountZ()/2)
        );
        
        int x = overworld.getRandom().nextBetween(pathBounding.getMinX(), pathBounding.getMaxX());
        int z = overworld.getRandom().nextBetween(pathBounding.getMinZ(), pathBounding.getMaxZ());
        
        shadow.state.currentLocation = new BlockPos(x, 0, z);
        Vec3d teleportPos = shadow.state.currentLocation.toBottomCenterPos();
        
        arrangePlayersInCircle(overworld, teleportPos, server.getPlayerManager().getPlayerList());
        
        overworld.getWorldBorder().setCenter(shadow.state.currentLocation.getX(), shadow.state.currentLocation.getZ());
        overworld.getWorldBorder().setSize(shadow.config.worldBorderSize);
        World nether = server.getWorld(World.NETHER);
        if (nether != null) {
            double scale = DimensionType.getCoordinateScaleFactor(overworld.getDimension(), nether.getDimension());
            nether.getWorldBorder().setCenter(shadow.state.currentLocation.getX() * scale, shadow.state.currentLocation.getZ() * scale);
            nether.getWorldBorder().setSize(shadow.config.worldBorderSize);
        }
        World end = server.getWorld(World.END);
        if (end != null) {
            end.getWorldBorder().setCenter(0, 0);
            end.getWorldBorder().setSize(WorldBorder.STATIC_AREA_SIZE);
        }
        
        
        List<Entity> nonPlayers = new ArrayList<>();
        
        server.getWorlds().forEach(
            world ->
                world.collectEntitiesByType(
                    TypeFilter.instanceOf(Entity.class),
                    (entity) -> entity.getType() != EntityType.PLAYER,
                    nonPlayers
                )
        );
        
        nonPlayers.forEach(Entity::discard);
        
        src.sendFeedback(
            () ->
                Text.literal(
                    "Searched and Went to Location"
                ),
            true
        );
        
        shadow.state.phase = GamePhase.LOCATION_SELECTED;
        
        shadow.saveAsync();
        
        for (IndirectPlayer player : shadow.getOnlinePlayers()) {
            player.getPlayerOrThrow().changeGameMode(GameMode.ADVENTURE);
            player.frozen = true;
        }
        
        return 1;
    }
    
    // Returns BlockBox encompassing the path from the stronghold start to the portal room
    public static BlockBox findLocation(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource src = ctx.getSource();
        MinecraftServer server = src.getServer();
        ServerWorld overworld = server.getOverworld();
        Shadow shadow = getShadow(server);
        
        Optional<RegistryEntry.Reference<Structure>> strongholdStructure = overworld.getRegistryManager().get(RegistryKeys.STRUCTURE).getEntry(StructureKeys.STRONGHOLD);
        
        if (strongholdStructure.isEmpty()) {
            shadow.ERROR("Strongholds aren't generating (maybe you have a mod or datapack that modifies stronghold generation?)");
            return null;
        }
        
        StructureSet strongholds = overworld
            .getRegistryManager()
            .get(RegistryKeys.STRUCTURE_SET)
            .get(StructureSetKeys.STRONGHOLDS);
        
        if (strongholds == null) {
            shadow.ERROR("Strongholds aren't generating (maybe you have a mod or datapack that modifies stronghold generation?)");
            return null;
        }
        StructurePlacement strongholdPlacement = strongholds.placement();
        
        if (!(strongholdPlacement instanceof ConcentricRingsStructurePlacement)) {
            shadow.ERROR("Strongholds are not in concentric rings (maybe you have a mod or datapack that modifies stronghold generation?)");
            return null;
        }
        
        List<ChunkPos> placementPositions = new ArrayList<>(
            Objects.requireNonNull(
                overworld
                    .getChunkManager()
                    .getStructurePlacementCalculator()
                    .getPlacementPositions((ConcentricRingsStructurePlacement) strongholdPlacement)
            )
        );
        
        placementPositions.removeAll(shadow.state.playedStrongholdPositions);

        Instant startTime = Instant.now();

        src.sendMessage(
            TextUtil.withColour("Generating stronghold", Formatting.AQUA)
        );
        
        ChunkPos startChunkPos = placementPositions.getFirst();

        FakeStructureWorldAccess fakeStructureWorldAccess = new FakeStructureWorldAccess(overworld);
        
        strongholdStructure.get().value().createStructureStart(
            overworld.getRegistryManager(),
            overworld.getChunkManager().getChunkGenerator(),
            overworld.getChunkManager().getChunkGenerator().getBiomeSource(),
            overworld.getChunkManager().getNoiseConfig(),
            overworld.getStructureTemplateManager(),
            overworld.getSeed(),
            startChunkPos,
            0,
            fakeStructureWorldAccess,
            (biome) -> true
        );
        
        shadow.state.strongholdChunkPosition = startChunkPos;

        long timeInMillis = Duration.between(startTime, Instant.now()).toMillis();

        src.sendMessage(
            TextUtil.withColour(
                "Finished generating portal rooms in (" + timeInMillis + "ms)",
                Formatting.AQUA
            )
        );
        
        shadow.saveAsync();
        
        Optional<BlockBox> possibleBox = fakeStructureWorldAccess.getPathFromPortalToStart()
                .stream().map(StructurePiece::getBoundingBox)
                .reduce(BlockBox::encompass);
        
        return possibleBox.orElse(null);
    }
    
    public static void arrangePlayersInCircle(ServerWorld world, Vec3d centerPos, List<ServerPlayerEntity> players) {
        int radius = players.size();
        
        float angleIncrement = MathHelper.TAU / players.size();
        
        float currentAngle = 0;
        
        for (ServerPlayerEntity player : players) {
            currentAngle += angleIncrement;
            
            float xOffset = MathHelper.cos(currentAngle) * radius;
            float zOffset = MathHelper.sin(currentAngle) * radius;
            
            double x = xOffset + centerPos.x;
            double z = zOffset + centerPos.z;
            
            int y = 1 + WorldUtil.getTopYForBoundingBox(world, player.getBoundingBox(player.getPose()).offset(x, 0, z), Heightmap.Type.MOTION_BLOCKING);
            
            player.teleport(world, x, y, z, currentAngle * 180 / MathHelper.PI, 0);
        }
    }
}
