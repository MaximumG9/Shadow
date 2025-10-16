package com.maximumg9.shadow.mixins.mechanics;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.ObfuscateRole;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.neutral.Spectator;
import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @org.spongepowered.asm.mixin.Shadow
    @Final
    public MinecraftServer server;
    
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
    
    @SuppressWarnings("UnusedReturnValue")
    @org.spongepowered.asm.mixin.Shadow
    public abstract boolean changeGameMode(GameMode gameMode);
    
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "onDeath", at = @At("HEAD"))
    public void modifyDeathMessage(DamageSource damageSource, CallbackInfo ci) {
        Shadow shadow = getShadow(this.server);
        
        GameRules.BooleanRule showDeathMessage = this.getWorld().getGameRules().get(GameRules.SHOW_DEATH_MESSAGES);
        if (shadow.state.phase == GamePhase.PLAYING) {
            showDeathMessage.set(false, this.server);
            
            MutableText name = Team.decorateName(this.getScoreboardTeam(), this.getName());
            
            IndirectPlayer iPlayer = shadow.getIndirect((ServerPlayerEntity) (Object) this);
            
            Style factionStyle = iPlayer.role == null ? Style.EMPTY : iPlayer.role.getFaction().name.getStyle();
            
            // @TODO test this code with a working ability that applies the hide role flag
            if (iPlayer.extraStorage.contains(ObfuscateRole.HIDE_ROLE_KEY, NbtElement.INT_TYPE)) {
                name
                    .setStyle(Style.EMPTY.withColor(Formatting.GRAY))
                    .append(Text.of(" died. They were a "))
                    .append(
                        TextUtil.gray("aaaaaaa").styled(style -> style.withObfuscated(true))
                    );
                
                this.server.getPlayerManager().getPlayerList().forEach((player) -> {
                    if (
                        shadow.getIndirect(player).role.getFaction().ordinal() ==
                            iPlayer.extraStorage.getInt(ObfuscateRole.HIDE_ROLE_KEY)
                            || shadow.getIndirect(player).role.getFaction() == Faction.SPECTATOR) {
                        player.sendMessage(
                            Text.literal("").
                                append(name)
                                .append(
                                    TextUtil.gray(" (")
                                )
                                .append(
                                    iPlayer.role == null ?
                                        TextUtil.red("Null") :
                                        iPlayer.role.getName()
                                )
                                .append(
                                    TextUtil.gray(")")
                                )
                                .append(TextUtil.gray("."))
                        );
                    } else {
                        player.sendMessage(
                            name.copy()
                                .append(TextUtil.gray("."))
                        );
                    }
                });
            } else {
                name
                    .setStyle(factionStyle)
                    .append(Text.of(" died. They were " + iPlayer.role.aOrAn() + " "))
                    .append(
                        iPlayer.role == null ?
                            TextUtil.red("Null") :
                            iPlayer.role.getName()
                    )
                    .append(Text.literal(".").setStyle(factionStyle));
                
                shadow.broadcast(name);
            }
        } else {
            showDeathMessage.set(true, this.server);
        }
    }
    
    @Inject(method = "onDeath", at = @At("TAIL"))
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        Shadow shadow = getShadow(this.server);
        
        IndirectPlayer player = shadow.getIndirect((ServerPlayerEntity) ((Object) this));
        
        if (player.role != null) player.role.onDeath();
        
        player.role = new Spectator(player);
        
        shadow.checkWin(null);
        
        player.getPlayerOrThrow().setHealth(0f);
    }
    
    @Inject(method = "onSpawn", at = @At("TAIL"))
    public void onSpawn(CallbackInfo ci) {
        Shadow shadow = getShadow(this.server);
        IndirectPlayer iPlayer = shadow.getIndirect((ServerPlayerEntity) (Object) this);
        if (iPlayer.role != null) {
            if (iPlayer.role.getFaction() == Faction.SPECTATOR) {
                this.changeGameMode(GameMode.SPECTATOR);
            }
        }
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    public void onDisconnect(CallbackInfo ci) {
        Shadow shadow = getShadow(server);
        Text name = this.getName();
        shadow.addTickable(
            Delay.of(
                () -> {
                    if (
                        shadow
                            .getIndirect((ServerPlayerEntity) (Object) this)
                            .getOfflineTicks() >=
                            shadow.config.disconnectTime
                    ) {
                        shadow.broadcast(
                            name.copy().styled(style -> style.withColor(Formatting.YELLOW))
                                .append(
                                    Text.literal(
                                        " has been disconnected for too long"
                                    )
                                )
                        );
                        shadow.checkWin(this.uuid);
                    }
                },
                shadow.config.disconnectTime
            )
        );
    }
}
