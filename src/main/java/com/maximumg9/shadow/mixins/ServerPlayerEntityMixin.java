package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.items.Eye;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.NBTUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @org.spongepowered.asm.mixin.Shadow
    @Final
    public MinecraftServer server;
    
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
    
    @Inject(method = "sendPickup", at = @At("HEAD"))
    private void pickupEnderEye(Entity item, int count, CallbackInfo ci) {
        Shadow shadow = getShadow(Objects.requireNonNull(item.getServer()));
        
        if (item instanceof ItemEntity) {
            List<Eye> eyesCopy = new ArrayList<>(shadow.state.eyes);
            for (Eye eye : eyesCopy) {
                if (eye.item().equals(item.getUuid())) {
                    eye.destroy(shadow);
                    shadow.state.eyes.remove(eye);
                }
            }
        }
    }
    
    @Inject(method = "onDisconnect", at = @At("HEAD"))
    public void onDisconnect(CallbackInfo ci) {
        Shadow shadow = getShadow(server);
        Text name = this.getName();
        shadow.addTickable(
            Delay.of(
                () -> {
                    if (
                        shadow
                            .getIndirect((ServerPlayerEntity) (Object) this)
                            .getOfflineTicks() >=
                            shadow.config.disconnectTime
                    ) {
                        shadow.broadcast(
                            name.copy().styled(style -> style.withColor(Formatting.YELLOW))
                                .append(
                                    Text.literal(
                                        " has been disconnected for too long"
                                    )
                                )
                        );
                        shadow.checkWin(this.uuid);
                    }
                },
                shadow.config.disconnectTime
            )
        );
    }
    
    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    public void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (NBTUtil.getCustomData(stack).getBoolean(NBTUtil.RESTRICT_MOVEMENT_KEY)) {
            cir.setReturnValue(null);
            cir.cancel();
            if (this.getInventory().insertStack(stack)) return;
            this.getInventory().setStack(0, stack);
        }
    }
}
