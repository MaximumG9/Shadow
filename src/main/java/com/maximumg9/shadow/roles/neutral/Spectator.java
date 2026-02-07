package com.maximumg9.shadow.roles.neutral;

import com.maximumg9.shadow.roles.*;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Spectator extends Role {
    public static final RoleFactory<Spectator> FACTORY = new Factory();
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.GRAY);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.LIGHT);
    
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Spectator(null).getName());
    }
    
    public Spectator(@Nullable IndirectPlayer player) {
        super(player, List.of());
    }

    @Override
    public void init() {
        player.addToTeam(player.getShadow().playerTeam, CancelPredicates.cancelOnLostRole(this));
    }
    
    @Override
    public Faction getFaction() {
        return Faction.SPECTATOR;
    }
    
    @Override
    public SubFaction getSubFaction() { return SubFaction.SPECTATOR; }
    
    @Override
    public String getRawName() {
        return "Spectator";
    }
    
    @Override
    public Style getStyle() {
        return STYLE;
    }
    
    @Override
    public void onNight() {
        this.player.sendOverlay(
            TextUtil.gray("It is now night"),
            CancelPredicates.IS_DAY
        );
        super.onNight();
    }
    
    @Override
    public void onDay() {
        this.player.sendOverlay(
            TextUtil.gray("It is now day"),
            CancelPredicates.IS_NIGHT
        );
        super.onDay();
    }
    
    @Override
    public Roles getRole() {
        return Roles.SPECTATOR;
    }
    
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
    
    private static class Factory implements RoleFactory<Spectator> {
        @Override
        public Spectator makeRole(@Nullable IndirectPlayer player) {
            return new Spectator(player);
        }
    }
}
