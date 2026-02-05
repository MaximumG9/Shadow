package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.abilities.filters.Filters;
import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

import java.util.List;

public class Haunt extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("haunt");
    private static final ItemStack ITEM_STACK;
    private static final int COOLDOWN_TIME = 3 * 60 * 20;
    private boolean haunting = false;

    static {
        ITEM_STACK = new ItemStack(Items.GRAY_STAINED_GLASS_PANE, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("Every 3 minutes, you may enter ")
                    .append(TextUtil.withColour("Spectator Mode", Formatting.WHITE))
                    .append(TextUtil.gray(" for 3 seconds.")),
                AbilityText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.withColour("Haunt", Formatting.GRAY)
        );
    }

    public Haunt(IndirectPlayer player) {
        super(player);
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    @Override
    public boolean getToggled() {
        return haunting;
    }

    @Override
    public Identifier getID() {
        return ID;
    }

    public List<Filter> getFilters() {
        return List.of(
            new Filters.Cooldown(COOLDOWN_TIME)
        );
    }

    @Override
    public AbilityResult apply() {
        haunting = true;
        this.player.getPlayerOrThrow().changeGameMode(GameMode.SPECTATOR);
        this.player.sendOverlayNow(Text.of("You are now HAUNTING"));
        this.resetLastActivated();

        getShadow().addTickable(
            Delay.of(() -> {
                haunting = false;
                this.player.scheduleUntil((p) -> {
                    p.changeGameMode(GameMode.SURVIVAL);
                    this.player.sendOverlayNow(Text.of("You are no longer HAUNTING"));
                }, CancelPredicates.NEVER_CANCEL);
            }
            , 3 * 20));
        return AbilityResult.CLOSE;
    }
}
