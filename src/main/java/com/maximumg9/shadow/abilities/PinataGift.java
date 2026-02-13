package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public class PinataGift extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("pinata_gifting");
    private static final ItemStack ITEM_STACK;
    private static final Identifier HP_ATTR_ID = MiscUtil.shadowID("pinata_max_health");
    private static final double healthIncrease = 10;


    static {
        ITEM_STACK = new ItemStack(Items.ENDER_CHEST, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                TextUtil.gray("When killed, the team/player that kills you are given special gifts."),
                PassiveText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.blue("Piñata's Gift")
        );
    }

    public PinataGift(IndirectPlayer player) { super(player); };

    public void onDeath(DamageSource damageSource) {
        ServerPlayerEntity attacker = (ServerPlayerEntity) damageSource.getAttacker();
        if (attacker == null) return;

        final List<IndirectPlayer> PLAYERS = this.player.getShadow().getAllLivingPlayers().toList();

        final List<IndirectPlayer> SHADOWS = new ArrayList<>(PLAYERS.stream().filter((p) -> p.role.getFaction() == Faction.SHADOW).toList());
        final List<IndirectPlayer> VILLAGERS = new ArrayList<>(PLAYERS.stream().filter((p) -> p.role.getFaction() == Faction.VILLAGER).toList());

        EntityAttributeInstance instance = attacker.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (instance == null) return;

        EntityAttributeModifier healthModifier = instance.getModifier(HP_ATTR_ID);
        if (healthModifier == null) {
            instance.addPersistentModifier(
                new EntityAttributeModifier(
                    HP_ATTR_ID,
                    healthIncrease,
                    EntityAttributeModifier.Operation.ADD_VALUE
                )
            );
        } else {
            if (healthModifier.operation() != EntityAttributeModifier.Operation.ADD_VALUE) {
                MiscUtil.getShadow(attacker.server).ERROR("Pinata Operation isn't ADD_VALUE (how did you MANAGE this)");
                return;
            }
            instance.overwritePersistentModifier(
                new EntityAttributeModifier(
                    HP_ATTR_ID,
                    healthIncrease + healthModifier.value(),
                    EntityAttributeModifier.Operation.ADD_VALUE
                )
            );
        }




        IndirectPlayer randomVillager = VILLAGERS.get((int) (Math.random()*VILLAGERS.size()));
        VILLAGERS.remove(randomVillager);
        IndirectPlayer randomShadow = this.player.getShadow().indirectPlayerManager.getIndirect(attacker).role.getFaction() != Faction.SHADOW ? SHADOWS.get((int) (Math.random() * SHADOWS.size())) : VILLAGERS.get((int) (Math.random()*VILLAGERS.size()));
        List<IndirectPlayer> giftedPlayers;
        if (Math.random() < 0.5) giftedPlayers = List.of(randomShadow, randomVillager);
        else giftedPlayers = List.of(randomVillager, randomShadow);


        attacker.server.getPlayerManager().getPlayerList().forEach((player) -> {
            player.sendMessage(
                TextUtil.withColour("The Piñata has gifted that one of ", Formatting.DARK_AQUA)
                    .append(giftedPlayers.getFirst().getName())
                    .append(" and ")
                    .append(giftedPlayers.getLast().getName())
                    .append(" is a shadow!")
            );
        });
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public AbilityResult apply() { return AbilityResult.NO_CLOSE; }
}
