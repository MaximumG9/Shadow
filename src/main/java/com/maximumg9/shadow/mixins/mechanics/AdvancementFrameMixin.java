package com.maximumg9.shadow.mixins.mechanics;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(AdvancementFrame.class)
public class AdvancementFrameMixin {
    @Redirect(method = "getChatAnnouncementText", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    public Text changeName(ServerPlayerEntity instance) {
        Shadow shadow = getShadow(instance.server);
        if (shadow.state.phase != GamePhase.PLAYING) return instance.getDisplayName();
        
        return Text.literal("??????");
    }
}
