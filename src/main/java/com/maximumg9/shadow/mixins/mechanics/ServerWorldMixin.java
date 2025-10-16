package com.maximumg9.shadow.mixins.mechanics;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Unique
    private double fractionalTime = 0;
    
    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    public void setTimeOfDay(ServerWorld instance, long timeOfDay) {
        Shadow shadow = getShadow(instance.getServer());
        if (shadow.state.phase != GamePhase.PLAYING) {
            shadow.setSilentDay();
        }
        
        if (timeOfDay % 24000 < 13000 && shadow.isNight()) {
            shadow.setDay();
        }
        
        if (timeOfDay % 24000 > 13000 && !shadow.isNight()) {
            shadow.setNight();
        }
        
        if (shadow.isNight()) {
            fractionalTime += shadow.config.additionalTimePerTickDuringNight;
            
            timeOfDay += MathHelper.floor(fractionalTime);
            
            fractionalTime = fractionalTime % 1;
        }
        
        instance.setTimeOfDay(timeOfDay);
    }
}
