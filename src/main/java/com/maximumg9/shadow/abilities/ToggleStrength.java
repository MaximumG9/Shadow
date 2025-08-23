package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.MiscUtil;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.util.List;
import java.util.function.Supplier;

public class ToggleStrength extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("toggle_strength");
    private static final ItemStack ITEM_STACK;
    
    static {
        ITEM_STACK = PotionContentsComponent.createStack(Items.POTION, Potions.LONG_STRENGTH);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("Toggle Strength")
                .styled(style -> style.withColor(Formatting.GOLD))
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("Toggle ")
                    .append(Text.literal("Strength I").styled(style -> style.withColor(Formatting.RED))),
                TextUtil.gray("During the night you also get ")
                    .append(Text.literal("Haste II").styled(style -> style.withColor(Formatting.GOLD)))
                    .append(TextUtil.gray(" and "))
                    .append(Text.literal("Speed II").styled(style -> style.withColor(Formatting.AQUA)))
                ,
                AbilityText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
    }
    
    private boolean hasStrength = false;
    
    public ToggleStrength(IndirectPlayer player) {
        super(player);
    }
    
    public List<Supplier<AbilityFilterResult>> getFilters() {
        return List.of(
            () -> {
                if (getShadow().isGracePeriod())
                    return AbilityFilterResult.FAIL("You cannot use this ability in Grace Period.");
                return AbilityFilterResult.PASS();
            }
        );
    }
    
    @Override
    public Identifier getID() { return ID; }
    
    @Override
    public AbilityResult apply() {
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
            
            this.player.sendMessageNow(TextUtil.success("Turned strength on."));
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
            this.player.sendMessageNow(TextUtil.success("Turned strength off."));
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
    
    @Override
    public void onNight() {
        if (hasStrength) {
            this.player.giveEffect(
                new StatusEffectInstance(
                    StatusEffects.HASTE,
                    -1,
                    4,
                    false,
                    false,
                    true
                ),
                CancelPredicates.IS_DAY
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
        super.onNight();
    }
    
    @Override
    public void onDay() {
        this.player.removeEffect(
            StatusEffects.SPEED,
            CancelPredicates.NEVER_CANCEL
        );
        this.player.removeEffect(
            StatusEffects.HASTE,
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
        super.onDay();
    }
    
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
}
