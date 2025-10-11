package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.items.AbilityStar;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Role implements ItemRepresentable {
    
    protected final IndirectPlayer player;
    private final List<Ability> abilities = new ArrayList<>();
    
    protected Role(IndirectPlayer player, List<Ability.Factory> abilityFactories) {
        this.player = player;
        abilityFactories.forEach((factory) -> abilities.add(factory.create(player)));
    }
    public static Role load(NbtCompound nbt, IndirectPlayer player) {
        String roleName = nbt.getString("name");
        if (Objects.equals(roleName, "")) return null;
        Roles role = Roles.getRole(roleName);
        
        return role.factory.fromNBT(nbt, player);
    }
    public abstract Faction getFaction();
    
    public abstract SubFaction getSubFaction();
    
    public List<Ability> getAbilities() {
        return this.abilities;
    }
    
    public String aOrAn() { return "a"; }
    public abstract String getRawName();
    
    public abstract Style getStyle();
    
    public void onDeath() {

    }
    
    public boolean hasAbility(Identifier id) {
        return this.abilities.stream().anyMatch(ability -> ability.getID().equals(id));
    }
    
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putString("role", this.getRawName());
        return nbt;
    }
    
    public void onNight() {
        // Cursed forcing to send an update on the flags
        this.player.getPlayer().ifPresent(
            (p) -> p.getDataTracker().set(
                Entity.FLAGS,
                p.getDataTracker().get(Entity.FLAGS),
                true
            )
        );
        this.abilities.forEach(Ability::onNight);
    }
    
    public void onDay() {
        // Cursed forcing to send an update on the flags
        this.player.getPlayer().ifPresent(
            (p) -> p.getDataTracker().set(
                Entity.FLAGS,
                p.getDataTracker().get(Entity.FLAGS),
                true
            )
        );
        this.abilities.forEach(Ability::onDay);
    }
    
    public void readNbt(NbtCompound nbt) { }
    
    public void init() {
        player.giveItemNow(
            this.player.getShadow().config.food.foodGiver.apply(
                this.player.getShadow().config.foodAmount
            ),
            MiscUtil.DELETE_WARN
        );
        
        ItemStack abilitySelector = Items.NETHER_STAR.getDefaultStack();
        
        abilitySelector.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.withColour("Ability Star",Formatting.YELLOW)
        );
        
        player.giveItemNow(
            NBTUtil.flagDisableAttributes(
                NBTUtil.flagRestrictMovement(
                    NBTUtil.flagAsInvisible(
                        NBTUtil.addID(
                            abilitySelector,
                            AbilityStar.ID
                        )
                    )
                )
            ),
            MiscUtil.DELETE_WARN
        );
        
        player.sendMessage(
            Text.literal("You are " + this.aOrAn() + " ")
                .setStyle(this.getStyle())
                .append(this.getName()),
            CancelPredicates.cancelOnPhaseChange(this.player.getShadow().state.phase)
        );
        
        this.player.scheduleUntil(
            (p) -> {
                p.setGlowing(true);
                p.getDataTracker().set(
                    Entity.FLAGS,
                    (byte) (p.getDataTracker().get(Entity.FLAGS) |
                        (1 << Entity.GLOWING_FLAG_INDEX)),
                    true
                );
            },
            CancelPredicates.NEVER_CANCEL
        );
        
        this.player.giveEffect(
            new StatusEffectInstance(
                StatusEffects.HASTE,
                -1, 1,
                false, false,
                true
            ),
            CancelPredicates.NEVER_CANCEL
        );
        this.player.giveEffect(
            new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE,
                10 * 20, 0,
                false, false,
                true
            ),
            CancelPredicates.cancelOnPhaseChange(this.player.getShadow().state.phase)
        );
        this.abilities.forEach(Ability::init);
    }
    
    public abstract Roles getRole();
    
    public void deInit() {
        this.abilities.forEach(Ability::deInit);
    }
    
    public Text getName() {
        return Text
            .literal(getRawName())
            .setStyle(getStyle());
    }
}
