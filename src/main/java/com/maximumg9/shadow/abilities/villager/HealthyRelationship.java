package com.maximumg9.shadow.abilities.villager;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.AbilityResult;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

public class HealthyRelationship extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("healthy_relationship");
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = new ItemStack(Items.POPPY, 1);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.green("Healthy Relationship")
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal(" gain ")
                        .append(TextUtil.hearts(2.0f))
                        .append(" ")
                        .append(TextUtil.gray("Resistance I"))
                        .append(" ")
                        .append(TextUtil.red("Regeneration I")),
                TextUtil.red("BUT IF EITHER DIES, BOTH DIE").styled(style -> style.withBold(true))
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }

    private final IndirectPlayer partner;

    public HealthyRelationship(IndirectPlayer player, IndirectPlayer partner) {
        super(player);
        this.partner = partner;
    }

    @Override
    public void init() {
        super.init();
        this.player.giveEffect(
            new StatusEffectInstance(
                StatusEffects.RESISTANCE,
                -1,
                1 - 1,
                false,
                false
            ),
            CancelPredicates.cancelOnLostAbility(this)
        );
        this.player.giveEffect(
            new StatusEffectInstance(
                StatusEffects.REGENERATION,
                -1,
                1 - 1,
                false,
                false
            ),
            CancelPredicates.cancelOnLostAbility(this)
        );
    }

    @Override
    public void deInit() {
        super.deInit();
        this.player.removeEffect(
            StatusEffects.RESISTANCE,
            CancelPredicates.cancelOnPhaseChange(this.getShadow().state.phase)
        );
        this.player.removeEffect(
            StatusEffects.REGENERATION,
            CancelPredicates.cancelOnPhaseChange(this.getShadow().state.phase)
        );
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        this.partner.role.getAbility(HealthyRelationship.ID)
            .ifPresent((a) -> this.partner.role.removeAbility(a));
        this.partner.damage(
            this.getShadow().getServer().getOverworld().getDamageSources()
                .starve(),
            Float.MAX_VALUE,
            CancelPredicates.cancelOnPhaseChange(
                this.getShadow().state.phase
            )
        );
        this.partner.setDead(true);

        super.onDeath(damageSource);
    }

    @Override
    public Identifier getID() {
        return ID;
    }

    @Override
    public AbilityResult apply() {
        return AbilityResult.NO_CLOSE;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ItemStack stack = ITEM_STACK.copy();
        stack.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.green("Healthy Relationship (with ")
                .append(this.partner.getName())
                .append(")")
        );
        return stack;
    }
}
