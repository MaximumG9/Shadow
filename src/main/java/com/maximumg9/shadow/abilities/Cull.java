package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Supplier;

public class Cull extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("cull");
    private static final ItemStack ITEM_STACK;
    
    static {
        ITEM_STACK = Items.NETHERITE_SWORD.getDefaultStack();
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.red("Cull")
        );
    }
    
    private boolean usedThisNight = false;
    
    public Cull(IndirectPlayer player) {
        super(player);
    }
    
    public List<Supplier<AbilityFilterResult>> getFilters() {
        return List.of(
            () -> {
                if (getShadow().isGracePeriod())
                    return AbilityFilterResult.FAIL("You cannot use this ability in Grace Period.");
                return AbilityFilterResult.PASS();
            },
            () -> {
                if (!getShadow().isNight())
                    return AbilityFilterResult.FAIL("You can only use this ability during the night!");
                return AbilityFilterResult.PASS();
            },
            () -> {
                if (usedThisNight) return AbilityFilterResult.FAIL("You've already used this ability tonight!");
                return AbilityFilterResult.PASS();
            }
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
                new DamageSource(
                    p.getServerWorld()
                        .getRegistryManager()
                        .get(RegistryKeys.DAMAGE_TYPE)
                        .getEntry(DamageTypes.MAGIC)
                        .orElseThrow()
                ),
                Math.min(damage, player.getHealth() - 1.0f)
            )
        );
        
        fakeTargets.forEach((player) ->
            player.damage(
                new DamageSource(
                    p.getServerWorld()
                        .getRegistryManager()
                        .get(RegistryKeys.DAMAGE_TYPE)
                        .getEntry(DamageTypes.MAGIC)
                        .orElseThrow()
                ),
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
