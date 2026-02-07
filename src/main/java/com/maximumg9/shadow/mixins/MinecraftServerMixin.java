package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Formatting;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ShadowProvider {
    @Unique
    private Shadow shadow;
    
    @Override
    public Shadow shadow$getShadow() {
        return this.shadow;
    }
    
    @Inject(method = "<init>", at = @At("CTOR_HEAD"))
    public void init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        this.shadow = new Shadow((MinecraftServer) (Object) this);
        this.shadow.startup();
    }

    @Inject(method = "loadWorld", at = @At(
        value = "TAIL"
    ))
    public void loadWorld(CallbackInfo ci) {
        shadow.onWorldLoad();
    }
    
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.shadow.tick();
    }
}
