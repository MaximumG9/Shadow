package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.util.NBTUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket.class)
public class EntityEquipmentUpdateS2CPacketMixin {
    @Mutable
    @Shadow
    @Final
    private List<Pair<EquipmentSlot, ItemStack>> equipmentList;
    
    @Inject(method = "<init>(ILjava/util/List;)V", at = @At("TAIL"))
    public void init(int entityId, List<Pair<EquipmentSlot, ItemStack>> equipmentList, CallbackInfo ci) {
        ArrayList<Pair<EquipmentSlot, ItemStack>> newList = new ArrayList<>(equipmentList);
        newList.replaceAll(
            pair ->
                pair.mapSecond(
                    item ->
                        NBTUtil.getCustomData(item)
                            .getBoolean(NBTUtil.INVISIBLE_KEY) ? ItemStack.EMPTY : item
                )
        );
        
        this.equipmentList = newList;
    }
}
