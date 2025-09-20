package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.abilities.PoseidonsTrident;
import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public abstract @NotNull ItemStack getWeaponStack();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "TAIL"))
    private void areItemsDifferent(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if(cir.getReturnValue() == null) return;
        cir.getReturnValue().entrySet().removeIf((entry) -> {
            ItemStack stack = entry.getValue();
            if(NBTUtil.hasID(stack, PoseidonsTrident.ID)) {
                entry.setValue(handleTrident(entry.getKey(),stack));
            }
            return NBTUtil.getCustomData(stack).getBoolean(NBTUtil.INVISIBLE_KEY);
        });
    }

    @Redirect(method = "getEquipmentChanges",at= @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    public boolean dontUpdateAttributeModifiers(ItemStack oldStack) {
        return oldStack.isEmpty() || NBTUtil.getCustomData(oldStack).getBoolean(NBTUtil.DISABLE_ATTRIBUTES_KEY);
    }

    @Inject(method = "disablesShield",at=@At("HEAD"), cancellable = true)
    public void disablesShield(CallbackInfoReturnable<Boolean> cir) {
        if(this.getWeaponStack().getItem() instanceof TridentItem) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private ItemStack handleTrident(EquipmentSlot slot, ItemStack trident) {
        if(slot == EquipmentSlot.MAINHAND) {
            World world = this.getWorld();
            if(world instanceof ServerWorld sWorld) {
                RegistryEntry<Enchantment> riptide = sWorld
                    .getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT)
                    .getEntry(Enchantments.RIPTIDE)
                    .orElseThrow();
                trident.apply(
                    DataComponentTypes.ENCHANTMENTS,
                    ItemEnchantmentsComponent.DEFAULT,
                    (enchs) ->
                        addRemovingConflicts(enchs,riptide,1)
                );
            }
        } else if(slot == EquipmentSlot.OFFHAND) {
            World world = this.getWorld();
            if(world instanceof ServerWorld sWorld) {
                RegistryEntry<Enchantment> loyalty = sWorld
                    .getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT)
                    .getEntry(Enchantments.LOYALTY)
                    .orElseThrow();
                trident.apply(
                    DataComponentTypes.ENCHANTMENTS,
                    ItemEnchantmentsComponent.DEFAULT,
                    (enchs) ->
                        addRemovingConflicts(enchs,loyalty,3)
                );
            }
        } else {
            if(!this.getWorld().isClient) {
                trident.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,true);
            }
        }

        return trident;
    }

    @Unique
    private static ItemEnchantmentsComponent addRemovingConflicts(ItemEnchantmentsComponent component, RegistryEntry<Enchantment> newEnch, int level) {
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(component);
        builder.remove(
            (existingEnch) ->
                !Enchantment.canBeCombined(
                    existingEnch,
                    newEnch
                )
        );
        builder.add(newEnch,level);
        return builder.build();
    }
}
