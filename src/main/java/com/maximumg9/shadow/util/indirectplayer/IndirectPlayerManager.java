package com.maximumg9.shadow.util.indirectplayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maximumg9.shadow.Tickable;
import com.maximumg9.shadow.saving.Saveable;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.ArrayListDeque;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class IndirectPlayerManager implements Tickable, Saveable {
    public static final Gson GSON;
    
    static {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        GSON = builder.create();
    }
    
    private final Path saveFile;
    private final HashMap<UUID, IndirectPlayer> indirectPlayers = new HashMap<>();
    private final MinecraftServer server;
    private final ArrayListDeque<IndirectPlayerTask> tasks = new ArrayListDeque<>();
    
    public IndirectPlayerManager(IndirectPlayerManager other) {
        this.saveFile = other.saveFile;
        this.server = other.server;
        other.indirectPlayers.forEach(
            (uuid, indirectPlayer) -> this.indirectPlayers.put(uuid, new IndirectPlayer(indirectPlayer))
        );
    }
    
    public IndirectPlayerManager(File saveFile, MinecraftServer server) {
        this.saveFile = saveFile.toPath();
        this.server = server;
    }

    public void load() throws IOException {
        NbtCompound nbt = NbtIo.readCompressed(this.saveFile, new NbtSizeTracker(0xffffffffffffL, 256));
        this.readNBT(nbt);
    }

    public void readNBT(NbtCompound nbt) {
        NbtList list = nbt.getList("indirectPlayers", NbtElement.COMPOUND_TYPE);
        
        for (int i = 0; i < list.size(); i++) {
            NbtCompound indirectPlayerData = list.getCompound(i);
            UUID uuid = IndirectPlayer.getUUIDForData(indirectPlayerData);
            IndirectPlayer existingPlayer = this.indirectPlayers.get(uuid);
            if(existingPlayer != null) {
                existingPlayer.readNBT(indirectPlayerData);
            } else {
                IndirectPlayer player = new IndirectPlayer(this.server, uuid);
                this.indirectPlayers.put(player.playerUUID, player);
            }
        }
    }

    public void save() throws IOException {
        NbtCompound data = this.writeNBT(new NbtCompound());
        NbtIo.writeCompressed(data, this.saveFile);
    }

    public NbtCompound writeNBT(NbtCompound nbt) {
        NbtList lv = new NbtList();
        for (IndirectPlayer player : indirectPlayers.values()) {
            lv.add(player.writeNBT(new NbtCompound()));
        }
        
        nbt.put("indirectPlayers", lv);
        
        return nbt;
    }

    @Override
    public void tick() {
        for (IndirectPlayerTask task : tasks) {
            boolean shouldEndTask = task.shouldEnd();
            if (shouldEndTask) {
                tasks.remove(task);
                task.onEnd();
            }
        }
        for (IndirectPlayer player : indirectPlayers.values()) {
            player.tick();
        }
    }

    void schedule(IndirectPlayerTask task) {
        this.tasks.add(task);
    }

    public Collection<IndirectPlayer> getAllPlayers() {
        return this.indirectPlayers.values();
    }

    public Collection<IndirectPlayer> getRecentlyOnlinePlayers(int maxTicksOffline) {
        return this.indirectPlayers.values().stream()
            .filter(iP -> iP.getOfflineTicks() <= maxTicksOffline)
            .toList();
    }

    public IndirectPlayer get(UUID uuid) {
        return this.indirectPlayers.computeIfAbsent(uuid, (Uuid) -> new IndirectPlayer(server, Uuid));
    }

    public IndirectPlayer getIndirect(ServerPlayerEntity base) {
        return this.indirectPlayers.computeIfAbsent(base.getUuid(), (uuid) -> new IndirectPlayer(base));
    }
    
    static class IndirectPlayerTask implements Tickable {
        private final Consumer<ServerPlayerEntity> task;
        private final Predicate<IndirectPlayer> disableCondition;
        private final IndirectPlayer player;
        
        IndirectPlayerTask(IndirectPlayer player, Consumer<ServerPlayerEntity> task, Predicate<IndirectPlayer> cancelCondition) {
            this.player = player;
            this.task = task;
            this.disableCondition = cancelCondition;
        }
        
        @Override
        public boolean shouldEnd() {
            if (this.disableCondition.test(player)) return true;
            
            Optional<ServerPlayerEntity> sPlayer = player.getPlayer();
            if (sPlayer.isPresent()) {
                this.task.accept(sPlayer.get());
                return true;
            }
            
            return false;
        }
        
        @Override
        public void tick() { }
    }
    
}
