package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.abilities.filters.Filters;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class Cull extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("cull");
    private static final ItemStack ITEM_STACK;
    
    static {
        ITEM_STACK = Items.NETHERITE_SWORD.getDefaultStack();
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.red("Cull")
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }
    
    private boolean usedThisNight = false;
    
    public Cull(IndirectPlayer player) {
        super(player);
    }
    
    public List<Filter> getFilters() {
        return List.of(
            new Filters.NotGracePeriod(),
            new Filters.Night(),
            new Filter("You've already used this ability tonight!") {
                @Override
                public boolean filter(Ability ability) {
                    return usedThisNight;
                }
            } // TODO: This is bad
        );
    }
    
    @Override
    public Identifier getID() { return ID; }
    
    @Override
    public void onDay() {
        usedThisNight = false;
        super.onDay();
    }
    
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ItemStack stack = ITEM_STACK.copy();
        stack.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("Damage all non-shadows within ")
                    .append(TextUtil.gray(String.valueOf(this.getShadow().config.cullRadius)))
                    .append(TextUtil.gray(" blocks of you")),
                TextUtil.gray("For each non-shadow within range"),
                TextUtil.gray("damage increases by ")
                    .append(TextUtil.hearts(2)),
                TextUtil.gray("(max of ")
                    .append(TextUtil.hearts(9.5f))
                    .append(TextUtil.gray(")")),
                AbilityText()
            )
        );
        return stack;
    }
    
    @Override
    public AbilityResult apply() {
        ServerPlayerEntity p = this.player.getPlayerOrThrow();
        
        List<ServerPlayerEntity> realTargets = p.getServerWorld().getPlayers(
            (player) -> {
                IndirectPlayer indirect = getShadow().getIndirect(player);
                return player.squaredDistanceTo(p) <= this.player.getShadow().config.cullRadius * this.player.getShadow().config.cullRadius
                    && (indirect.role == null || indirect.role.getFaction() != Faction.SHADOW);
            }
        );
        
        if (realTargets.isEmpty()) {
            this.player.sendMessageNow(
                Text.literal("No targets to hit")
            );
            return AbilityResult.CLOSE;
        }
        
        List<ServerPlayerEntity> fakeTargets = p.getServerWorld().getPlayers(
            (player) -> {
                IndirectPlayer indirect = getShadow().getIndirect(player);
                return player.squaredDistanceTo(p) <= this.player.getShadow().config.cullRadius * this.player.getShadow().config.cullRadius
                    && indirect.role != null && indirect.role.getFaction() == Faction.SHADOW;
            }
        );
        
        float damage = Math.min(realTargets.size() * 4, 19);
        
        realTargets.forEach((player) ->
            player.damage(
                p.getServerWorld()
                    .getDamageSources()
                    .magic(),
                Math.min(damage, player.getHealth() - 1.0f)
            )
        );
        
        fakeTargets.forEach((player) ->
            player.damage(
                p.getServerWorld()
                    .getDamageSources()
                    .magic(),
                0.001f
            )
        );
        
        this.player.sendMessageNow(
            TextUtil.green("Damaged ")
                .append(
                    Texts.join(
                        realTargets.stream()
                            .map(PlayerEntity::getName)
                            .map(
                                (text) -> text.copy().styled(style -> style.withColor(Formatting.YELLOW))
                            )
                            .toList(),
                        TextUtil.green(", ")
                    )
                ).append(
                    TextUtil.green(" for ")
                ).append(TextUtil.hearts(damage / 2))
        );
        
        usedThisNight = true;
        return AbilityResult.CLOSE;
    }
}
