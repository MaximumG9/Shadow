package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.abilities.filters.Filters;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.villager.Lifeweaver;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddHealthLink extends Ability {
    private static final ItemStack ITEM_STACK = new ItemStack(Items.GOLDEN_APPLE);
    public static final Identifier ID = MiscUtil.shadowID("lifelink");

    private final List<Filter> filters = List.of(
        new Filters.OneTimeUse()
    );

    static {
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("Link")
                .setStyle(Lifeweaver.STYLE)
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil
                    .gray("Link your health with another player, but gain ")
                    .append(
                        Text.literal("Resistance I").styled(style -> style.withColor(Formatting.DARK_GRAY))
                    ).append(
                        " and "
                    ).append(
                        Text.literal("Regeneration I").styled(style -> style.withColor(Formatting.RED))
                    ),
                TextUtil
                    .gray("If you link with a shadow, you both gain ")
                    .append(
                        Text.literal("Resistance I").styled(style -> style.withColor(Formatting.DARK_GRAY))
                    ).append(
                        " and "
                    ).append(
                        Text.literal("Regeneration I").styled(style -> style.withColor(Formatting.RED))
                    ).append(
                        " but your health is not linked"
                    ),
                com.maximumg9.shadow.abilities.Ability.AbilityText()
            )
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }

    public AddHealthLink(IndirectPlayer player) {
        super(player);
    }

    @Override
    public Identifier getID() {
        return ID;
    }

    @Override
    public AbilityResult apply() {
        this.player.getPlayerOrThrow().openHandledScreen(
            new DecisionScreenHandler.Factory<>(
                Text.literal("Link to a player"),
                (target, clicker, button, actionType) -> {
                    this.link(target, getShadow().getIndirect(clicker));
                },
                this.getShadow().indirectPlayerManager
                    .getAllPlayers()
                    .stream()
                    .filter(p -> p.role != null && p.role.getFaction() != Faction.SPECTATOR)
                    .toList()
            )
        );

        return AbilityResult.NO_CLOSE;
    }

    private void link(IndirectPlayer player1, IndirectPlayer player2) {
        player1.role
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    @Override
    public List<Filter> getFilters() {
        return filters;
    }

    public static class Link {
        private float health = 1;
        public final List<IndirectPlayer> players = new ArrayList<>();
        private final Shadow shadow;

        public Link(Shadow shadow) {
            this.shadow = shadow;
        }

        public NbtCompound writeNBT(NbtCompound nbt) {
            nbt.putFloat("health",health);

            NbtList list = new NbtList();
            players.stream()
                .map(
                    (p) ->
                        NbtHelper.fromUuid(p.playerUUID)
                ).forEach(list::add);

            nbt.put("players",list);
            return nbt;
        }

        public void readNBT(NbtCompound nbt) {
            this.players.clear();
            this.health = nbt.getFloat("health");

            NbtList players = nbt.getList("players", NbtElement.INT_ARRAY_TYPE);

            players.stream()
                .map(NbtHelper::toUuid)
                .map(shadow.indirectPlayerManager::get)
                .forEach(this.players::add);
        }

        public static Link combine(Link link1, Link link2) {
            if(link1.shadow != link2.shadow) throw new IllegalArgumentException("Links must be from the same shadow (wtf???)");

            Link newLink = new Link(link1.shadow);

            newLink.players.addAll(link1.players);
            newLink.players.addAll(link2.players);

            newLink.health = (link1.health + link2.health) / 2;

            RegistryEntry<DamageType> genericDamage = newLink.shadow
                .getServer()
                .getRegistryManager()
                .get(RegistryKeys.DAMAGE_TYPE)
                .getEntry(DamageTypes.GENERIC)
                .get();

            newLink.syncHealths(new DamageSource(genericDamage));

            return newLink;
        }

        private void syncHealths(DamageSource source) {
            players.stream()
                .filter(
                    (p) ->
                        p.role == null ||
                            p.role.getFaction() != Faction.SHADOW
                )
                .forEach(
                    (player) ->
                        player.scheduleUntil(
                            (sPlayer) -> {
                                sPlayer.setHealth(sPlayer.getMaxHealth() * this.health);
                                if (sPlayer.isDead()) {
                                    if (!sPlayer.tryUseTotem(source)) {
                                        sPlayer.onDeath(source);
                                    }
                                }
                            },
                            CancelPredicates.cancelOnPhaseChange(player.getShadow().state.phase)
                        )
                );
        }

        private void update(DamageSource source, float newHealth, float oldHealth, ServerPlayerEntity damageTarget) {
            float fractionDamage = (newHealth - oldHealth) / damageTarget.getMaxHealth();

            this.health += fractionDamage;

            syncHealths(source);
        }
    }
}
