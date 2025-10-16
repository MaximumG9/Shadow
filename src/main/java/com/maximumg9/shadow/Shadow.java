package com.maximumg9.shadow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maximumg9.shadow.commands.*;
import com.maximumg9.shadow.config.Config;
import com.maximumg9.shadow.items.AbilityStar;
import com.maximumg9.shadow.items.Eye;
import com.maximumg9.shadow.items.ItemUseCallback;
import com.maximumg9.shadow.items.ParticipationEye;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.LinkRegistry;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayerManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Unique;

import java.io.*;
import java.util.*;
import java.util.function.Function;

public class Shadow implements Tickable {
    
    public static final Map<Identifier, Function<Item.Settings, Item>> modifiedItems = new HashMap<>();
    
    public static final HashMap<Identifier, ItemUseCallback> ITEM_USE_CALLBACK_MAP = new HashMap<>();
    public static final File CONFIG_FILE = new File("config.nbt");
    private static final File INDIRECT_PLAYERS_FILE = new File("shadow-indirect-players.nbt");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final File STATE_FILE = new File("shadow-state.json");
    private static final Gson DATA_GSON;
    
    static {
        ITEM_USE_CALLBACK_MAP.put(
            AbilityStar.ID,
            new AbilityStar()
        );
        ITEM_USE_CALLBACK_MAP.put(
            ParticipationEye.ID,
            new ParticipationEye()
        );
        ITEM_USE_CALLBACK_MAP.put(
            LifeweaverHeart.ID,
            new LifeweaverHeart()
        );
    }
    
    static {
        GsonBuilder builder = new GsonBuilder();
        DATA_GSON = builder.create();
    }
    
    public final Config config = new Config(this, CONFIG_FILE.toPath());
    public final Random random = Random.create();
    private final MinecraftServer server;
    private final List<Tickable> tickables = new ArrayList<>();
    public IndirectPlayerManager indirectPlayerManager;
    public GameState state = new GameState();
    public LinkRegistry linkRegistry = new LinkRegistry(this);

    public Shadow(MinecraftServer server) {
        this.server = server;
        this.indirectPlayerManager = new IndirectPlayerManager(INDIRECT_PLAYERS_FILE, server);
        this.addTickable(this.indirectPlayerManager);
        
        try {
            this.loadSync();
        } catch (FileNotFoundException e) {
            LOGGER.warn("Failed to load data, creating file");
            this.state = new GameState();
        } catch (IOException e) {
            LOGGER.warn("Exception while loading data");
        }
        
        try {
            this.saveSync();
        } catch (IOException e) {
            LOGGER.warn("Failed to save data");
        }
    }
    
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        DebugCommand.register(dispatcher);
        LocationCommand.register(dispatcher);
        StartCommand.register(dispatcher);
        RolesCommand.register(dispatcher);
        ModifiersCommand.register(dispatcher);
        CancelCommand.register(dispatcher);
        ShadowChatCommand.register(dispatcher);
    }
    
    private static void save(GameState state, IndirectPlayerManager playerManager, Config config) throws IOException {
        FileWriter writer = new FileWriter(STATE_FILE);
        DATA_GSON.toJson(
            state,
            GameState.class,
            writer
        );
        writer.close();
        
        playerManager.save();
        config.save();
    }
    
    public boolean isNight() {
        return state.isNight;
    }
    public boolean isGracePeriod() { return server.getOverworld().getTime() - state.startTick <= config.gracePeriodTicks; }
    
    public void setNight() {
        this.state.isNight = true;
        this.indirectPlayerManager.getAllPlayers().forEach((player) -> {
            if (player.role != null) player.role.onNight();
        });
    }
    
    public void setDay() {
        this.state.isNight = false;
        this.indirectPlayerManager.getAllPlayers().forEach((player) -> {
            if (player.role != null) player.role.onDay();
        });
    }
    
    public void setSilentDay() {
        this.state.isNight = false;
    }
    
    public void ERROR(String message) {
        LOGGER.error(message);
        this.broadcast(
            TextUtil.red("[ERROR] ")
                .append(message)
        );
    }
    
    public void ERROR(Throwable t) {
        ERROR("", t);
    }
    
    public void ERROR(String message, Throwable t) {
        LOGGER.error(message, t);
        this.broadcast(TextUtil.red(message).append(
            TextUtil.red(
                Arrays.stream(t.getStackTrace())
                    .collect(
                        StringBuilder::new,
                        (str, stack) -> {
                            str.append(stack.toString());
                            str.append("\n");
                        },
                        (str1, str2) -> str1.append(str2.toString())
                    ).toString()
            )
        ));
    }
    
    public void LOG(String message) {
        LOGGER.info(message);
        Text messageAsText = TextUtil.gray("[LOG] ")
            .append(TextUtil.gray(message));
        if (config.debug) {
            this.getOnlinePlayers()
                .forEach(
                    (player) -> player.sendMessageNow(messageAsText)
                );
        } else {
            this.getOnlinePlayers().stream()
                .filter(player ->
                    player.role != null &&
                    player.role.getFaction() == Faction.SPECTATOR &&
                    player.getPlayerOrThrow().hasPermissionLevel(3)
                )
                .forEach(
                    (player) -> player.sendMessageNow(messageAsText)
                );
        }
    }
    
    public Collection<IndirectPlayer> getAllPlayers() {
        getOnlinePlayers();
        return this.indirectPlayerManager.getAllPlayers();
    }
    
    public void clearEyes() {
        for (Eye eye : this.state.eyes) {
            eye.destroy(this);
        }
        this.state.eyes.clear();
    }
    
    public void resetState() {
        try {
            this.clearEyes();
            
            this.state = new GameState(this.state);
            
            for (ServerWorld serverWorld : this.server.getWorlds()) {
                serverWorld.setTimeOfDay(0);
            }
            
            this.indirectPlayerManager.getAllPlayers().forEach((player) -> {
                player.clearPlayerData(CancelPredicates.NEVER_CANCEL);
                if (player.role != null) player.role.deInit();
                player.role = null;
                player.frozen = false;
            });
            
            this.config.roleManager.clearRoles();

            this.linkRegistry.clearLinks();
            
            this.saveAsync();
        } catch (Throwable t) {
            LogUtils.getLogger().error("error while cancelling", t);
        }
        
    }
    
    public void endGame(List<IndirectPlayer> winners, @Nullable Faction winningFaction, @Nullable Faction secondaryWinningFaction) {
        state.phase = GamePhase.WON;
        
        this.state.playedStrongholdPositions.add(
            this.state.strongholdChunkPosition
        );
        
        MutableText titleText;
        
        if (winningFaction == null) {
            winningFaction = secondaryWinningFaction;
            secondaryWinningFaction = null;
        }
        
        if (winningFaction == null) {
            titleText = Text.literal("Tie Game");
        } else {
            titleText = winningFaction.name.copy();
            titleText.append(" Win");
        }
        
        MutableText subtitleText;
        
        if (secondaryWinningFaction != null) {
            subtitleText = Text.literal("& ");
            subtitleText.append(secondaryWinningFaction.name);
        } else {
            subtitleText = null;
        }
        
        this.getAllPlayers().forEach((player) -> {
            player.scheduleUntil((sPlayer) -> sPlayer.changeGameMode(GameMode.SPECTATOR), CancelPredicates.cancelOnPhaseChange(state.phase));
            player.setTitleTimes(10, 40, 10, CancelPredicates.cancelOnPhaseChange(state.phase));
            player.sendTitle(titleText, CancelPredicates.cancelOnPhaseChange(state.phase));
            if (subtitleText != null) {
                player.sendSubtitle(subtitleText, CancelPredicates.cancelOnPhaseChange(state.phase));
            }
        });
        
        MutableText winnersText = TextUtil.gold("Winners are: ");
        
        winnersText.append(
            Texts.join(
                winners.stream().map(
                    (winner) ->
                        winner.getName().copy().setStyle(winner.role == null ? Style.EMPTY : winner.role.getStyle())
                ).toList(),
                TextUtil.gray(", ")
            )
        );
        
        this.broadcast(titleText.append(
            subtitleText == null ?
                Text.literal("") : subtitleText
        ));
        
        this.broadcast(winnersText);
        
        resetState();
        state.phase = GamePhase.WON;
    }
    
    public void broadcast(Text text) {
        this.server.getPlayerManager().broadcast(text, false);
        
    }
    
    public IndirectPlayer getIndirect(ServerPlayerEntity player) {
        return this.indirectPlayerManager.getIndirect(player);
    }
    
    public List<IndirectPlayer> getOnlinePlayers() {
        return this.server.getPlayerManager().getPlayerList().stream().map((player) -> this.indirectPlayerManager.getIndirect(player)).toList();
    }
    
    public MinecraftServer getServer() {
        return this.server;
    }
    
    public void addTickable(Tickable tickable) {
        this.tickables.add(tickable);
    }
    
    @Override
    public void tick() {
        List<Tickable> tickableCopy = this.tickables.stream().toList();
        for (Tickable tickable : tickableCopy) {
            tickable.tick();
            if (tickable.shouldEnd()) {
                tickable.onEnd();
                this.tickables.remove(tickable);
            }
        }
    }
    
    @Unique
    public void checkWin(@Nullable UUID playerToIgnore) {
        if (this.state.phase != GamePhase.PLAYING) return;
        
        long villagers = this.indirectPlayerManager
            .getRecentlyOnlinePlayers(this.config.disconnectTime)
            .stream()
            .filter(
                (player) ->
                    player.role != null &&
                        player.role.getFaction() == Faction.VILLAGER &&
                        (playerToIgnore == null || !playerToIgnore.equals(player.playerUUID))
            ).count();
        long shadows = this.indirectPlayerManager
            .getRecentlyOnlinePlayers(this.config.disconnectTime)
            .stream()
            .filter(
                (player) ->
                    player.role != null &&
                        player.role.getFaction() == Faction.SHADOW &&
                        (playerToIgnore == null || !playerToIgnore.equals(player.playerUUID))
            ).count();
        
        if (villagers == 0 && shadows == 0) {
            this.endGame(List.of(), null, null);
        } else if (villagers == 0) {
            this.endGame(
                this.indirectPlayerManager
                    .getRecentlyOnlinePlayers(this.config.disconnectTime)
                    .stream()
                    .filter(
                        (player) -> player.originalRole != null &&
                            player.originalRole.faction == Faction.SHADOW
                    ).toList(),
                Faction.SHADOW,
                null
            );
        } else if (shadows == 0) {
            this.endGame(
                this.indirectPlayerManager
                    .getRecentlyOnlinePlayers(this.config.disconnectTime)
                    .stream()
                    .filter(
                        (player) -> player.originalRole != null &&
                            player.originalRole.faction == Faction.VILLAGER
                    ).toList(),
                Faction.VILLAGER,
                null
            );
        }
    }
    
    public void init() {
        ServerWorld world = this.server.getOverworld();
        for (Eye eye : this.state.eyes) {
            Entity possibleDisplay = world.getEntity(eye.display());
            if (possibleDisplay == null) continue;
            possibleDisplay.setGlowing(true);
            possibleDisplay
                .getDataTracker()
                .set(Entity.FLAGS,
                    (byte) (possibleDisplay.getDataTracker().get(Entity.FLAGS) |
                        (1 << Entity.GLOWING_FLAG_INDEX)),
                    true
                );
        }
        
        this.state.startTick = server.getOverworld().getTime();
    }
    
    public void saveAsync() {
        LOGGER.info("Saving async...");
        
        GameState stateCopy = this.state.clone();
        IndirectPlayerManager playerManagerCopy = new IndirectPlayerManager(this.indirectPlayerManager);
        Config configCopy = this.config.copy(this);
        
        Util.getIoWorkerExecutor().submit(
            () -> {
                try {
                    save(stateCopy, playerManagerCopy, configCopy);
                } catch (IOException e) {
                    LOGGER.error("Error while saving data async", e);
                }
            }
        );
    }
    
    public void saveSync() throws IOException {
        save(this.state, this.indirectPlayerManager, this.config);
    }
    
    public void loadSync() throws IOException {
        FileReader reader = new FileReader(STATE_FILE);
        this.state = DATA_GSON.fromJson(
            reader,
            GameState.class
        );
        reader.close();
        
        this.indirectPlayerManager.load();
        this.config.load();
    }
}
