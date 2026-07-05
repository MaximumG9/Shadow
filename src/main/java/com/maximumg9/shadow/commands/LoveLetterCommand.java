package com.maximumg9.shadow.commands;


import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.villager.SendLoveLetter;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;
import static net.minecraft.command.argument.EntityArgumentType.entity;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LoveLetterCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("loveletter")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .then(
                    argument("target", entity())
                        .executes((ctx) -> {
                            Shadow shadow = getShadow(ctx.getSource().getServer());

                            IndirectPlayer iTarget;
                            try {
                                Entity sTarget = EntityArgumentType.getEntity(ctx,"target");
                                if(sTarget instanceof ServerPlayerEntity p) {
                                    iTarget = shadow.getIndirect(p);
                                } else {
                                    throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
                                }
                            } catch (CommandSyntaxException e) {
                                // Allow accepting and find the indirect target even if they are offline
                                EntitySelector selector = ctx.getArgument("target", EntitySelector.class);
                                if(selector.uuid == null) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
                                iTarget = shadow.indirectPlayerManager.get(selector.uuid);
                            }

                            if(iTarget == null) {
                                throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
                            }

                            Optional<SendLoveLetter> letterSender =
                                iTarget.role.getAbility(SendLoveLetter.ID)
                                    .map((ability) -> (SendLoveLetter) ability)
                                ;
                            if(letterSender.isEmpty()) {
                                throw LETTER_NOT_FOUND_EXCEPTION.create();
                            }

                            ServerPlayerEntity sSelf = ctx.getSource().getPlayer();
                            IndirectPlayer iSelf = shadow.getIndirect(sSelf);

                            if(letterSender.get().resolveLoveLetter(iSelf)) {
                                IndirectPlayer finalITarget = iTarget;
                                ctx.getSource().sendFeedback(
                                    () -> TextUtil.green("Accepted the love letter from ")
                                        .append(finalITarget.getName()),
                                    false
                                );
                                return Command.SINGLE_SUCCESS;
                            } else {
                                throw LETTER_NOT_FOUND_EXCEPTION.create();
                            }
                        })
                )
        );
    }

    public static final SimpleCommandExceptionType LETTER_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(
        Text.literal("Target player has sent no love letters to you")
    );

}
