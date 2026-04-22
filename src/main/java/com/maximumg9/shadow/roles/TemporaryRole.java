package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.roles.shadow.AbstractShadow;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TemporaryRole extends Role {
    public static final RoleFactory<TemporaryRole> FACTORY = new Factory();
    private static final Style STYLE = Style.EMPTY.withColor(Formatting.WHITE);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.STRUCTURE_VOID);
    private Faction faction;

    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new TemporaryRole(null, Faction.SPECTATOR).getName());
    }

    public TemporaryRole(@Nullable IndirectPlayer player, Faction faction) {
        super(player, List.of());
        this.faction = faction;
    }

    @Override
    public Faction getFaction() {return faction;}
    @Override
    public SubFaction getSubFaction() { return SubFaction.TEMPORARY; }
    @Override
    public String getRawName() { return "Temporary Role"; }
    @Override
    public Style getStyle() { return STYLE; }
    @Override
    public Roles getRole() {
        return Roles.TEMP_ROLE;
    }
    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) { return ITEM_STACK.copy(); }

    @Override
    public void roleInit() {
        super.roleInit();

        if (faction == Faction.SHADOW) AbstractShadow.announceShadowPartners(this.player);
    }

    @Override
    public void readNBT(NbtCompound nbt) {
        faction = Faction.getFaction(nbt.getString("faction"));
        super.readNBT(nbt);
    }

    @Override
    public NbtCompound writeNBT(NbtCompound nbt) {
        nbt.putString("faction", faction.name());
        return super.writeNBT(nbt);
    }

    private static class Factory implements RoleFactory<TemporaryRole> {
        @Override
        public TemporaryRole makeRole(@Nullable IndirectPlayer player) {
            return new TemporaryRole(player, Faction.SPECTATOR);
        }
    }
}
