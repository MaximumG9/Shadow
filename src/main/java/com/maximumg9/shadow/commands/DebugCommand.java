package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.items.Eye;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;

import java.util.Objects;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DebugCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("$debug")
                .then(
                    literal("setRole")
                        .requires((source) -> source.hasPermissionLevel(3))
                        .then(
                            argument("player", player())
                                .then(
                                    argument("role", string())
                                        .suggests(Roles::suggest)
                                        .executes((ctx) -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                                            Roles role = Roles.getRole(ctx, "role");
                                            Shadow shadow = getShadow(ctx.getSource().getServer());

                                            IndirectPlayer indirectPlayer = shadow.getIndirect(player);

                                            if (indirectPlayer.role != null) {
                                                indirectPlayer.role.deInit();
                                            }

                                            indirectPlayer.role = role.factory.makeRole(indirectPlayer);

                                            indirectPlayer.role.init();


                                            ctx.getSource().sendFeedback(
                                                () ->
                                                Text.literal("Set ")
                                                    .append(player.getName())
                                                    .append(Text.literal("'s Role to "))
                                                    .append(
                                                        Objects.requireNonNull(indirectPlayer.role)
                                                            .getName()
                                                    ),
                                            false
                                            );

                                            return 1;
                                        })
                                )
                        )
                )
                .then(
                    literal("setPhase")
                        .requires((source) -> source.hasPermissionLevel(3))
                        .then(
                            argument("phase", string())
                                .suggests(GamePhase::suggest)
                                .executes((ctx) -> {
                                    GamePhase phase = GamePhase.getPhase(ctx, "phase");
                                    Shadow shadow = getShadow(ctx.getSource().getServer());
                                    
                                    shadow.state.phase = phase;
                                    shadow.saveAsync();
                                    
                                    ctx.getSource().sendFeedback(() -> Text.of("Set Game Phase to " + phase.name()), false);
                                    
                                    return 1;
                                })
                        )
                )
                .then(
                    literal("sampleHeightmap")
                        .requires((source) -> source.hasPermissionLevel(3))
                        .executes((ctx) -> {
                            BlockPos position = BlockPos.ofFloored(ctx.getSource().getPosition());
                            
                            int x = position.getX();
                            int z = position.getZ();
                            
                            int sample = ctx.getSource().getWorld().getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z)).sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x, z);
                            
                            ctx.getSource().sendFeedback(() -> Text.of("Heightmap gives: " + sample), false);
                            
                            return 1;
                        })
                )
                .then(
                    literal("currentRoles")
                        .requires((source) -> {
                            if (source.hasPermissionLevel(3)) {
                                return true;
                            }
                            IndirectPlayer player = getShadow(source.getServer()).getIndirect(source.getPlayer());
                            return player.role != null && player.role.getFaction() == Faction.SPECTATOR;
                        })
                        .executes((ctx) -> {
                            Shadow shadow = getShadow(ctx.getSource().getServer());
                            
                            MutableText response = Text.literal("Roles: \n");
                            
                            shadow.indirectPlayerManager.getAllPlayers().forEach(
                                player ->
                                    response
                                        .append(player.getName())
                                        .append(Text.literal(": ")).setStyle(Style.EMPTY)
                                        .append(player.role != null ?
                                            player.role.getName() :
                                            Text.literal("null").styled(style -> style.withColor(Formatting.GRAY))
                                        )
                                        .append(Text.literal("\n"))
                            );
                            
                            ctx.getSource().sendFeedback(() -> response, false);
                            
                            return 1;
                        })
                )
                .then(
                    literal("eyes")
                        .requires((source) -> source.hasPermissionLevel(3))
                        .executes((ctx) -> {
                            StringBuilder text = new StringBuilder("Eyes: ");
                            for (Eye eye : getShadow(ctx.getSource().getServer()).state.eyes) {
                                text.append(eye.toString()).append(", ");
                            }
                            
                            ctx.getSource().sendFeedback(() -> Text.literal(text.toString()), false);
                            
                            return 1;
                        })
                )
        );
    }
}
