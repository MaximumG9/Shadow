package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.LifeweaverHeart;
import com.maximumg9.shadow.roles.Lifeweaver;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class GetHeart extends Ability {
    public GetHeart(IndirectPlayer player) {
        super(player);
    }

    private static final ItemStack ITEM_STACK = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);

    static {
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("Get Heart")
                .setStyle(Lifeweaver.STYLE)
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal("Get a heart as an item"),
                Ability.AbilityText()
            )
        );
    }

    public static final Identifier ATTR_ID = MiscUtil.shadowID("lifeweaver_max_health");

    @Override
    public Identifier getID() {
        return MiscUtil.shadowID("get_heart");
    }

    @Override
    public AbilityResult apply() {
        ServerPlayerEntity player = this.player.getPlayerOrThrow();

        float pastMaxHealth = player.getMaxHealth();
        if(pastMaxHealth > 2f) {
            EntityAttributeInstance attr = player
                .getAttributes()
                .getCustomInstance(
                    EntityAttributes.GENERIC_MAX_HEALTH
                );

            if(attr == null) {
                getShadow().ERROR("Players don't have a max health attribute (we're so cooked)");
                return AbilityResult.CLOSE;
            }

            ItemStack heart = NBTUtil.addID(
                new ItemStack(
                    Items.ENCHANTED_GOLDEN_APPLE
                ),
                LifeweaverHeart.ID
            );

            NBTUtil.applyCustomDataToStack(
                heart,
                (nbt) -> {
                    nbt.putDouble(LifeweaverHeart.HEALTH_INCREASE_KEY, 2.0);
                    return nbt;
                }
            );

            if(this.player.giveItemNow(
                heart,
                MiscUtil.DELETE
            )) {
                this.player.sendMessageNow(
                    Text.literal("Failed to get heart")
                        .styled(style -> style.withColor(Formatting.RED))
                );
                return AbilityResult.CLOSE;
            } else {
                player.currentScreenHandler.sendContentUpdates();

                EntityAttributeModifier modifier = attr.getModifier(ATTR_ID);
                if(modifier == null) {
                    attr.addPersistentModifier(new EntityAttributeModifier(
                        ATTR_ID,
                        -2.0,
                        EntityAttributeModifier.Operation.ADD_VALUE
                    ));
                } else {
                    double oldValue = modifier.value();
                    if(
                        modifier.operation() !=
                        EntityAttributeModifier.Operation.ADD_VALUE
                    ) {
                        getShadow().ERROR("Existing lifeweaver attribute modifier is not add value");
                    }
                    attr.overwritePersistentModifier(new EntityAttributeModifier(
                        ATTR_ID,
                        oldValue - 2.0,
                        EntityAttributeModifier.Operation.ADD_VALUE
                    ));
                }

                this.player.sendMessageNow(
                    Text.literal("Removed heart")
                        .styled(style -> style.withColor(Formatting.GREEN))
                );
                return AbilityResult.NO_CLOSE;
            }
        } else {
            this.player.sendMessageNow(
                Text.literal("Not enough health to remove another heart")
                    .styled(style -> style.withColor(Formatting.RED))
            );

            return AbilityResult.CLOSE;
        }
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
}
