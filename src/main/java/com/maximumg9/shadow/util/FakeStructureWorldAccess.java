package com.maximumg9.shadow.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FakeStructureWorldAccess implements StructureWorldAccess {
    private final ServerWorld backing;
    
    private final List<BlockPos> portalFrames = new ArrayList<>();
    
    public FakeStructureWorldAccess(ServerWorld backingWorld) {
        this.backing = backingWorld;
    }
    
    public List<BlockPos> getPortalFrames() {
        return portalFrames;
    }
    
    @Override
    public long getSeed() {
        return this.backing.getSeed();
    }
    
    @Override
    public ServerWorld toServerWorld() {
        return this.backing;
    }
    
    @Override
    public long getTickOrder() {
        return this.backing.getTickOrder();
    }
    
    @Override
    public QueryableTickScheduler<Block> getBlockTickScheduler() {
        return this.backing.getBlockTickScheduler();
    }
    
    @Override
    public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
        return this.backing.getFluidTickScheduler();
    }
    
    @Override
    public WorldProperties getLevelProperties() {
        return this.backing.getLevelProperties();
    }
    
    @Override
    public LocalDifficulty getLocalDifficulty(BlockPos pos) {
        return this.backing.getLocalDifficulty(pos);
    }
    
    @Nullable
    @Override
    public MinecraftServer getServer() {
        return this.backing.getServer();
    }
    
    @Override
    public ChunkManager getChunkManager() {
        return this.backing.getChunkManager();
    }
    
    @Override
    public Random getRandom() {
        return this.backing.getRandom();
    }
    
    @Override
    public void playSound(@Nullable PlayerEntity source, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
    
    }
    
    @Override
    public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    
    }
    
    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
    
    }
    
    @Override
    public void emitGameEvent(RegistryEntry<GameEvent> event, Vec3d emitterPos, GameEvent.Emitter emitter) {
    
    }
    
    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return this.backing.getBrightness(direction, shaded);
    }
    
    @Override
    public LightingProvider getLightingProvider() {
        return this.backing.getLightingProvider();
    }
    
    @Override
    public WorldBorder getWorldBorder() {
        return this.backing.getWorldBorder();
    }
    
    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }
    
    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.backing.getBlockState(pos);
    }
    
    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.backing.getFluidState(pos);
    }
    
    @Override
    public List<Entity> getOtherEntities(@Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
        return List.of();
    }
    
    @Override
    public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate) {
        return List.of();
    }
    
    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return List.of();
    }
    
    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
        if (state.isOf(Blocks.END_PORTAL_FRAME)) {
            portalFrames.add(pos);
        }
        return true;
    }
    
    @Override
    public boolean removeBlock(BlockPos pos, boolean move) {
        return true;
    }
    
    @Override
    public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth) {
        return true;
    }
    
    @Override
    public boolean testBlockState(BlockPos pos, Predicate<BlockState> state) {
        return this.backing.testBlockState(pos, state);
    }
    
    @Override
    public boolean testFluidState(BlockPos pos, Predicate<FluidState> state) {
        return this.backing.testFluidState(pos, state);
    }
    
    @Nullable
    @Override
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        Chunk chunk = this.backing.getChunk(chunkX, chunkZ, leastStatus, create);
        if (chunk == null) return null;
        return new FakeChunk(chunk, this.backing.getRegistryManager().get(RegistryKeys.BIOME));
    }
    
    @Override
    public int getTopY(Heightmap.Type heightmap, int x, int z) {
        return this.backing.getTopY(heightmap, x, z);
    }
    
    @Override
    public int getAmbientDarkness() {
        return this.backing.getAmbientDarkness();
    }
    
    @Override
    public BiomeAccess getBiomeAccess() {
        return this.backing.getBiomeAccess();
    }
    
    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return this.backing.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }
    
    @Override
    public boolean isClient() {
        return false;
    }
    
    @Override
    public int getSeaLevel() {
        return this.backing.getSeaLevel();
    }
    
    @Override
    public DimensionType getDimension() {
        return this.backing.getDimension();
    }
    
    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.backing.getRegistryManager();
    }
    
    @Override
    public FeatureSet getEnabledFeatures() {
        return this.backing.getEnabledFeatures();
    }
}
