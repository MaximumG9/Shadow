package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.items.ParticipationEye;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.StartTicker;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;
import static net.minecraft.server.command.CommandManager.literal;

public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("$start")
                .then(
                    literal("force").executes(StartCommand::start)
                )
                .executes(StartCommand::checkAndStart)
        );
    }
    
    public static int checkAndStart(CommandContext<ServerCommandSource> ctx) {
        if (!check(ctx)) {
            ctx.getSource().sendError(Text.literal("Phase is not location selected"));
            return -1;
        }
        return start(ctx);
    }
    
    public static boolean check(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Shadow shadow = getShadow(server);
        
        return shadow.state.phase == GamePhase.LOCATION_SELECTED;
    }
    
    public static int start(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Shadow shadow = getShadow(server);
        
        shadow.addTickable(new StartTicker(shadow));
        shadow.addTickable(new StartTicker.GracePeriodTicker(shadow));
        
        for (IndirectPlayer player : shadow.getOnlinePlayers()) {
            ServerPlayerEntity entity = player.getPlayerOrThrow();
            
            player.clearPlayerData(CancelPredicates.NEVER_CANCEL);
            
            ItemStack eyeStack = new ItemStack(Items.ENDER_EYE);
            
            ParticipationEye.EnderEyeData data = new ParticipationEye.EnderEyeData(player.participating);
            
            data.write(eyeStack);
            
            NBTUtil.flagRestrictMovement(eyeStack);
            
            entity.giveItemStack(eyeStack);
        }
        
        return 0;
    }
}


