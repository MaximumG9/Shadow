package com.maximumg9.shadow.abilities.villager;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.AbilityResult;
import com.maximumg9.shadow.abilities.filters.Filter;
import com.maximumg9.shadow.abilities.filters.Filters;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.*;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class SendLoveLetter extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("send_love_letter");
    private static final ItemStack SEND_ITEM_STACK;
    private static final ItemStack WAIT_ITEM_STACK;

    private static final int COOLDOWN = 3 * 60 * 20;

    static {
        SEND_ITEM_STACK = new ItemStack(Items.PAPER);
        SEND_ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.green("Send a love letter")
        );
        SEND_ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("The target may accept or decline"),
                TextUtil.green("If they accept and are a villager: You both gain ")
                    .append(TextUtil.hearts(2.0f))
                    .append(" ")
                    .append(TextUtil.gray("Resistance I"))
                    .append(" ")
                    .append(TextUtil.red("Regeneration I")),
                TextUtil.green("BUT IF EITHER DIES, BOTH DIE").styled((style) -> style.withBold(true)),
                TextUtil.red("If they accept and are a shadow: for 10 minutes will lose if you ")
                    .append(TextUtil.withColour("DIE",Formatting.BLACK).styled(style -> style.withBold(true))),
                TextUtil.red("BUT IF THE SHADOW DIES, YOU DIE").styled((style) -> style.withBold(true)),
                AbilityText()
            )
        );
        NBTUtil.removeAttributeModifiers(SEND_ITEM_STACK);

        WAIT_ITEM_STACK = new ItemStack(Items.CLOCK);
        SEND_ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("The target may accept or decline"),
                TextUtil.green("If they accept and are a villager: You both gain ")
                    .append(TextUtil.hearts(2.0f))
                    .append(" of max health ")
                    .append(TextUtil.gray("Resistance I"))
                    .append(" and ")
                    .append(TextUtil.red("Regeneration I")),
                TextUtil.green("BUT IF EITHER DIES, BOTH DIE").styled((style) -> style.withBold(true)),
                TextUtil.red("If they accept and are a shadow: for the next 10 minutes if you die you do not win "),
                TextUtil.red("AND IF THE SHADOW DIES, YOU DIE").styled((style) -> style.withBold(true)),
                AbilityText()
            )
        );
        NBTUtil.removeAttributeModifiers(WAIT_ITEM_STACK);
    }

    private IndirectPlayer sendTarget = null;

    public SendLoveLetter(IndirectPlayer player) {
        super(player);
    }

    @Override
    public List<Filter> getFilters() {
        return List.of(
            new Filters.Cooldown(COOLDOWN,"You cannot send a love letter until the next one expires in %s")
        );
    }

    @Override
    public Identifier getID() {
        return ID;
    }

    @Override
    public AbilityResult apply() {
        this.player.getPlayerOrThrow().openHandledScreen(
            new DecisionScreenHandler.Factory<>(
                Text.literal("Send a love letter"),
                (target, clicker, button, actionType) -> {
                    if(target == null) {
                        clicker.sendMessage(TextUtil.red("You need to select a player to send a love letter to"));
                        return;
                    }
                    if(target.equals(this.player)) {
                        clicker.sendMessage(TextUtil.red("You cannot send a love letter to yourself (that's just pathetic ): )"));
                        return;
                    }
                    if(this.sendTarget != null) return;
                    this.sendLoveLetter(target);
                    clicker.sendMessage(
                        TextUtil.green("Sent a love letter to ")
                            .append(target.getName())
                    );
                    resetLastActivated();
                },
                this.getShadow().indirectPlayerManager
                    .getAllPlayers()
                    .stream()
                    .filter(IndirectPlayer::isLiving)
                    .toList()
            )
        );

        return AbilityResult.NO_CLOSE;
    }

    @Override
    public void deInit() {
        if(this.sendTarget != null) {
            this.sendTarget.sendMessageOrThrow(TextUtil.red("The love letter sent to you has been cancelled"));
        }
    }

    public boolean resolveLoveLetter(IndirectPlayer attemptor) {
        if(!attemptor.equals(this.sendTarget)) return false;
        if(!attemptor.isLiving()) return false;
        if(!this.player.isLiving()) return false;

        if(this.sendTarget.role.getFaction() == Faction.VILLAGER) {
            attemptor.role.addAbility((p) -> new HealthyRelationship(p,this.player));
            this.player.role.addAbility((p) -> new HealthyRelationship(p,attemptor));

            this.player.sendMessage(
                attemptor.getName().copy().append(
                    TextUtil.green(" accepted your love letter putting you in a ")
                        .append(
                            TextUtil.red("Healthy Relationship").styled(
                                style -> style.withBold(true)
                            )
                        )
                ),
                CancelPredicates.cancelOnPhaseChange(this.getShadow().state.phase)
            );
        } else {
            attemptor.role.addAbility((p) -> new ToxicRelationship(p,this.player,false));
            this.player.role.addAbility((p) -> new ToxicRelationship(p,attemptor,true));

            this.player.sendMessage(
                attemptor.getName().copy().append(
                    TextUtil.green(" accepted your love letter putting you in a ")
                        .append(
                            TextUtil.withColour(
                                "Toxic Relationship",
                                Formatting.BLACK
                            ).styled(
                                style -> style.withBold(true)
                            )
                        )
                ),
                CancelPredicates.cancelOnPhaseChange(this.getShadow().state.phase)
            );
        }
        this.player.role.removeAbility(this);
        this.sendTarget = null;
        return true;
    }

    private void sendLoveLetter(IndirectPlayer sendTarget) {
        if(sendTarget.role.getFaction() == Faction.VILLAGER) {
            sendTarget.sendMessage(
                TextUtil.yellow("You received a love letter from ")
                    .append(this.player.getName())
                    .append("\n")
                    .append(Text.literal("If you accept, you will enter a "))
                    .append(TextUtil.red("Healthy Relationship"))
                    .append(Text.literal("\n"))
                    .append(Text.literal("You will gain "))
                    .append(TextUtil.hearts(2.0f))
                    .append(" of max health ")
                    .append(TextUtil.gray("Resistance I"))
                    .append(" and ")
                    .append(TextUtil.red("Regeneration I"))
                    .append(Text.literal("\n"))
                    .append(
                        TextUtil.red("BUT IF EITHER DIES, BOTH DIE")
                            .styled((style) -> style.withBold(true))
                        )
                    .append(Text.literal("\n"))
                    .append(
                        TextUtil.green("[ACCEPT]")
                            .styled(
                                (style -> style.withClickEvent(
                                    new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/loveletter " + this.player.playerUUID.toString()
                                    )
                                ))
                            )
                    ),
                CancelPredicates.or(
                    CancelPredicates.timer(COOLDOWN),
                    CancelPredicates.cancelOnLostAbility(this)
                )
            );
        } else {
            sendTarget.sendMessage(
                TextUtil.yellow("You received a love letter from ")
                    .append(this.player.getName())
                    .append("\n")
                    .append(Text.literal("If you accept, you will enter a "))
                    .append(TextUtil.withColour("Toxic Relationship", Formatting.BLACK))
                    .append(Text.literal(
                        " for " +
                            TimeUtil.ticksToText(this.getShadow().config.toxicRelationshipLength,false)
                    ))
                    .append(Text.literal("\n"))
                    .append(Text.literal("In a toxic relationship, they will lose if they "))
                    .append(TextUtil.withColour("DIE",Formatting.BLACK))
                    .append(Text.literal(" and if you "))
                    .append(TextUtil.withColour("DIE",Formatting.BLACK))
                    .append(Text.literal(" they "))
                    .append(TextUtil.withColour("DIE",Formatting.BLACK))
                    .append(Text.literal("\n"))
                    .append(
                        TextUtil.green("[ACCEPT]")
                            .styled(
                                (style -> style.withClickEvent(
                                    new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/loveletter " + this.player.playerUUID.toString()
                                    )
                                ))
                            )
                    ),
                CancelPredicates.or(
                    CancelPredicates.timer(COOLDOWN),
                    CancelPredicates.cancelOnLostAbility(this)
                )
            );
        }
        this.sendTarget = sendTarget;
        this.getShadow()
            .addTickable(
                CancellableDelay.of(
                    () -> {
                        this.player.sendMessage(
                            TextUtil.red("The love letter you sent to ")
                                .append(sendTarget.getName())
                                .append(" expired"),
                            CancelPredicates.cancelOnPhaseChange(this.getShadow().state.phase)
                        );
                        sendTarget.sendMessage(
                            TextUtil.red("The love letter sent to you by ")
                                .append(this.player.getName())
                                .append(" expired"),
                            CancelPredicates.cancelOnPhaseChange(this.getShadow().state.phase)
                        );
                        this.sendTarget = null;
                    },
                    COOLDOWN,
                    CancellableDelay.wrapCancelCondition(
                        CancelPredicates.cancelOnLostAbility(this),
                        this.player
                    )
                )
            );
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        if(sendTarget == null) {
            return SEND_ITEM_STACK.copy();
        } else {
            WAIT_ITEM_STACK.set(
                DataComponentTypes.ITEM_NAME,
                TextUtil.green("Wait for ")
                    .append(sendTarget.getName())
                    .append(" to respond!")
            );
            return WAIT_ITEM_STACK.copy();
        }
    }
}
