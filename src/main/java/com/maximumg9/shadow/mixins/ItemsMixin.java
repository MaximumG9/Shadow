package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.Shadow;
import net.minecraft.component.Component;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(Items.class)
public class ItemsMixin {
    
    @Inject(method = "register(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/item/Item;)Lnet/minecraft/item/Item;", at = @At("HEAD"), cancellable = true)
    private static void register(RegistryKey<Item> key, Item item, CallbackInfoReturnable<Item> cir) {
        Function<Item.Settings, Item> modifiedItemConstructor = Shadow.modifiedItems.get(key.getValue());
        
        if (modifiedItemConstructor == null) {
            item.registryEntry = Registries.ITEM.createEntry(item);
            return;
        }
        
        Item.Settings settings = new Item.Settings();
        
        item.getComponents()
            .forEach((component) ->
                addComponent(settings, component)
            );
        
        settings.recipeRemainder(item.getRecipeRemainder());
        
        Item moddedItem = modifiedItemConstructor.apply(settings);
        
        moddedItem.registryEntry = Registries.ITEM.createEntry(moddedItem);
        
        moddedItem.requiredFeatures = item.getRequiredFeatures();
        
        if (moddedItem instanceof BlockItem) {
            ((BlockItem) moddedItem).appendBlocks(Item.BLOCK_ITEMS, moddedItem);
        }
        
        Registry.register(Registries.ITEM, key, moddedItem);
        
        cir.setReturnValue(moddedItem);
        cir.cancel();
    }
    
    @Unique
    private static <T> void addComponent(Item.Settings settings, Component<T> component) {
        settings.component(component.type(), component.value());
    }
}
