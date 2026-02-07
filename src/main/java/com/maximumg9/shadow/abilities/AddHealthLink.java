package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.abilities.filters.Filters;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.villager.Lifeweaver;
import com.maximumg9.shadow.saving.Saveable;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
                Ability.AbilityText()
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
                    if(target == null) {
                        clicker.sendMessage(TextUtil.red("You need to select a player to link to"));
                        return;
                    }
                    IndirectPlayer iClicker = getShadow().getIndirect(clicker);
                    this.link(target, iClicker);
                    clicker.sendMessage(
                        TextUtil.green("Successfully linked to ")
                            .append(target.getName())
                    );
                    if(target.role.getFaction() == Faction.SHADOW) {
                        target.sendMessage(
                            TextUtil.green("You appear to have been linked with ")
                                .append(this.player.getName())
                                .append(" (Your health is not actually linked as you are a shadow)"),
                            (p) ->
                                p.role.getFaction() == Faction.SHADOW
                        );
                    } else {
                        target.sendMessage(
                            TextUtil.green("Your health has been linked with ")
                                .append(this.player.getName()),
                            (p) ->
                                p.role.getFaction() == Faction.VILLAGER || p.role.getFaction() == Faction.NEUTRAL
                        );
                    }
                    giveEffects(iClicker, target);
                },
                this.getShadow().indirectPlayerManager
                    .getAllPlayers()
                    .stream()
                    .filter(p -> p.role.getFaction() != Faction.SPECTATOR)
                    .toList()
            )
        );

        return AbilityResult.NO_CLOSE;
    }

    private void giveEffects(IndirectPlayer player1, IndirectPlayer player2) {
        StatusEffectInstance firstResistance = new StatusEffectInstance(
            StatusEffects.RESISTANCE,
            -1,
            1 - 1,
            false,
            false,
            true
        );

        StatusEffectInstance firstRegeneration = new StatusEffectInstance(
            StatusEffects.REGENERATION,
            -1,
            1 - 1,
            false,
            false,
            true
        );

        player1.giveEffect(
            firstResistance,
            (p) ->
                p.role.getFaction() != Faction.SPECTATOR
        );
        player1.giveEffect(
            firstRegeneration,
            (p) ->
                p.role.getFaction() != Faction.SPECTATOR
        );

        StatusEffectInstance secondResistance = new StatusEffectInstance(
            StatusEffects.RESISTANCE,
            -1,
            1 - 1,
            false,
            false,
            true
        );

        StatusEffectInstance secondRegeneration = new StatusEffectInstance(
            StatusEffects.REGENERATION,
            -1,
            1 - 1,
            false,
            false,
            true
        );

        player2.giveEffect(
            secondResistance,
            (p) ->
                p.role != null &&
                p.role.getFaction() != Faction.SPECTATOR
        );
        player2.giveEffect(
            secondRegeneration,
            (p) ->
                p.role != null &&
                p.role.getFaction() != Faction.SPECTATOR
        );
    }

    private void link(IndirectPlayer player1, IndirectPlayer player2) {
        if(player1.link == null && player2.link == null) {
            Link link = new Link(getShadow(), player1, player2);
            player1.link = link;
            player2.link = link;
        } else if(
            player1.link != null &&
            player2.link != null &&
            player1.link != player2.link
        ) {
            Link.combinePropagateAndSync(player1.link,player2.link);
        } else {
            Link oldLink;
            if(player1.link != null) {
                oldLink = player1.link;
                player1.link.players.add(player2);
            } else {
                oldLink = player2.link;
                player2.link.players.add(player1);
            }

            oldLink.syncHealths(
                new DamageSource(
                    MiscUtil.getDamageType(
                        player1.getShadow().getServer(),
                        DamageTypes.GENERIC
                    )
                )
            );
        }
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    @Override
    public List<Filter> getFilters() {
        return filters;
    }

    public static class Link implements Saveable {
        private float health = 1;
        public final List<IndirectPlayer> players;
        private final Shadow shadow;

        public Link(Shadow shadow, IndirectPlayer... players) {
            this.shadow = shadow;
            this.players = new ArrayList<>(List.of(players));
            syncHealths(
                new DamageSource(
                    MiscUtil.getDamageType(
                        shadow.getServer(),
                        DamageTypes.GENERIC
                    )
                )
            );
            shadow.linkRegistry.addLink(this);
        }

        public Link(Shadow shadow) {
            this.shadow = shadow;
            this.players = new ArrayList<>();
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

            NbtList players = nbt.getList(
                "players",
                NbtElement.INT_ARRAY_TYPE
            );

            players.stream()
                .map(NbtHelper::toUuid)
                .map(shadow.indirectPlayerManager::get)
                .forEach(this.players::add);
        }

        public static void combinePropagateAndSync(Link link1, Link link2) {
            Link newLink = combine(link1,link2);
            propagateNewLink(link1,newLink);
            propagateNewLink(link2,newLink);
            newLink.syncHealths(
                newLink
                    .shadow
                    .getServer()
                    .getOverworld()
                    .getDamageSources()
                    .generic()
            );
            Link.destroyLink(link1);
            Link.destroyLink(link2);
        }

        private static void propagateNewLink(Link oldLink, Link newLink) {
            for(IndirectPlayer p : oldLink.players) {
                p.link = newLink;
            }
            oldLink.players.clear();
        }

        private static Link combine(Link link1, Link link2) {
            if(link1.shadow != link2.shadow) {
                throw new IllegalArgumentException("Links must be from the same shadow (wtf???)");
            }

            Link newLink = new Link(link1.shadow);

            newLink.players.addAll(link1.players);
            newLink.players.addAll(link2.players);

            newLink.health = (link1.health + link2.health) / 2;

            return newLink;
        }

        public static void destroyLink(Link link) {
            for(IndirectPlayer p : link.players) {
                p.link = null;
            }

            link.shadow.linkRegistry.removeLink(link);
        }

        public void syncHealths(DamageSource source) {
            players.stream()
                .filter(
                    (p) ->
                        p.role.getFaction() != Faction.SHADOW
                )
                .forEach(
                    (player) ->
                        player.scheduleUntil(
                            (sPlayer) -> {
                                float newHealth = sPlayer.getMaxHealth() * this.health;
                                setHealthNoLifeLink(sPlayer,newHealth);
                                if (sPlayer.isDead()) {
                                    if (!sPlayer.tryUseTotem(source)) {
                                        sPlayer.onDeath(source);
                                    }
                                }
                            },
                            CancelPredicates.cancelOnPhaseChange(
                                player.getShadow().state.phase
                            )
                        )
                );

            if(this.health <= 0) {
                destroyLink(this);
            }
        }

        public static void setHealthNoLifeLink(LivingEntity entity, float health) {
            entity.getDataTracker()
                .set(
                    LivingEntity.HEALTH,
                    MathHelper.clamp(
                        health,
                        0.0F,
                        entity.getMaxHealth()
                    )
                );
        }

        public boolean update(@Nullable DamageSource source, float newHealth, float oldHealth, ServerPlayerEntity damageTarget) {
            Shadow shadow = MiscUtil.getShadow(damageTarget.server);

            IndirectPlayer p = shadow.getIndirect(damageTarget);

            if(p.role.getFaction() == Faction.SHADOW) return false;

            float fractionDamage = (newHealth - oldHealth) / damageTarget.getMaxHealth();

            this.health += fractionDamage;

            if(source == null) {
                source = damageTarget.getDamageSources().magic();
            }

            // Guys is life linking magical?
            // Should I use the magic damage type???

            syncHealths(source);

            return true;
        }
    }
}
