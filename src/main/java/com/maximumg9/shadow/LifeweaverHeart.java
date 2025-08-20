package com.maximumg9.shadow;

import com.maximumg9.shadow.abilities.GetHeart;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public class LifeweaverHeart implements ItemUseCallback {
    public static final String HEALTH_INCREASE_KEY = "health_increase";
    public static final Identifier ID = MiscUtil.shadowID("lifeweaver_heart");

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(!(world instanceof ServerWorld)) return null;

        ItemStack stack = user.getStackInHand(hand);

        NbtCompound nbt = NBTUtil.getCustomData(stack);

        if(!nbt.contains(HEALTH_INCREASE_KEY, NbtElement.DOUBLE_TYPE)) {
            return null;
        } else {
            double healthIncrease = nbt.getDouble(HEALTH_INCREASE_KEY);
            EntityAttributeInstance instance = user.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

            if(instance == null) {
                return  null;
            }

            EntityAttributeModifier modifier = instance.getModifier(GetHeart.ATTR_ID);

            if(modifier == null) {
                instance.addPersistentModifier(
                    new EntityAttributeModifier(
                        GetHeart.ATTR_ID,
                        healthIncrease,
                        EntityAttributeModifier.Operation.ADD_VALUE
                    )
                );
            } else {
                if(modifier.operation() != EntityAttributeModifier.Operation.ADD_VALUE) {
                    getShadow(world.getServer()).ERROR("Existing lifeweaver attribute modifier is not add value");
                    return null;
                }
                instance.overwritePersistentModifier(
                    new EntityAttributeModifier(
                        GetHeart.ATTR_ID,
                        healthIncrease + modifier.value(),
                        EntityAttributeModifier.Operation.ADD_VALUE
                    )
                );
            }
            stack.decrement(1);
            return TypedActionResult.consume(stack);
        }
    }
}
