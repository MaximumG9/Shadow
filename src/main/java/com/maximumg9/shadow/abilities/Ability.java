package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Ability implements ItemRepresentable {
    private static final Text PASSIVE_TEXT = Text.literal("[PASSIVE]")
        .styled(style -> style.withColor(Formatting.BLUE));
    private static final Text ITEM_TEXT = Text.literal("[ITEM]")
        .styled(style -> style.withColor(Formatting.GOLD));
    private static final Text INVISIBLE_TEXT = Text.literal("[INVISIBLE]")
        .styled(style -> style.withColor(Formatting.GRAY));
    private static final Text ABILITY_TEXT = Text.literal("[ABILITY]")
        .styled(style -> style.withColor(Formatting.DARK_PURPLE));
    
    final IndirectPlayer player;
    private long lastActivated;
    
    public Ability(IndirectPlayer player) {
        this.player = player;
    }
    static MutableText PassiveText() { return PASSIVE_TEXT.copy(); }
    static MutableText ItemText() { return ITEM_TEXT.copy(); }
    static MutableText InvisibleText() { return INVISIBLE_TEXT.copy(); }
    static MutableText AbilityText() { return ABILITY_TEXT.copy(); }
    public List<Filter> getFilters() { return List.of(); }
    public long getLastActivated() { return lastActivated; }
    public void resetLastActivated() { this.lastActivated = this.getShadow().getServer().getOverworld().getTime(); }
    
    public long getCooldownTimeLeft(long cooldown) {
        return this.getShadow().config.maxCooldownManager.getMaxCooldown(this.getID(), cooldown) +
            getLastActivated() -
            this.getShadow().getServer().getOverworld().getTime();
    }
    
    public Shadow getShadow() { return player.getShadow(); }
    
    public abstract Identifier getID();
    
    public IndirectPlayer getPlayer() {
        return this.player;
    }
    
    public AbilityResult triggerApply() {
        for (Filter filter : getFilters()) {
            AbilityFilterResult result = filter.test(this);
            if (!result.status.equals(AbilityFilterResult.Status.PASS)) {
                this.player.sendMessageNow(TextUtil.error(result.message));
                return AbilityResult.CLOSE;
            }
        }
        
        return apply();
    }
    public abstract AbilityResult apply();
    
    public void init() { this.lastActivated = -Integer.MAX_VALUE; }
    public void deInit() { }
    
    public void onNight() { }
    public void onDay() { }
    
    @FunctionalInterface
    public interface Factory {
        Ability create(@Nullable IndirectPlayer player);
    }
}
