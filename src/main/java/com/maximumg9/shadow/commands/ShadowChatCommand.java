package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ShadowChatCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> cmd = dispatcher.register(
            literal("shadowchat")
                .then(
                    argument("message", string())
                        .executes(ShadowChatCommand::sendShadowChat)
                )
        );
        dispatcher.register( literal("sc").redirect(cmd) );
        dispatcher.register( literal("$shadowchat").redirect(cmd) );
        dispatcher.register( literal("$sc").redirect(cmd) );
    }

    private static int sendShadowChat(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        MinecraftServer server = ctx.getSource().getServer();
        Shadow shadow = getShadow(server);
        if(!ctx.getSource().isExecutedByPlayer()) {
            ctx.getSource().sendError(Text.literal("You are not a player").styled(style -> style.withColor(Formatting.RED)));
            return 0;
        }

        IndirectPlayer player = shadow.getIndirect(ctx.getSource().getPlayerOrThrow());

        if(player.role == null || player.role.getFaction() != Faction.SHADOW) {
            ctx.getSource().sendError(Text.literal("You are not a shadow").styled(style -> style.withColor(Formatting.RED)));
            return 0;
        }

        String message = getString(ctx,"message");

            shadow.getAllPlayers().stream().filter(
                (p) ->
                p.role != null && p.role.getFaction() == Faction.SHADOW
            ).forEach((p) ->
                p.sendMessage(
                    Text.literal("[Shadow Chat]")
                        .styled(style -> style.withColor(Formatting.DARK_RED))
                        .append(Text.literal(" <"))
                        .append(player.getName().copy().setStyle(player.role.getStyle()))
                        .append(Text.literal("> "))
                        .append(Text.literal(message)),
                    CancelPredicates.cancelOnPhaseChange(shadow.state.phase)
                )
            );

        return 1;
    }
}
