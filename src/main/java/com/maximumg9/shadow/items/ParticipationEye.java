package com.maximumg9.shadow.items;

import com.maximumg9.shadow.util.ItemData;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Unique;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public class ParticipationEye implements ItemUseCallback {
    @Unique
    public static final Identifier ID = MiscUtil.shadowID("participation_eye");
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld)) return null;
        
        ItemStack item = user.getStackInHand(hand);
        EnderEyeData data = EnderEyeData.read(item);
        
        EnderEyeData newData = new EnderEyeData(!data.participating);
        newData.write(item);
        
        getShadow(world.getServer())
            .getIndirect((ServerPlayerEntity) user)
            .participating = newData.participating;
        
        return TypedActionResult.success(item);
    }
    
    public record EnderEyeData(boolean participating) implements ItemData {
        
        private static final Text PARTICIPATING_TEXT = TextUtil.green("Participating");
        private static final Text NOT_PARTICIPATING_TEXT = TextUtil.red("Not Participating");
        
        public static EnderEyeData read(ItemStack stack) {
            NbtCompound customData = NBTUtil.getCustomData(stack);
            
            boolean participating = customData.getBoolean("participating");
            
            return new EnderEyeData(participating);
        }
        
        @Override
        public void write(ItemStack stack) {
            NBTUtil.applyCustomDataToStack(stack, (compound) -> {
                compound.putBoolean("participating", participating);
                
                return compound;
            });
            
            NBTUtil.addID(stack, ID);
            
            stack.apply(DataComponentTypes.ITEM_NAME, Text.literal("This text should never appear"), (text) -> {
                if (participating) {
                    return PARTICIPATING_TEXT;
                } else {
                    return NOT_PARTICIPATING_TEXT;
                }
            });
        }
    }
}


