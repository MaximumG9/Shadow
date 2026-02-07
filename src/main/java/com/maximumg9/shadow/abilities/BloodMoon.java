package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class BloodMoon extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("blood_moon");
    private static final ItemStack ITEM_STACK;
    private static final Identifier HP_ATTR_ID = MiscUtil.shadowID("blood_moon_max_health");
    private static final double healthIncrease = 6;
    private double totalHealthIncrease;

    static {
        ITEM_STACK = new ItemStack(Items.REDSTONE, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("During Night, gain ").append(TextUtil.hearts(3)).append(" additional hearts per player YOU kill."),
                TextUtil.gray("These hearts are permanent, but only take effect during Night."),
                PassiveText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.red("Blood Moon")
        );
    }

    public BloodMoon(IndirectPlayer player) { super(player); }

    @Override
    public void onPlayerKill() {
        if (!getShadow().isNight()) return;
        EntityAttributeInstance instance = player.getPlayerOrThrow().getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (instance == null) return;

        EntityAttributeModifier modifier = instance.getModifier(HP_ATTR_ID);
        totalHealthIncrease += healthIncrease;
        if (modifier == null) {
            instance.addPersistentModifier(
                new EntityAttributeModifier(
                    HP_ATTR_ID,
                    totalHealthIncrease,
                    EntityAttributeModifier.Operation.ADD_VALUE
                )
            );
        } else {
            if (modifier.operation() != EntityAttributeModifier.Operation.ADD_VALUE) {
                getShadow().ERROR("Fuck off (Blood Moon Health Operation Not Add Value)");
                return;
            }
            instance.overwritePersistentModifier(
                new EntityAttributeModifier(
                    HP_ATTR_ID,
                    totalHealthIncrease,
                    EntityAttributeModifier.Operation.ADD_VALUE
                )
            );
        }
    }

    @Override
    public void onDay() {
        super.onDay();
        player.scheduleUntil(
            (p) -> {
                EntityAttributeInstance instance = player.getPlayerOrThrow().getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (instance == null) {
                    getShadow().ERROR("People are unhealthy (max health doesn't exist somehow lol) \"we're so cooked\" - osmii");
                    return;
                }

                EntityAttributeModifier modifier = instance.getModifier(HP_ATTR_ID);
                if (modifier != null) {
                    totalHealthIncrease = modifier.value();
                    if (modifier.operation() != EntityAttributeModifier.Operation.ADD_VALUE) {
                        getShadow().ERROR("Fuck off (Blood Moon Health Operation Not Add Value)");
                        return;
                    }
                    instance.overwritePersistentModifier(
                        new EntityAttributeModifier(
                            HP_ATTR_ID,
                            0,
                            EntityAttributeModifier.Operation.ADD_VALUE
                        )
                    );
                }
            }
        , CancelPredicates.cancelOnLostAbility(this).or(CancelPredicates.IS_NIGHT));
    }

    @Override
    public void onNight() {
        super.onNight();
        player.scheduleUntil(
            (p) -> {
                EntityAttributeInstance instance = p.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (instance == null) {
                    getShadow().ERROR("People are unhealthy (max health doesn't exist somehow lol) \"we're so cooked\" - osmii");
                    return;
                }

                EntityAttributeModifier modifier = instance.getModifier(HP_ATTR_ID);
                if (modifier != null) {
                    if (modifier.operation() != EntityAttributeModifier.Operation.ADD_VALUE) {
                        getShadow().ERROR("Fuck off (Blood Moon Health Operation Not Add Value)");
                        return;
                    }
                    instance.overwritePersistentModifier(
                        new EntityAttributeModifier(
                            HP_ATTR_ID,
                            totalHealthIncrease,
                            EntityAttributeModifier.Operation.ADD_VALUE
                        )
                    );
                }
            },
            CancelPredicates.cancelOnLostAbility(this).or(CancelPredicates.IS_DAY)
        );
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public AbilityResult apply() { return AbilityResult.NO_CLOSE; }
}
