package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.Tickable;
import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Ability implements ItemRepresentable, Tickable {
    private static final Text PASSIVE_TEXT = TextUtil.blue("[PASSIVE]");
    private static final Text ITEM_TEXT = TextUtil.gold("[ITEM]");
    private static final Text INVISIBLE_TEXT = TextUtil.gray("[INVISIBLE]");
    private static final Text ABILITY_TEXT = TextUtil.withColour("[ABILITY]",Formatting.DARK_PURPLE);

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
        return this.getShadow().config.maxCooldownManager.getMaxCooldown(this.getID(), cooldown) -
            (this.getShadow().getServer().getOverworld().getTime() - getLastActivated());
    }
    
    public Shadow getShadow() { return player.getShadow(); }
    
    public abstract Identifier getID();
    
    public IndirectPlayer getPlayer() {
        return this.player;
    }

    public boolean getToggled() { return false; }

    public AbilityResult triggerApply() {
        for (Filter filter : getFilters()) {
            AbilityFilterResult result = filter.test(this);
            if (!result.status.equals(AbilityFilterResult.Status.PASS)) {
                this.player.sendMessageNow(TextUtil.red(result.message));
                return AbilityResult.CLOSE;
            }
        }

        AbilityResult result = apply();

        for (Filter filter : getFilters()) {
            filter.postApply(this);
        }

        getShadow().getOnlinePlayers()
            .stream()
            .filter(p ->
                this.player.getSquaredDistance(p) <=
                    this.player.getShadow().config.fearRadius * this.player.getShadow().config.fearRadius
                    && this.player.role.hasAbility(Paranoia.ID)
            ).map((p) ->
                ((Paranoia) p.role.getAbility(Paranoia.ID).get())
            ).forEach(
                (p) -> p.addPing(this.player)
            );

        return result;
    }
    public abstract AbilityResult apply();
    
    public void init() {
        this.lastActivated = Integer.MIN_VALUE; // Just a very negative value AAHHH I can't use Long.MIN_VALUE because underflows
    }
    public void deInit() { }
    
    public void onNight() { }
    public void onDay() { }

    public void onJoin() { }
    public void onLeave() { }

    public void onDeath(DamageSource damageSource) { }

    public void onAnyDeath(DamageSource damageSource, IndirectPlayer deadPlayer) { }

    public void onPlayerKill() { }

    public void tick() { }

    @FunctionalInterface
    public interface Factory {
        Ability create(@Nullable IndirectPlayer player);
    }
}
