package com.maximumg9.shadow.abilities.villager;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.AbilityResult;
import com.maximumg9.shadow.util.*;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.util.Optional;

public class ToxicRelationship extends Ability {

    public static final Identifier ID = MiscUtil.shadowID("toxic_relationship");
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = new ItemStack(Items.WITHER_ROSE, 1);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.withColour("Toxic Relationship", Formatting.BLACK)
        );
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }

    private final boolean victim;
    private final IndirectPlayer partner;
    private long ticksRemaining = -1;

    public ToxicRelationship(IndirectPlayer player, IndirectPlayer partner, boolean victim) {
        super(player);
        this.victim = victim;
        this.partner = partner;
    }

    @Override
    public void init() {
        this.ticksRemaining = this.getShadow().config.toxicRelationshipLength;
        super.init();
    }

    @Override
    public void tick() {
        super.tick();
        if(!(this.victim && !this.player.isLiving())) {
            if(this.ticksRemaining > 0) {
                this.ticksRemaining--;
                if(ticksRemaining == 0) {
                    this.player.role.removeAbility(this);
                }
            }
        }
    }

    @Override
    public Optional<Boolean> overrideWin(WinState state) {
        if(this.victim) {
            return this.player.isLiving() ? Optional.empty() : Optional.of(false);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deInit() {
        super.deInit();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        if(!this.victim) {
            this.partner.damage(
                this.getShadow().getServer().getOverworld().getDamageSources()
                    .starve(),
                Float.MAX_VALUE,
                CancelPredicates.cancelOnPhaseChange(
                    this.getShadow().state.phase
                )
            );
        }
    }

    @Override
    public boolean preserveOnDeath() {
        return true;
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public AbilityResult apply() {
        return AbilityResult.NO_CLOSE;
    }

    private LoreComponent getLore() {
        String time = TimeUtil.ticksToText(this.ticksRemaining,false);
        if(this.victim) {
            return MiscUtil.makeLore(
                TextUtil.red("YOU MUST SURVIVE THE NEXT " + time + " OR ELSE YOU LOSE"),
                TextUtil.red("IF YOUR PARTNER (WHO IS NOT A VILLAGER)")
                    .append(this.partner.getName())
                    .append(" DIES YOU DIE AND LOSE")
            );
        } else {
            return MiscUtil.makeLore(
                TextUtil.red("Your partner ")
                    .append(this.partner.getName())
                    .append(" knows you are not a villager"),
                TextUtil.red(" but for the next " + time)
                    .append(" if they die, they lose"),
                TextUtil.red(" and if you die, they die and lose")
            );
        }
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ItemStack stack = ITEM_STACK.copy();
        stack.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.withColour("Toxic Relationship (with ", Formatting.BLACK)
                .append(this.partner.getName())
                .append(")")
        );
        stack.set(
            DataComponentTypes.LORE,
            getLore()
        );
        return stack;
    }
}
