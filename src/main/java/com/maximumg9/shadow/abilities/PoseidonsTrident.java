package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

public class PoseidonsTrident extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("poseidons_trident");
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = new ItemStack(Items.BOW, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal("In main hand:")
                    .styled(style -> style.withColor(Formatting.GRAY).withItalic(false))
                    .append(
                        Text.literal("Riptide I")
                            .styled(style -> style.withColor(Formatting.BLUE))
                    ),
                Text.literal("In off hand: ")
                    .styled(style -> style.withColor(Formatting.GRAY).withItalic(false))
                    .append(
                        Text.literal("Loyalty III")
                            .styled(style -> style.withColor(Formatting.BLUE))
                    ),
                ItemText().append(InvisibleText())
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("Poseidon's Trident").styled(style -> style.withColor(Formatting.BLUE))
        );
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
        NBTUtil.removeAttributeModifiers(ITEM_STACK);
    }

    public PoseidonsTrident(IndirectPlayer player) {
        super(player);
    }

    private static ItemStack createTrident() {
        ItemStack item = NBTUtil.addID(
            new ItemStack(Items.TRIDENT),
            ID
        );

        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal("In main hand:")
                    .styled(style -> style.withColor(Formatting.GRAY).withItalic(false))
                    .append(
                        Text.literal("Riptide I")
                            .styled(style -> style.withColor(Formatting.BLUE))
                    ),
                Text.literal("In off hand: ")
                    .styled(style -> style.withColor(Formatting.GRAY).withItalic(false))
                    .append(
                        Text.literal("Loyalty III")
                            .styled(style -> style.withColor(Formatting.BLUE))
                    ),
                InvisibleText()
            )
        );

        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("Poseidon's Trident").styled(style -> style.withColor(Formatting.BLUE))
        );
        NBTUtil.flagAsInvisible(item);
        NBTUtil.flagRestrictMovement(item);
        return item;
    }

    @Override
    public void init() {
        player.giveItem(
            createTrident(),
            MiscUtil.DELETE_WARN,
            CancelPredicates.cancelOnPhaseChange(player.getShadow().state.phase)
        );

        super.init();
    }
    @Override
    public void deInit() {
        player.scheduleUntil(
            (player) ->
                player.getInventory()
                    .remove((item) -> player.getUuid().equals(NBTUtil.getCustomData(item).getUuid("owner")),
                        1,
                        player.playerScreenHandler.getCraftingInput()),
            CancelPredicates.cancelOnPhaseChange(player.getShadow().state.phase)
        );
        super.deInit();
    }

    @Override
    public Identifier getID() { return ID; }

    @Override
    public AbilityResult apply() {
        return AbilityResult.NO_CLOSE;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
}
