package com.maximumg9.shadow.util.indirectplayer;


import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.villager.AddHealthLink;
import com.maximumg9.shadow.config.InternalTeam;
import com.maximumg9.shadow.modifiers.Modifier;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.roles.neutral.Spectator;
import com.maximumg9.shadow.saving.Saveable;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.MiscUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This is meant to represent a player who existed at some time, even if the player does not exist now
 */
public class IndirectPlayer implements ItemRepresentable, Saveable {
    public final UUID playerUUID;
    final MinecraftServer server;
    @NotNull
    public Role role;
    @Nullable
    public Roles originalRole;
    public ArrayList<Modifier> modifiers = new ArrayList<>();
    public boolean participating = true;
    public boolean frozen;
    public int chatMessageCooldown;
    public NbtCompound extraStorage;
    private int offlineTicks = Integer.MAX_VALUE;
    private String name = null;
    @Nullable
    public AddHealthLink.Link link = null;
    private final HashMap<IndirectPlayer, InternalTeam> teamViewOverrides = new HashMap<>();
    
    public IndirectPlayer(ServerPlayerEntity base) {
        this.playerUUID = base.getUuid();
        this.server = base.server;
        this.role = new Spectator(this);
        this.name = base.getNameForScoreboard();
        this.extraStorage = new NbtCompound();

        getShadow().addTickable(Delay.instant(this.role::roleInit));
    }
    
    IndirectPlayer(MinecraftServer server, UUID uuid) {
        this.server = server;
        this.playerUUID = uuid;
        this.extraStorage = new NbtCompound();
        this.role = new Spectator(this);

        getShadow().addTickable(Delay.instant(this.role::roleInit));
    }
    
    @SuppressWarnings("CopyConstructorMissesField")
    IndirectPlayer(IndirectPlayer src) {
        this.playerUUID = src.playerUUID;
        this.server = src.server;
        this.role = src.role;
        this.modifiers = src.modifiers;
        this.participating = src.participating;
        this.frozen = src.frozen;
        this.name = src.getLiteralName();
        this.chatMessageCooldown = src.chatMessageCooldown;
        this.originalRole = src.originalRole;
        this.offlineTicks = src.offlineTicks;
        this.extraStorage = src.extraStorage.copy();
    }

    static UUID getUUIDForData(NbtCompound nbt) {
        return nbt.getUuid("playerUUID");
    }

    public void readNBT(NbtCompound nbt) {
        this.frozen = nbt.getBoolean("frozen");
        this.participating = nbt.getBoolean("participating");
        this.name = nbt.getString("name");
        Role tempRole = null;

        if (nbt.contains("role", NbtElement.COMPOUND_TYPE)) {
            tempRole = Role.load(nbt.getCompound("role"), this);
        }

        if (nbt.contains("original_role", NbtElement.STRING_TYPE)) {
            this.originalRole = Roles.getRole(nbt.getString("original_role"));
        } else {
            this.originalRole = null;
        }

        if (nbt.contains("modifiers", NbtElement.LIST_TYPE)) {
            for (int i = 0; i < nbt.getList("modifiers", NbtElement.COMPOUND_TYPE).size(); i++) {
                this.modifiers.add(
                    Modifier.load(
                        nbt.getList(
                            "modifiers",
                            NbtElement.COMPOUND_TYPE
                        ).getCompound(i),
                        this
                    )
                );
            }
        }

        NbtCompound teamViewOverrides = nbt.getCompound("team_view_overrides");
        teamViewOverrides.getKeys()
            .forEach(
                (playerUUID) -> {
                    this.teamViewOverrides.put(
                        getShadow().indirectPlayerManager.get(UUID.fromString(playerUUID)),
                        InternalTeam.getTeam(teamViewOverrides.getString(playerUUID))
                    );
                }
            );

        this.offlineTicks = nbt.getInt("offline_ticks");
        this.extraStorage = nbt.getCompound("extra_storage");

        if(tempRole != null) {
            this.role = tempRole;
        } else {
            this.role = new Spectator(this);
            getShadow().addTickable(Delay.instant(this.role::roleInit));
        }
    }

    public Shadow getShadow() {
        return MiscUtil.getShadow(this.server);
    }

    public NbtCompound writeNBT(NbtCompound nbt) {
        nbt.putUuid("playerUUID", this.playerUUID);
        nbt.putBoolean("frozen", this.frozen);
        nbt.putBoolean("participating", this.participating);
        nbt.putString("name", this.name);

        nbt.put("role", this.role.writeNBT(new NbtCompound()));

        if (this.originalRole != null) {
            nbt.putString("original_role", this.originalRole.name);
        }
        
        NbtList list = new NbtList();
        list.addAll(this.modifiers.stream().map(modifier -> modifier.writeNBT(new NbtCompound())).toList());
        nbt.put("modifiers", list);

        NbtCompound teamViewOverrides = new NbtCompound();

        this.teamViewOverrides.forEach(
            (player, team) -> {
                teamViewOverrides.putString(
                    player.playerUUID.toString(),
                    team.teamName
                );
            }
        );

        nbt.put("team_view_overrides", teamViewOverrides);
        
        nbt.putInt("offline_ticks", this.offlineTicks);
        nbt.put("extra_storage", this.extraStorage);
        
        return nbt;
    }
    
    public void tick() {
        chatMessageCooldown = chatMessageCooldown > 0 ? chatMessageCooldown - 1 : 0;
        if (this.getPlayer().isPresent()) {
            this.offlineTicks = 0;
        } else if (offlineTicks < Integer.MAX_VALUE) {
            this.offlineTicks++;
        }
        this.role.tick();
    }
    
    public int getOfflineTicks() {
        return this.offlineTicks;
    }
    
    public String getLiteralName() {
        this.getPlayer().ifPresent((psPlayer) -> this.name = psPlayer.getNameForScoreboard());
        if (name == null) {
            UserCache cache = this.server.getUserCache();
            if (cache != null) {
                Optional<GameProfile> profile = cache.getByUuid(this.playerUUID);
                this.name = profile.map(
                    GameProfile::getName
                ).orElse(
                    playerUUID.toString()
                );
            } else {
                this.name = playerUUID.toString();
            }

        }
        return this.name;
    }

    public Text getName() {
        return Text.of(getLiteralName());
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof IndirectPlayer)) return false;
        else return this.playerUUID.equals(((IndirectPlayer) object).playerUUID);
    }

    @Override
    public int hashCode() {
        return this.playerUUID.hashCode();
    }
    
    public ServerPlayerEntity getPlayerOrThrow() throws OfflinePlayerException {
        return this.getPlayer()
            .orElseThrow(OfflinePlayerException::new);
    }
    
    public Optional<ServerPlayerEntity> getPlayer() {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(this.playerUUID));
    }
    
    public void damage(DamageSource source, float amount, Predicate<IndirectPlayer> cancelPredicate) {
        scheduleUntil(
            (player) -> player.damage(source, amount),
            cancelPredicate
        );
    }

    public double getSquaredDistance(IndirectPlayer other) {
        Optional<ServerPlayerEntity> thisPlayer = this.getPlayer();
        Optional<ServerPlayerEntity> otherPlayer = other.getPlayer();
        if(thisPlayer.isEmpty() || otherPlayer.isEmpty()) return Double.NaN;
        return thisPlayer.get().squaredDistanceTo(otherPlayer.get());
    }
    
    public void damageOrThrow(DamageSource source, float amount) {
        this.getPlayerOrThrow()
            .damage(source, amount);
    }
    
    public void giveEffect(StatusEffectInstance effect, Predicate<IndirectPlayer> cancelPredicate) {
        scheduleUntil(
            (player) -> player.addStatusEffect(effect),
            cancelPredicate
        );
    }
    
    public void giveEffectOrThrow(StatusEffectInstance effect) {
        this.getPlayerOrThrow()
            .addStatusEffect(effect);
    }
    
    public void removeEffect(RegistryEntry<StatusEffect> effectType, Predicate<IndirectPlayer> cancelPredicate) {
        scheduleUntil(
            (player) -> player.removeStatusEffect(effectType),
            cancelPredicate
        );
    }
    
    public void removeEffectOrThrow(RegistryEntry<StatusEffect> effectType) {
        this.getPlayerOrThrow().removeStatusEffect(effectType);
    }
    
    public void giveItem(ItemStack stack, BiConsumer<ServerPlayerEntity, ItemStack> ifFail, Predicate<IndirectPlayer> cancelPredicate) {
        scheduleUntil(
            (player) -> {
                boolean result = player.getInventory().insertStack(stack);
                if (!result) {
                    ifFail.accept(player, stack);
                }
            },
            cancelPredicate
        );
    }
    
    public boolean giveItemOrThrow(ItemStack stack, BiConsumer<ServerPlayerEntity, ItemStack> ifFail) {
        ServerPlayerEntity player = this.getPlayerOrThrow();
        
        boolean succeeded = player
            .getInventory()
            .insertStack(stack);
        
        if (!succeeded) {
            ifFail.accept(player, stack);
        }

        return succeeded;
    }
    
    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks, Predicate<IndirectPlayer> cancelPredicate) {
        TitleFadeS2CPacket packet = new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks);
        scheduleUntil(
            (player) -> player.networkHandler.sendPacket(packet),
            cancelPredicate
        );
    }
    
    public void setTitleTimesOrThrow(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        TitleFadeS2CPacket packet = new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks);
        
        this.getPlayerOrThrow()
            .networkHandler.sendPacket(packet);
    }
    
    public void sendTitle(Text title, Predicate<IndirectPlayer> cancelCondition) {
        TitleS2CPacket packet = new TitleS2CPacket(title);
        
        scheduleUntil(
            (player) -> player.networkHandler.sendPacket(packet)
            , cancelCondition);
    }
    
    public void sendTitleOrThrow(Text title) throws OfflinePlayerException {
        TitleS2CPacket packet = new TitleS2CPacket(title);
        this.getPlayerOrThrow()
            .networkHandler.sendPacket(packet);
    }
    
    public void sendSubtitle(Text subtitle, Predicate<IndirectPlayer> cancelCondition) {
        SubtitleS2CPacket packet = new SubtitleS2CPacket(subtitle);
        TitleS2CPacket titlePacket = new TitleS2CPacket(Text.empty());
        
        scheduleUntil(
            (player) -> {
                player.networkHandler.sendPacket(packet);
                player.networkHandler.sendPacket(titlePacket);
            }
            , cancelCondition);
    }
    
    public void sendSubtitleOrThrow(Text subtitle) throws OfflinePlayerException {
        SubtitleS2CPacket packet = new SubtitleS2CPacket(subtitle);
        this.getPlayerOrThrow()
            .networkHandler.sendPacket(packet);
    }
    
    public void sendMessage(Text chatMessage, Predicate<IndirectPlayer> cancelCondition) {
        scheduleUntil(
            (player) -> player.sendMessage(chatMessage)
            , cancelCondition);
    }
    
    public void sendMessageOrThrow(Text chatMessage) throws OfflinePlayerException {
        this.getPlayerOrThrow()
            .sendMessage(chatMessage);
    }
    
    public void sendOverlay(Text chatMessage, Predicate<IndirectPlayer> cancelCondition) {
        scheduleUntil(
            (player) -> player.sendMessage(chatMessage, true)
            , cancelCondition);
    }
    
    public void playSoundOrThrow(RegistryEntry.Reference<SoundEvent> event, SoundCategory category, float volume, float pitch) throws OfflinePlayerException {
        ServerPlayerEntity player = this.getPlayerOrThrow();
        player.networkHandler.sendPacket(
                new PlaySoundFromEntityS2CPacket(
                    event,
                    category,
                    player,
                    volume,
                    pitch,
                    player.getServerWorld().random.nextLong()
                )
            );
    }

    public void playSound(RegistryEntry.Reference<SoundEvent> event, SoundCategory category, float volume, float pitch, Predicate<IndirectPlayer> cancelCondition) {
        scheduleUntil(
            (player) -> player.networkHandler.sendPacket(
                new PlaySoundFromEntityS2CPacket(
                    event,
                    category,
                    player,
                    volume,
                    pitch,
                    player.getServerWorld().random.nextLong()
                )
            )
            , cancelCondition);
    }

    public void addToTeam(Team team, Predicate<IndirectPlayer> cancelCondition) {
        scheduleUntil(
            (p) -> this.server.getScoreboard().addScoreHolderToTeam(
                p.getNameForScoreboard(),
                team
            ),
            cancelCondition
        );
    }

    public void addToTeamOrThrow(Team team) {
        this.server.getScoreboard().addScoreHolderToTeam(
            getPlayerOrThrow().getNameForScoreboard(),
            team
        );
    }

    public void addToTeamNow(InternalTeam team) {
        this.server.getScoreboard().addScoreHolderToTeam(
            this.name,
            server.getScoreboard().getTeam(team.teamName)
        );
    }

    public void spoofAddPlayersToTeam(List<IndirectPlayer> targets, Team team, Predicate<IndirectPlayer> cancelCondition) {
        scheduleUntil(
            (p) ->
                targets.forEach((t) ->
                    p.networkHandler.sendPacket(
                        TeamS2CPacket.changePlayerTeam(team,
                            t.getLiteralName(),
                            TeamS2CPacket.Operation.ADD
                    )
                )
            ),
            cancelCondition
        );
    }

    public void spoofAddPlayersToTeamOrThrow(List<IndirectPlayer> targets, Team team) {
        targets.forEach((t) ->
           getPlayerOrThrow().networkHandler.sendPacket(
                TeamS2CPacket.changePlayerTeam(team,
                    t.getLiteralName(),
                    TeamS2CPacket.Operation.ADD
                )
            )
        );
    }

    public void sendOverlayOrThrow(Text chatMessage) throws OfflinePlayerException {
        this.getPlayerOrThrow()
            .sendMessage(chatMessage, true);
    }
    
    public void scheduleUntil(Consumer<ServerPlayerEntity> task, Predicate<IndirectPlayer> cancelCondition) {
        Optional<ServerPlayerEntity> sPlayer = this.getPlayer();
        
        if (sPlayer.isPresent()) {
            task.accept(sPlayer.get());
            
        } else if (!cancelCondition.test(this)) { // Don't bother scheduling if it should already be cancelled
            getShadow()
                .indirectPlayerManager
                .schedule(
                    new IndirectPlayerManager.IndirectPlayerTask(
                        this,
                        task,
                        cancelCondition
                    )
                );
        }
    }
    
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ComponentChanges.Builder builder = ComponentChanges.builder();
        builder.add(
            DataComponentTypes.ITEM_NAME,
            this.getName()
                .copy()
                .styled(
                    style -> style.withColor(Formatting.GRAY)
                )
        );
        builder.add(
            DataComponentTypes.PROFILE,
            new ProfileComponent(
                Optional.empty(),
                Optional.of(this.playerUUID),
                new PropertyMap()
            )
        );
        
        return new ItemStack(
            Registries.ITEM.getEntry(Items.PLAYER_HEAD),
            1,
            builder.build());
    }
    
    public void clearPlayerData(Predicate<IndirectPlayer> cancelCondition) {
        scheduleUntil(
            (player) -> {
                for (
                    AdvancementEntry advancement
                    :
                    Objects.requireNonNull(player.getServer())
                        .getAdvancementLoader()
                        .getAdvancements()
                ) {
                    AdvancementProgress advancementProgress = player
                        .getAdvancementTracker()
                        .getProgress(advancement);
                    if (advancementProgress.isAnyObtained()) {
                        for (String string : advancementProgress.getObtainedCriteria()) {
                            player
                                .getAdvancementTracker()
                                .revokeCriterion(advancement, string);
                        }
                    }
                }
                
                player.getInventory().clear();
                player.getEnderChestInventory().clear();
                player.setHealth(player.getMaxHealth());
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(5f);
                player.clearStatusEffects();
                player.setExperienceLevel(0);
                player.setExperiencePoints(0);
                
                AttributeContainer attributes = player.getAttributes();
                
                attributes.custom.values().forEach(EntityAttributeInstance::clearModifiers);
            },
            cancelCondition
        );
    }
    
    public class OfflinePlayerException extends IllegalStateException {
        private OfflinePlayerException() {
            super(IndirectPlayer.this.getLiteralName() + " could not execute the task as they are not online");
        }
    }
}
