package com.maximumg9.shadow.items;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.AbilityResult;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public class AbilityStar implements ItemUseCallback {
    @Unique
    public static final Identifier ID = MiscUtil.shadowID("ability_star");
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld)) {
            return null;
        }
        
        ItemStack stack = user.getStackInHand(hand);
        
        Shadow shadow = getShadow(world.getServer());
        
        Role role = shadow.getIndirect((ServerPlayerEntity) user).role;
        
        if (role == null) {
            return TypedActionResult.fail(stack);
        }

        ArrayList<ItemRepresentable> abilities = getAbilities((ServerPlayerEntity) user, role, shadow);
        user.openHandledScreen(new DecisionScreenHandler.Factory<>(
            Text.literal("Ability Menu"),
            (ability, clicker, _a, _b) -> {
                if (ability instanceof Ability) {
                    AbilityResult result = ((Ability) ability).triggerApply();
                    if (result.close) {
                        clicker.closeHandledScreen();
                    }
                }
            },
            abilities,
            false
        ));
        
        
        return TypedActionResult.success(stack, false);
    }

    private static @NotNull ArrayList<ItemRepresentable> getAbilities(ServerPlayerEntity user, Role role, Shadow shadow) {
        ArrayList<ItemRepresentable> abilities =
            new ArrayList<>(role.getAbilities());
        abilities.addFirst(
            wrapper -> {
                ItemStack roleCard = new ItemStack(Items.WRITABLE_BOOK);
                IndirectPlayer player = shadow.getIndirect(user);
                assert player.role != null;

                roleCard.set(
                    DataComponentTypes.ITEM_NAME,
                    player.role.getName()
                );
                roleCard.set(
                    DataComponentTypes.LORE,
                    MiscUtil.makeLore(
                        TextUtil.gray("Subfaction: ").append(player.role.getSubFaction().name),
                        TextUtil.gray("Your alignment: ").append(player.role.getFaction().name)
                    )
                );
                roleCard.set(
                    DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
                    true
                );

                return roleCard;
            });
        return abilities;
    }
}
