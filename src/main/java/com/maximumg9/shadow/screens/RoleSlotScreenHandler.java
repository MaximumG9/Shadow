package com.maximumg9.shadow.screens;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.RoleSlot;
import com.maximumg9.shadow.roles.Roles;
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
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public class RoleSlotScreenHandler extends ShadowScreenHandler {
    private static final int SIZE = 9 * 6;
    private static final Text NEXT_PAGE_TEXT = TextUtil.gold("Next Page");
    private static final Text LAST_PAGE_TEXT = TextUtil.gold("Last Page");
    private static final Text EXIT_SCREEN_TEXT = TextUtil.red("Return to role menu");
    private final RoleSlot slot;
    
    private final SimpleInventory inventory;
    private final ScreenHandlerContext context;
    private int page = 0;
    
    private RoleSlotScreenHandler(int syncID, RoleSlot slot, PlayerInventory playerInventory, ScreenHandlerContext context) {
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
        if (this.page < (Roles.values().length / 9) / 2) {
            ItemStack nextPageStack = Items.SPECTRAL_ARROW.getDefaultStack();
            nextPageStack.set(DataComponentTypes.ITEM_NAME, NEXT_PAGE_TEXT);
            this.inventory.setStack(8, nextPageStack);
        }
        if (this.page > 0) {
            ItemStack nextPageStack = Items.SPECTRAL_ARROW.getDefaultStack();
            nextPageStack.set(DataComponentTypes.ITEM_NAME, LAST_PAGE_TEXT);
            this.inventory.setStack(9 + 8, nextPageStack);
        }
        
        ItemStack exitStack = Items.BARRIER.getDefaultStack();
        exitStack.set(DataComponentTypes.ITEM_NAME, EXIT_SCREEN_TEXT);
        this.inventory.setStack(SIZE - 1, exitStack);
        
        buildPage();
    }
    
    private void buildPage() {
        int startRoleIndicies = page * 6 * 8;
        
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 8; column++) {
                int roleIndex = startRoleIndicies + row * 8 + column;
                
                if (roleIndex >= Roles.values().length) break;
                
                Roles role = Roles.values()[roleIndex];
                Role proxyRole = role.factory.makeRole(null);
                
                int weight = slot.getWeight(role);
                
                ItemStack roleStack = MiscUtil.getItemWithContext(proxyRole, this.context);
                roleStack.set(
                    DataComponentTypes.LORE,
                    MiscUtil.makeLore(
                        Text.literal("Weight: ")
                            .append(
                                Text.literal(String.valueOf(weight))
                            ),
                        Text.literal("[Left Click]")
                            .append(
                                TextUtil.gray(" to increase weight")
                            ),
                        Text.literal("[Right Click]")
                            .append(
                                TextUtil.gray(" to decrease weight")
                            )
                    )
                );
                
                if (weight > 0) {
                    roleStack.set(
                        DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
                        true
                    );
                }
                
                int count = MathHelper.clamp(weight, 1, 99);
                roleStack.setCount(count);
                roleStack.set(
                    DataComponentTypes.MAX_STACK_SIZE,
                    count
                );
                
                this.inventory.setStack(row * 9 + column, roleStack);
            }
        }
    }
    
    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity sPlayer) {
            // Yeah, I'm a bit of a math guy
            if (slotIndex % 9 < 8) {
                if (actionType == SlotActionType.SWAP ||
                    actionType == SlotActionType.CLONE ||
                    actionType == SlotActionType.THROW ||
                    actionType == SlotActionType.PICKUP_ALL ||
                    actionType == SlotActionType.QUICK_CRAFT
                ) return;
                
                ClickType clickType = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
                
                // Oh, also sorry to whoever works with this next, but I just wanted to do it this way :P
                // what the fuck
                
                int roleRow = (slotIndex / 9);
                
                int roleColumn = (slotIndex - (slotIndex / 9)) % 8;
                
                int roleIndex = roleRow * 8 + roleColumn;
                
                if (roleIndex >= Roles.values().length) return;
                
                Roles role = Roles.values()[roleIndex];
                LogUtils.getLogger().info(role.name);
                
                if (clickType == ClickType.LEFT) {
                    this.slot.setWeight(role, this.slot.getWeight(role) + 1);
                } else if (this.slot.weightSum() > 1 && this.slot.getWeight(role) > 0) {
                    this.slot.setWeight(role, this.slot.getWeight(role) - 1);
                }
                // nothing for clicking the role icon for now!
                
                this.buildPage();
                this.syncState();
            } else if (slotIndex == 8) {
                if (this.page >= (Roles.values().length / 9) / 2) return;
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
            }
        }
    }
    
    @Override
    public void onClosed(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity sPlayer) {
            Shadow shadow = getShadow(sPlayer.getServer());
            shadow.saveAsync();
            
            shadow.addTickable(Delay.instant(
                () -> shadow.config.roleManager.showRoleListIndex(sPlayer, true)
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
    
    public record Factory(Text name, RoleSlot slot) implements NamedScreenHandlerFactory {
        
        @Override
        public Text getDisplayName() {
            return name;
        }
        
        @Override
        public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new RoleSlotScreenHandler(
                syncId, slot,
                playerInventory,
                ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())
            );
        }
    }
}
