package com.maximumg9.shadow.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.tick.BasicTickScheduler;
import org.jetbrains.annotations.Nullable;

public class FakeChunk extends Chunk {
    private final Chunk backing;
    
    public FakeChunk(Chunk backingChunk, Registry<Biome> biomeRegistry) {
        super(backingChunk.getPos(), backingChunk.getUpgradeData(), backingChunk.getHeightLimitView(), biomeRegistry, backingChunk.getInhabitedTime(), backingChunk.getSectionArray(), backingChunk.getBlendingData());
        this.backing = backingChunk;
    }
    
    @Nullable
    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean moved) {
        return state;
    }
    
    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
    
    }
    
    @Override
    public void addEntity(Entity entity) {
    
    }
    
    @Override
    public ChunkStatus getStatus() {
        return this.backing.getStatus();
    }
    
    @Override
    public void removeBlockEntity(BlockPos pos) {
    
    }
    
    @Nullable
    @Override
    public NbtCompound getPackedBlockEntityNbt(BlockPos pos, RegistryWrapper.WrapperLookup registryLookup) {
        return new NbtCompound();
    }
    
    @Override
    public BasicTickScheduler<Block> getBlockTickScheduler() {
        return null;
    }
    
    @Override
    public BasicTickScheduler<Fluid> getFluidTickScheduler() {
        return null;
    }
    
    @Override
    public TickSchedulers getTickSchedulers() {
        return null;
    }
    
    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }
    
    @Override
    public BlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }
    
    // Disable Console Warning Text
    @Override
    public void markBlockForPostProcessing(short packedPos, int index) { }
    @Override
    public void markBlockForPostProcessing(BlockPos pos) { }
    
    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.getDefaultState();
    }
}
