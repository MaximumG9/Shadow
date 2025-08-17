package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.Shadow;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Objects;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;
import static net.minecraft.server.command.CommandManager.literal;

public class ModifiersCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("$modifiers")
                .executes((ctx) -> {
                    ServerCommandSource src = ctx.getSource();
                    
                    if (!src.isExecutedByPlayer()) { return -1; }
                    
                    Shadow shadow = getShadow(src.getServer());
                    shadow.config.modifierManager.showModifierListIndex(Objects.requireNonNull(src.getPlayer()), src.hasPermissionLevel(3));
                    
                    ctx.getSource().sendFeedback(
                        () -> Text.literal("Opened Modifier Menu"),
                        false
                    );
                    
                    return 2;
                })
        );
    }
}
