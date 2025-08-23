package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.Shadow;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @org.spongepowered.asm.mixin.Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;
    
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/AdvancementCommand;register(Lcom/mojang/brigadier/CommandDispatcher;)V"))
    public void init(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci) {
        Shadow.registerCommands(this.dispatcher, commandRegistryAccess);
    }
}
