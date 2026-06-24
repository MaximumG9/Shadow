package com.maximumg9.shadow.abilities.villager;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.AbilityResult;
import com.maximumg9.shadow.roles.villager.Librarian;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class LibrarianInfo extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("librarian_info");
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.DARK_GREEN);
    private static final ItemStack ITEM_STACK;
    private MutableText playerText = Text.empty();

    static {
        ITEM_STACK = Items.BOOK.getDefaultStack();
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.gold("Librarian Information")
        );
    }

    public LibrarianInfo(IndirectPlayer player) {
        super(player);
    }

    @Override
    public void init() {
        ArrayList<IndirectPlayer> possibleGoodPlayers = new ArrayList<>(this.getShadow().getAllLivingPlayers()
            .filter(p -> !p.equals(this.player))
            .filter(p -> p.role.getFaction() == this.player.role.getFaction())
            .toList());
        if(possibleGoodPlayers.size() < 2) {
            this.player.sendMessageOrThrow(Text.literal("Not enough players on your faction to give librarian information"));
        } else {

            ArrayList<IndirectPlayer> players = new ArrayList<>();

            players.add(possibleGoodPlayers.remove(
                this.getShadow().random.nextInt(possibleGoodPlayers.size())
            ));
            players.add(possibleGoodPlayers.get(
                this.getShadow().random.nextInt(possibleGoodPlayers.size())
            ));

            List<IndirectPlayer> possibleThirds = this.getShadow().getAllLivingPlayers()
                .filter(p -> !p.equals(this.player))
                .filter(p -> !players.contains(p))
                .toList();

            players.add(possibleThirds.get(
                this.getShadow().random.nextInt(possibleThirds.size())
            ));
            Util.shuffle(players,this.getShadow().random);

            MutableText message = Text.literal("At least two of these players are on your faction:\n").setStyle(Librarian.STYLE);

            boolean first = true;

            for(IndirectPlayer p : players) {
                if(!first) playerText.append(", ");
                first = false;
                playerText.append(p.getName());
            }

            this.player.sendMessageOrThrow(
                message.append(playerText)
            );
        }
        super.init();
    }

    @Override
    public Identifier getID() {
        return ID;
    }

    @Override
    public AbilityResult apply() {
        return AbilityResult.NO_CLOSE;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        Text constant = Text.literal("At least two of these players are in your faction").setStyle(Librarian.STYLE);
        ItemStack stack = ITEM_STACK.copy();
        stack.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                constant,
                playerText
            )
        );
        return stack;
    }
}
