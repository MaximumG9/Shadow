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

public class SheriffBow extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("sheriff_bow");
    private static final ItemStack ITEM_STACK;
    
    static {
        ITEM_STACK = new ItemStack(Items.BOW, 1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal("A bow that can instantly kill whoever you shoot it at.")
                    .styled(style -> style.withColor(Formatting.GRAY).withItalic(false)),
                Text.literal("If shot a villager, the owner and shooter die too.")
                    .styled(style -> style.withColor(Formatting.GRAY).withItalic(false)),
                ItemText()
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("Sheriff Bow").styled(style -> style.withColor(Formatting.GOLD))
        );
        ITEM_STACK.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
    }
    
    public SheriffBow(IndirectPlayer player) {
        super(player);
    }
    private static ItemStack createSheriffBow(IndirectPlayer player) {
        ItemStack item =
            NBTUtil.applyCustomDataToStack(
                NBTUtil.addID(
                    new ItemStack(Items.BOW),
                    ID
                ),
                compound -> {
                    compound.putUuid("owner", player.playerUUID);
                    return compound;
                }
            );
        item.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("Sheriff Bow").styled(style -> style.withColor(Formatting.GOLD))
        );
        item.set(
            DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
            true
        );
        
        NBTUtil.flagRestrictMovement(item);
        return item;
    }
    
    @Override
    public void init() {
        player.giveItem(
            createSheriffBow(player),
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
            CancelPredicates.cancelOnPhaseChange(player.getShadow().state.phase));
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
