package com.maximumg9.shadow.screens;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.config.Config;
import com.maximumg9.shadow.config.Food;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import net.minecraft.component.type.LoreComponent;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public class ConfigScreenHandler extends ShadowScreenHandler {
    private static final int SIZE = 9 * 3;
    private static final Text DEBUG_INFO_PAPER = TextUtil.withColour("Modify these configs and more with /$config [config]", Formatting.WHITE);
    private static final Text EYE_COUNT = TextUtil.withColour("# of Placed Eyes: ", Formatting.WHITE);
    private static final LoreComponent TEMP = MiscUtil.makeLore(Text.literal("[Left Click]").append(TextUtil.gray(" to increase amount")), Text.literal("[Right Click]").append(TextUtil.gray(" to decrease amount")), Text.literal("[Drop]").append(TextUtil.gray(" to reset to default")));
    private static final List<Food> FOOD_LIST = Arrays.stream(Food.values()).toList();

    private final SimpleInventory inventory;
    private final Config config;
    private final boolean editable;
    private final ScreenHandlerContext context;

    private ConfigScreenHandler(int syncID, PlayerInventory playerInventory, ScreenHandlerContext context, Config config, boolean editable) {
        super(ScreenHandlerType.GENERIC_9X3, syncID, playerInventory);

        this.inventory = new SimpleInventory(SIZE);

        this.config = config;
        this.editable = editable;
        this.context = context;

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

    /// ,--------------------------,
    /// | EYES FOOD CHAT CULL BRDR |
    /// | EYES      GRCE           |
    /// | EYES                PAPR |
    /// '--------------------------'
    ///
    @SuppressWarnings("PointlessArithmeticExpression")
    private void buildUI() {
        ItemStack eyeOverworldStack = Items.GRASS_BLOCK.getDefaultStack();
        eyeOverworldStack.set(DataComponentTypes.ITEM_NAME, Text.literal("(Overworld) ").append(EYE_COUNT).append(Text.literal(String.valueOf(config.overworldEyes))));
        eyeOverworldStack.setCount(config.overworldEyes);
        this.inventory.setStack((0*9), eyeOverworldStack);

        ItemStack eyeNetherStack = Items.NETHERRACK.getDefaultStack();
        eyeNetherStack.set(DataComponentTypes.ITEM_NAME, Text.literal("(Nether) ").append(EYE_COUNT).append(Text.literal(String.valueOf(config.netherEyes))));
        eyeNetherStack.setCount(config.netherEyes);
        this.inventory.setStack((1*9), eyeNetherStack);

        ItemStack eyeRoofStack = Items.BEDROCK.getDefaultStack();
        eyeRoofStack.set(DataComponentTypes.ITEM_NAME, Text.literal("(Roof) ").append(EYE_COUNT).append(Text.literal(String.valueOf(config.netherRoofEyes))));
        eyeRoofStack.setCount(config.netherRoofEyes);
        this.inventory.setStack((2*9), eyeRoofStack);


        ItemStack foodStack = MiscUtil.getItemWithContext(config.food,context);
        foodStack.set(DataComponentTypes.ITEM_NAME, Text.literal(config.food.name()));
        foodStack.setCount(config.foodAmount);
        foodStack.set(DataComponentTypes.LORE, MiscUtil.makeLore(Text.literal("[Middle Click]").append(TextUtil.gray(" to cycle options"))));
        this.inventory.setStack(1+(0*9), foodStack);


        ItemStack chatStack = Items.NAME_TAG.getDefaultStack();
        if(config.disableChat) chatStack.set(DataComponentTypes.ITEM_NAME, TextUtil.gray("[DISABLED] Chat Cooldown: ").append(TextUtil.gray(String.valueOf(config.chatMessageCooldown/20))).append(TextUtil.gray("s")));
        else chatStack.set(DataComponentTypes.ITEM_NAME, Text.literal("Chat Cooldown: ").append(Text.literal(String.valueOf(config.chatMessageCooldown/20))).append(Text.literal("s")));
        chatStack.set(DataComponentTypes.LORE, MiscUtil.makeLore(Text.literal("[Middle Click]").append(TextUtil.gray(" to Toggle"))));
        this.inventory.setStack(2+(0*9),chatStack);

        ItemStack graceStack = Items.IRON_SWORD.getDefaultStack();
        graceStack.set(DataComponentTypes.ITEM_NAME, Text.literal("Grace Period Timer: ").append(Text.literal(String.valueOf(config.gracePeriodTicks/20))).append(Text.literal("s")));
        this.inventory.setStack(2+(1*9),graceStack);


        ItemStack cullStack = Items.NETHERITE_SWORD.getDefaultStack();
        cullStack.set(DataComponentTypes.ITEM_NAME, Text.literal("Cull Radius: ").append(Text.literal(String.valueOf(config.cullRadius))).append(Text.literal(" blocks")));
        this.inventory.setStack(3+(0*9),cullStack);


        ItemStack borderStack = Items.STRUCTURE_VOID.getDefaultStack();
        borderStack.set(DataComponentTypes.ITEM_NAME, TextUtil.withColour("Worldborder Size: ", Formatting.WHITE).append(Text.literal(String.valueOf(config.worldBorderSize))).append(Text.literal(" blocks (Diameter)")));
        this.inventory.setStack(4+(0*9),borderStack);


        ItemStack infoStack = Items.PAPER.getDefaultStack();
        infoStack.set(DataComponentTypes.ITEM_NAME, DEBUG_INFO_PAPER);
        infoStack.set(DataComponentTypes.LORE, TEMP);
        this.inventory.setStack(SIZE - 1, infoStack);
    }

    @Override
    @SuppressWarnings("PointlessArithmeticExpression")
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {

        if (actionType == SlotActionType.SWAP ||
            actionType == SlotActionType.PICKUP_ALL ||
            actionType == SlotActionType.QUICK_CRAFT
        ) return;

        ClickType clickType = button == 0 ? ClickType.LEFT : ClickType.RIGHT;

        if(slotIndex == 0) {
            if (actionType == SlotActionType.THROW) config.overworldEyes = 8;
            else if (clickType == ClickType.LEFT) config.overworldEyes++;
            else if (config.overworldEyes > 1) config.overworldEyes--;
        }
        if(slotIndex == 0+9) {
            if (actionType == SlotActionType.THROW) config.netherEyes = 8;
            else if (clickType == ClickType.LEFT) config.netherEyes++;
            else if (config.netherEyes > 1) config.netherEyes--;
        }
        if(slotIndex == 0+(2*9)) {
            if (actionType == SlotActionType.THROW) config.netherRoofEyes = 8;
            else if (clickType == ClickType.LEFT) config.netherRoofEyes++;
            else if (config.netherRoofEyes > 1) config.netherRoofEyes--;
        }

        if(slotIndex == 1) {
            if (actionType == SlotActionType.THROW) {
                config.foodAmount = 16;
                config.food = Food.BREAD;
            }
            else if (actionType == SlotActionType.CLONE) config.food = FOOD_LIST.indexOf(config.food) == FOOD_LIST.size() - 1 ? FOOD_LIST.getFirst() : FOOD_LIST.get(1+FOOD_LIST.indexOf(config.food)); // doesnt work in survival, so were just gonna put a rug over it and make configs creative only LMAO
            else if (clickType == ClickType.LEFT) config.foodAmount++;
            else if (config.foodAmount > 1) config.foodAmount--;
        }

        if(slotIndex == 2) {
            if (actionType == SlotActionType.THROW) {
                config.chatMessageCooldown = 30 * 20;
                config.disableChat = false;
            }
            else if (actionType == SlotActionType.CLONE) config.disableChat = !config.disableChat;
            else if (clickType == ClickType.LEFT) config.chatMessageCooldown += 20;
            else if (config.chatMessageCooldown > 20) config.chatMessageCooldown -= 20;
        }
        if(slotIndex == 2+(1*9)) {
            if (actionType == SlotActionType.THROW) config.gracePeriodTicks = 180 * 20;
            else if (clickType == ClickType.LEFT) config.gracePeriodTicks += 20;
            else if (config.gracePeriodTicks > 20) config.gracePeriodTicks -= 20;
        }

        if(slotIndex == 3) {
            if (actionType == SlotActionType.THROW) config.cullRadius = 18;
            else if (clickType == ClickType.LEFT) config.cullRadius+=0.5;
            else if (config.chatMessageCooldown > 1) config.cullRadius-=0.5;
        }

        if(slotIndex == 4) {
            if (actionType == SlotActionType.THROW) config.worldBorderSize = 150;
            else if (clickType == ClickType.LEFT) config.worldBorderSize++;
            else if (config.worldBorderSize > 1) config.worldBorderSize--;
        }

        this.buildUI();
        this.syncState();
    }

    public void onClosed(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity sPlayer) {
            Shadow shadow = getShadow(sPlayer.getServer());
            shadow.saveAsync();
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

    public record Factory(Text name, Config config, boolean editable) implements NamedScreenHandlerFactory {

        @Override
        public Text getDisplayName() {
            return name;
        }

        @Override
        public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new ConfigScreenHandler(
                syncId,
                playerInventory,
                ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()),
                this.config, this.editable
            );
        }
    }
}
