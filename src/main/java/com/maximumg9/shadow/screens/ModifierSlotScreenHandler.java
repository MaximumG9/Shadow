package com.maximumg9.shadow.screens;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.modifiers.Modifier;
import com.maximumg9.shadow.modifiers.ModifierSlot;
import com.maximumg9.shadow.modifiers.Modifiers;
import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.NotNull;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public class ModifierSlotScreenHandler extends ShadowScreenHandler {
    private static final int SIZE = 9 * 6;
    private static final Text NEXT_PAGE_TEXT = TextUtil.gold("Next Page");
    private static final Text LAST_PAGE_TEXT = TextUtil.gold("Last Page");
    
    private static final Text MINUS_10_PERCENT = TextUtil.red("-10%");
    private static final Text MINUS_5_PERCENT = TextUtil.red("-10%");
    private static final Text MINUS_1_PERCENT = TextUtil.red("-1%");
    private static final Text PLUS_10_PERCENT = TextUtil.green("+10%");
    private static final Text PLUS_5_PERCENT = TextUtil.green("+5%");
    private static final Text PLUS_1_PERCENT = TextUtil.green("+1%");
    
    private static final Text EXIT_SCREEN_TEXT = TextUtil.red("Return to modifier menu");
    private final ModifierSlot slot;
    private final SimpleInventory inventory;
    private final ScreenHandlerContext context;
    private int page = 0;

    private ModifierSlotScreenHandler(int syncID, ModifierSlot slot, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ScreenHandlerType.GENERIC_9X6, syncID, playerInventory);
        this.context = context;
        
        this.slot = slot;
        
        this.inventory = new SimpleInventory(SIZE);
        
        initSlots();
        
        buildUI();
    }
    
    void initSlots() {
        for (int k = 0; k < SIZE; ++k) {
            this.addSlot(
                new Slot(
                    inventory,
                    k,
                    0,
                    0
                )
            );
        }
        super.initSlots();
    }
    
    private void buildUI() {
        if (this.page < (Modifiers.values().length / 9) / 2) {
            ItemStack nextPageStack = Items.SPECTRAL_ARROW.getDefaultStack();
            nextPageStack.set(DataComponentTypes.ITEM_NAME, NEXT_PAGE_TEXT);
            this.inventory.setStack(8, nextPageStack);
        }
        if (this.page > 0) {
            ItemStack previousPageStack = Items.SPECTRAL_ARROW.getDefaultStack();
            previousPageStack.set(DataComponentTypes.ITEM_NAME, LAST_PAGE_TEXT);
            this.inventory.setStack(9 + 8, previousPageStack);
        }
        
        ItemStack minus10PercentStack = Items.RED_CONCRETE.getDefaultStack();
        minus10PercentStack.set(DataComponentTypes.ITEM_NAME, MINUS_10_PERCENT);
        this.inventory.setStack(SIZE - 9, minus10PercentStack);
        ItemStack minus5PercentStack = Items.ORANGE_CONCRETE.getDefaultStack();
        minus5PercentStack.set(DataComponentTypes.ITEM_NAME, MINUS_5_PERCENT);
        this.inventory.setStack(SIZE - 8, minus5PercentStack);
        ItemStack minus1PercentStack = Items.YELLOW_CONCRETE.getDefaultStack();
        minus1PercentStack.set(DataComponentTypes.ITEM_NAME, MINUS_1_PERCENT);
        this.inventory.setStack(SIZE - 7, minus1PercentStack);
        
        ItemStack chanceStack = Items.WHITE_CONCRETE.getDefaultStack();
        chanceStack.set(DataComponentTypes.MAX_STACK_SIZE, (int) Math.floor(this.slot.chance * 100));
        chanceStack.setCount((int) Math.floor(this.slot.chance * 100));
        chanceStack.set(DataComponentTypes.ITEM_NAME, Text.literal("Chance: " + (int) Math.floor(this.slot.chance * 100)));
        this.inventory.setStack(SIZE - 6, chanceStack);
        
        ItemStack plus1PercentStack = Items.LIME_CONCRETE.getDefaultStack();
        plus1PercentStack.set(DataComponentTypes.ITEM_NAME, PLUS_1_PERCENT);
        this.inventory.setStack(SIZE - 5, plus1PercentStack);
        ItemStack plus5PercentStack = Items.GREEN_CONCRETE.getDefaultStack();
        plus5PercentStack.set(DataComponentTypes.ITEM_NAME, PLUS_5_PERCENT);
        this.inventory.setStack(SIZE - 4, plus5PercentStack);
        ItemStack plus10PercentStack = Items.CYAN_CONCRETE.getDefaultStack();
        plus10PercentStack.set(DataComponentTypes.ITEM_NAME, PLUS_10_PERCENT);
        this.inventory.setStack(SIZE - 3, plus10PercentStack);
        
        ItemStack amountStack = Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE.getDefaultStack();
        amountStack.set(DataComponentTypes.MAX_STACK_SIZE, this.slot.count);
        amountStack.setCount(this.slot.count);
        amountStack.set(DataComponentTypes.ITEM_NAME,
            TextUtil.gray("Amount: ")
                .append(TextUtil.withColour(String.valueOf(this.slot.count), Formatting.AQUA)));
        amountStack.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal("[Left Click]")
                    .append(TextUtil.gray(" to increase amount")),
                Text.literal("[Right Click]")
                    .append(TextUtil.gray(" to decrease amount"))
            )
        );
        amountStack.set(
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            Unit.INSTANCE
        );
        this.inventory.setStack(SIZE - 2, amountStack);
        
        ItemStack exitStack = Items.BARRIER.getDefaultStack();
        exitStack.set(DataComponentTypes.ITEM_NAME, EXIT_SCREEN_TEXT);
        this.inventory.setStack(SIZE - 1, exitStack);
        
        buildPage();
    }
    
    private void buildPage() {
        int startModifierIndicies = page * 5 * 8;
        
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 8; column++) {
                int modifierIndex = startModifierIndicies + row * 8 + column;
                
                if (modifierIndex >= Modifiers.values().length) break;
                
                Modifiers modifier = Modifiers.values()[modifierIndex];
                Modifier proxyModifier = modifier.factory.makeModifier(null);
                
                ItemStack modifierStack = MiscUtil.getItemWithContext(proxyModifier, this.context);
                
                if (this.slot.modifier.equals(modifier)) {
                    modifierStack.set(
                        DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
                        true
                    );
                }
                
                this.inventory.setStack(row * 9 + column, modifierStack);
            }
        }
    }
    
    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity sPlayer) {
            if (
                actionType == SlotActionType.SWAP ||
                    actionType == SlotActionType.CLONE ||
                    actionType == SlotActionType.THROW ||
                    actionType == SlotActionType.PICKUP_ALL ||
                    actionType == SlotActionType.QUICK_CRAFT
            ) return;
            
            if (slotIndex % 9 < 8 && slotIndex < 53 - 9) {
                int modifierRow = (slotIndex / 9);
                int modifierColumn = (slotIndex - (slotIndex / 9)) % 8;
                int modifierIndex = modifierRow * 8 + modifierColumn;
                if (modifierIndex >= Modifiers.values().length) return;
                
                Modifiers modifier = Modifiers.values()[modifierIndex];
                LogUtils.getLogger().info(modifier.name);
                
                this.slot.modifier = modifier;
                
                this.buildPage();
                this.syncState();
            } else if (slotIndex == 8) {
                if (this.page >= (Modifiers.values().length / 9) / 2) return;
                this.page++;
                this.buildUI();
                this.syncState();
            } else if (slotIndex == 9 + 8) {
                if (this.page <= 0) return;
                this.page--;
                
                this.buildUI();
                this.syncState();
            } else if (slotIndex == SIZE - 1) {
                sPlayer.closeHandledScreen();
            } else if (slotIndex == SIZE - 2) {
                ClickType clickType = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
                
                if (clickType == ClickType.LEFT && this.slot.count < 99) this.slot.count++;
                else if (clickType == ClickType.RIGHT && this.slot.count > 1) this.slot.count--;
                
                this.buildUI();
                this.syncState();
            } else {
                int bump = new int[]{ -10, -5, -1, 0, 1, 5, 10 }[slotIndex - 45];
                this.slot.chance += bump / 100f;
                this.slot.chance = (float) Math.round(this.slot.chance * 100) / 100;
                this.slot.chance = Math.clamp(this.slot.chance, 0, 1);
                
                this.buildUI();
                this.syncState();
            }
        }
    }
    
    @Override
    public void onClosed(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity sPlayer) {
            Shadow shadow = getShadow(sPlayer.getServer());
            shadow.saveAsync();
            
            shadow.addTickable(Delay.instant(
                () -> shadow.config.modifierManager.showModifierListIndex(sPlayer, true)
            ));
        }
    }
    
    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }
    
    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
    
    public record Factory(Text name, ModifierSlot slot) implements NamedScreenHandlerFactory {
        
        @Override
        public Text getDisplayName() {
            return name;
        }
        
        @Override
        public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new ModifierSlotScreenHandler(
                syncId, slot,
                playerInventory,
                ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())
            );
        }
    }
}
