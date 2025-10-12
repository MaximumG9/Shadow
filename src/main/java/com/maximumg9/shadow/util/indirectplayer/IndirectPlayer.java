package com.maximumg9.shadow.util.indirectplayer;


import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.AddHealthLink;
import com.maximumg9.shadow.modifiers.Modifier;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.roles.neutral.Spectator;
import com.maximumg9.shadow.screens.ItemRepresentable;
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
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This is meant to represent a player who existed at some time, even if the player does not exist now
 */
public class IndirectPlayer implements ItemRepresentable {
    public final UUID playerUUID;
    final MinecraftServer server;
    @Nullable
    public Role role;
    @Nullable
    public Roles originalRole;
    public ArrayList<Modifier> modifiers = new ArrayList<>();
    public boolean participating;
    public boolean frozen;
    public int chatMessageCooldown;
    public NbtCompound extraStorage;
    private int offlineTicks = Integer.MAX_VALUE;
    private Text name = null;
    @Nullable
    public AddHealthLink.Link link = null;
    
    public IndirectPlayer(ServerPlayerEntity base) {
        this.playerUUID = base.getUuid();
        this.server = base.server;
        this.role = new Spectator(this);
        this.participating = getShadow().state.phase != GamePhase.PLAYING;
        this.name = base.getName();
        this.extraStorage = new NbtCompound();
    }
    
    IndirectPlayer(MinecraftServer server, UUID uuid) {
        this.server = server;
        this.playerUUID = uuid;
        this.extraStorage = new NbtCompound();
    }
    
    IndirectPlayer(IndirectPlayer src) {
        this.playerUUID = src.playerUUID;
        this.server = src.server;
        this.role = src.role;
        this.modifiers = src.modifiers;
        this.participating = src.participating;
        this.frozen = src.frozen;
        this.name = src.getName();
        this.chatMessageCooldown = src.chatMessageCooldown;
        this.originalRole = src.originalRole;
        this.offlineTicks = src.offlineTicks;
        this.extraStorage = src.extraStorage.copy();
    }

    static UUID getUUIDForData(NbtCompound nbt) {
        return nbt.getUuid("playerUUID");
    }

    void readNbt(NbtCompound nbt) {
        this.frozen = nbt.getBoolean("frozen");
        this.participating = nbt.getBoolean("participating");

        if (nbt.contains("role", NbtElement.COMPOUND_TYPE)) {
            this.role = Role.load(nbt.getCompound("role"), this);
        } else {
            this.role = null;
        }

        if (nbt.contains("original_role", NbtElement.COMPOUND_TYPE)) {
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

        this.offlineTicks = nbt.getInt("offline_ticks");
        this.extraStorage = nbt.getCompound("extra_storage");
    }

    public Shadow getShadow() {
        return MiscUtil.getShadow(this.server);
    }

    NbtCompound writeNBT(NbtCompound nbt) {
        nbt.putUuid("playerUUID", this.playerUUID);
        nbt.putBoolean("frozen", this.frozen);
        nbt.putBoolean("participating", this.participating);
        
        if (this.role != null) {
            nbt.put("role", this.role.writeNbt(new NbtCompound()));
        }
        if (this.originalRole != null) {
            nbt.putString("original_role", this.originalRole.name);
        }
        
        NbtList list = new NbtList();
        list.addAll(this.modifiers.stream().map(modifier -> modifier.writeNbt(new NbtCompound())).toList());
        nbt.put("modifiers", list);
        
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
    }
    
    public int getOfflineTicks() {
        return this.offlineTicks;
    }
    
    public Text getName() {
        this.getPlayer().ifPresent((psPlayer) -> this.name = psPlayer.getName());
        if (name == null) {
            UserCache cache = this.server.getUserCache();
            if (cache != null) {
                Optional<GameProfile> profile = cache.getByUuid(this.playerUUID);
                this.name = profile.map(
                    gameProfile -> Text.literal(
                        gameProfile.getName()
                    )
                ).orElse(
                    Text.literal(
                        playerUUID.toString()
                    )
                );
            } else {
                this.name = Text.literal(
                    playerUUID.toString()
                );
            }
            
        }
        return this.name;
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
    
    public void damageNow(DamageSource source, float amount) {
        this.getPlayerOrThrow()
            .damage(source, amount);
    }
    
    public void giveEffect(StatusEffectInstance effect, Predicate<IndirectPlayer> cancelPredicate) {
        scheduleUntil(
            (player) -> player.addStatusEffect(effect),
            cancelPredicate
        );
    }
    
    public void giveEffectNow(StatusEffectInstance effect) {
        this.getPlayerOrThrow()
            .addStatusEffect(effect);
    }
    
    public void removeEffect(RegistryEntry<StatusEffect> effectType, Predicate<IndirectPlayer> cancelPredicate) {
        scheduleUntil(
            (player) -> player.removeStatusEffect(effectType),
            cancelPredicate
        );
    }
    
    public void removeEffectNow(RegistryEntry<StatusEffect> effectType) {
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
    
    public boolean giveItemNow(ItemStack stack, BiConsumer<ServerPlayerEntity, ItemStack> ifFail) {
        ServerPlayerEntity player = this.getPlayerOrThrow();
        
        boolean result = player
            .getInventory()
            .insertStack(stack);
        
        if (!result) {
            ifFail.accept(player, stack);
        }
        
        return this.getPlayerOrThrow()
            .getInventory()
            .insertStack(stack);
    }
    
    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks, Predicate<IndirectPlayer> cancelPredicate) {
        TitleFadeS2CPacket packet = new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks);
        scheduleUntil(
            (player) -> player.networkHandler.sendPacket(packet),
            cancelPredicate
        );
    }
    
    public void setTitleTimesNow(int fadeInTicks, int stayTicks, int fadeOutTicks) {
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
    
    public void sendTitleNow(Text title) throws OfflinePlayerException {
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
    
    public void sendSubtitleNow(Text subtitle) throws OfflinePlayerException {
        SubtitleS2CPacket packet = new SubtitleS2CPacket(subtitle);
        this.getPlayerOrThrow()
            .networkHandler.sendPacket(packet);
    }
    
    public void sendMessage(Text chatMessage, Predicate<IndirectPlayer> cancelCondition) {
        scheduleUntil(
            (player) -> player.sendMessage(chatMessage)
            , cancelCondition);
    }
    
    public void sendMessageNow(Text chatMessage) throws OfflinePlayerException {
        this.getPlayerOrThrow()
            .sendMessage(chatMessage);
    }
    
    public void sendOverlay(Text chatMessage, Predicate<IndirectPlayer> cancelCondition) {
        scheduleUntil(
            (player) -> player.sendMessage(chatMessage, true)
            , cancelCondition);
    }
    
    public void sendOverlayNow(Text chatMessage) throws OfflinePlayerException {
        this.getPlayerOrThrow()
            .sendMessage(chatMessage, true);
    }
    
    public void scheduleUntil(Consumer<ServerPlayerEntity> task, Predicate<IndirectPlayer> cancelCondition) {
        Optional<ServerPlayerEntity> sPlayer = this.getPlayer();
        
        if (sPlayer.isPresent()) {
            task.accept(sPlayer.get());
            
        } else if (cancelCondition.test(this)) { // Don't bother scheduling if it should already be cancelled
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
            super(IndirectPlayer.this.getName().getLiteralString() + " could not execute the task as they are not online");
        }
    }
}
