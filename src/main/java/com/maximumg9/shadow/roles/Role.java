package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.Tickable;
import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.items.AbilityStar;
import com.maximumg9.shadow.saving.Saveable;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public abstract class Role implements ItemRepresentable, Saveable, Tickable {
    
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

    public Optional<Ability> getAbility(Identifier id) {
        return this.abilities.stream().filter(ability -> ability.getID().equals(id)).findFirst();
    }
    
    public String aOrAn() { return "a"; }
    public abstract String getRawName();
    
    public abstract Style getStyle();

    public void onPlayerKill() {
        for(Ability ability : getAbilities()) {
            ability.onPlayerKill();
        }
    }
    
    public void onDeath(DamageSource damageSource) {
        for (Ability ability : this.getAbilities()) {
            ability.onDeath(damageSource);
        }
        for (IndirectPlayer indirect : player.getShadow().getAllPlayers()) {
            indirect.role.onAnyDeath(damageSource, player);
        }
    }

    public void onAnyDeath(DamageSource damageSource, IndirectPlayer deadPlayer) {
        for (Ability ability : this.getAbilities()) {
            ability.onAnyDeath(damageSource, deadPlayer);
        }
    }

    public void tick() {
        for(Ability ability : this.abilities) {
            ability.tick();
        }
    }
    
    public boolean hasAbility(Identifier id) {
        return this.abilities.stream().anyMatch(ability -> ability.getID().equals(id));
    }

    public boolean hasAnyOfAbilities(Collection<Identifier> abilities) {
        return this.abilities.stream().map(Ability::getID).anyMatch(abilities::contains);
    }

    public void addAbility(IndirectPlayer player, Ability.Factory abilityFactory) {
        this.abilities.add(abilityFactory.create(player));
    }

    public void removeAbility(Ability ability) {
        this.abilities.remove(ability);
    }
    
    public NbtCompound writeNBT(NbtCompound nbt) {
        nbt.putString("role", this.getRawName());
        return nbt;
    }

    public void onJoin() {
        for (Ability ability : getAbilities()) {
            ability.onJoin();
        }
    }

    public void onLeave() {
        Text name = this.getName();
        Shadow shadow = player.getShadow();

        shadow.addTickable(
            Delay.of(
                () -> {
                    if (this.player.getOfflineTicks() >= shadow.config.disconnectTime && this.player.role.getFaction() != Faction.SPECTATOR) {
                        shadow.broadcast(
                            name.copy().styled(style -> style.withColor(Formatting.YELLOW))
                                .append(
                                    Text.literal(
                                        " has been disconnected for too long"
                                    )
                                )
                        );
                        shadow.checkWin(this.player.playerUUID);
                    }
                },
                shadow.config.disconnectTime
            )
        );

        for (Ability ability : getAbilities()) {
            ability.onLeave();
        }
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
    
    public void readNBT(NbtCompound nbt) { }
    
    public void init() {
        player.addToTeam(player.getShadow().playerTeam, CancelPredicates.cancelOnLostRole(this));

        player.giveItem(
            this.player.getShadow().config.food.foodGiver.apply(
                this.player.getShadow().config.foodAmount
            ),
            MiscUtil.DELETE_WARN,
            CancelPredicates.cancelOnLostRole(this)
        );
        
        ItemStack abilitySelector = Items.NETHER_STAR.getDefaultStack();
        
        abilitySelector.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.withColour("Ability Star",Formatting.YELLOW)
        );
        
        player.giveItem(
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
            MiscUtil.DELETE_WARN,
            CancelPredicates.cancelOnLostRole(this)
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
