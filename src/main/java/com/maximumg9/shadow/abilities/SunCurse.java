package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

// could this have been made as a togglestrength extension?
public class SunCurse extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("sun_curse");
    private static final ItemStack ITEM_STACK_DAY;
    private static final ItemStack ITEM_STACK_NIGHT;

    static {
        ITEM_STACK_DAY = PotionContentsComponent.createStack(Items.POTION, Potions.LONG_WEAKNESS);
        ITEM_STACK_DAY.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("You cannot enable Strength during the day."),
                PassiveText()
            )
        );
        ITEM_STACK_DAY.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.withColour("Sun's Curse", Formatting.DARK_RED)
        );
        ITEM_STACK_DAY.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK_DAY);

        ITEM_STACK_NIGHT = PotionContentsComponent.createStack(Items.POTION, Potions.LONG_STRENGTH);
        ITEM_STACK_NIGHT.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.gold("Toggle Strength")
        );
        ITEM_STACK_NIGHT.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("Toggle ")
                    .append(TextUtil.red("Strength I")),
                TextUtil.gray("During the night you also get ")
                    .append(TextUtil.gold("Haste II"))
                    .append(TextUtil.gray(" and "))
                    .append(TextUtil.withColour("Speed II",Formatting.AQUA))
                ,
                AbilityText()
            )
        );
        ITEM_STACK_NIGHT.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK_NIGHT);
    }

    private boolean hasStrength = false;

    public SunCurse(IndirectPlayer player) { super(player); }

    @Override
    public void init() {
        if (player.role.hasAbility(ToggleStrength.ID)) getShadow().addTickable(Delay.instant(() -> player.role.removeAbility(player.role.getAbility(ToggleStrength.ID).get())));
    }

    @Override
    public boolean getToggled() { return hasStrength; }

    @Override
    public void onDay() {
        this.player.removeEffect(
            StatusEffects.STRENGTH,
            CancelPredicates.NEVER_CANCEL
        );
        this.player.removeEffect(
            StatusEffects.SPEED,
            CancelPredicates.NEVER_CANCEL
        );
        this.player.removeEffect(
            StatusEffects.HASTE,
            CancelPredicates.NEVER_CANCEL
        );
        hasStrength = false;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        if (!getShadow().isNight()) return ITEM_STACK_DAY.copy();
        return ITEM_STACK_NIGHT;
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public AbilityResult apply() {
        if (!getShadow().isNight()) return AbilityResult.NO_CLOSE;
        hasStrength = !hasStrength;
        if (hasStrength) {
            this.player.giveEffectNow(
                new StatusEffectInstance(
                    StatusEffects.STRENGTH,
                    -1,
                    0,
                    false,
                    false,
                    true
                )
            );
            if (this.player.getShadow().isNight()) {
                this.player.giveEffectNow(
                    new StatusEffectInstance(
                        StatusEffects.HASTE,
                        -1,
                        4,
                        false,
                        false,
                        true
                    )
                );
                this.player.giveEffectNow(
                    new StatusEffectInstance(
                        StatusEffects.SPEED,
                        -1,
                        1,
                        false,
                        false,
                        true
                    )
                );
            }

            this.player.sendMessageNow(TextUtil.green("Turned strength on."));
        } else {
            this.player.removeEffectNow(StatusEffects.STRENGTH);

            if (this.player.getShadow().isNight()) {
                this.player.removeEffect(
                    StatusEffects.HASTE,
                    CancelPredicates.NEVER_CANCEL
                );
                this.player.removeEffect(
                    StatusEffects.SPEED,
                    CancelPredicates.NEVER_CANCEL
                );
                this.player.giveEffect(
                    new StatusEffectInstance(
                        StatusEffects.HASTE,
                        -1,
                        1,
                        false,
                        false,
                        true
                    ),
                    CancelPredicates.IS_DAY
                );
            }
            this.player.sendMessageNow(TextUtil.green("Turned strength off."));
        }
        return AbilityResult.CLOSE;
    }

    @Override
    public void deInit() {
        this.player.removeEffect(
            StatusEffects.STRENGTH,
            CancelPredicates.NEVER_CANCEL
        );
        this.player.removeEffect(
            StatusEffects.SPEED,
            CancelPredicates.NEVER_CANCEL
        );
        this.player.removeEffect(
            StatusEffects.HASTE,
            CancelPredicates.NEVER_CANCEL
        );
    }
}
