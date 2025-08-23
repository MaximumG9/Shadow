package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;
    
    @Redirect(method = "method_53637", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    public void redirectBroadcast(PlayerManager instance, Text message, boolean overlay) {
        com.maximumg9.shadow.Shadow shadow = getShadow(this.owner.server);
        if (shadow.state.phase != GamePhase.PLAYING) {
            instance.broadcast(message, overlay);
            return;
        }
        IndirectPlayer player = shadow.getIndirect(this.owner);
        
        if (player.role != null && player.role.getFaction() != Faction.SPECTATOR) {
            instance.broadcast(message, overlay);
        }
    }
}
