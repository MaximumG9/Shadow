package com.maximumg9.shadow.abilities;

import com.google.common.collect.ImmutableList;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.TextUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SpecialistSelectKit extends Ability {
    public static final Identifier ID = MiscUtil.shadowID("specialist_kit_select");
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = new ItemStack(Items.DAMAGED_ANVIL);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            TextUtil.gray("Select Kit")
        );
    }

    public SpecialistSelectKit(IndirectPlayer player) {
        super(player);
    }

    @Override
    public Identifier getID() {
        return ID;
    }

    @Override
    public AbilityResult apply() {
        this.player.getPlayerOrThrow().openHandledScreen(
            new DecisionScreenHandler.Factory<Kit>(
                Text.literal("Kit to select"),
                (kit, clicker, _a, _b) -> {
                    if (kit == null) {
                        clicker.sendMessage(TextUtil.red("No kit selected"));
                        return;
                    }
                    RegistryWrapper.WrapperLookup reg = clicker.server.getRegistryManager();
                    for(Function<RegistryWrapper.WrapperLookup,ItemStack> itemSupplier : kit.items) {
                        this.player.giveItemOrThrow(
                            itemSupplier.apply(reg),
                            MiscUtil.DROP
                        );
                    }
                    this.player.sendMessageOrThrow(Text.literal("Got ").append(kit.name));
                    this.player.role.removeAbility(this);
                },
                List.of(Kit.values()),
                true
            )
        );
        return AbilityResult.NO_CLOSE;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }

    public enum Kit implements ItemRepresentable {
        SLIME(
            TextUtil.green("Slime!"),
            MiscUtil.makeLore(TextUtil.green("64x Slimeball")),
            Items.SLIME_BALL,
            ImmutableList.of(
                (reg) -> Items.SLIME_BALL.getDefaultStack().copyWithCount(64)
            )
        ),
        POTIONS(
            TextUtil.green("Random Potions!"),
            MiscUtil.makeLore(TextUtil.blue("3x Random Splash Potion (VERY RANDOM)")),
            Items.SPLASH_POTION,
            ImmutableList.of(
                (reg) -> createRandomPotion(),
                (reg) -> createRandomPotion(),
                (reg) -> createRandomPotion()
            )
        ),
        ANVIL(
            TextUtil.green("Anvil & Exp!"),
            MiscUtil.makeLore(
                TextUtil.gray("1x Anvil"),
                TextUtil.green("32x Bottle o' Enchanting")
            ),
            Items.ANVIL,
            ImmutableList.of(
                (reg) -> Items.DAMAGED_ANVIL.getDefaultStack(),
                (reg) -> Items.EXPERIENCE_BOTTLE.getDefaultStack().copyWithCount(32)
            )
        ),
        ARROWS(
            TextUtil.green("Tipped Arrows!"),
            MiscUtil.makeLore(
                TextUtil.gold("4x Strength II Tipped Arrow (0:11)"),
                TextUtil.blue("4x Swiftness I Tipped Arrow (1:00)"),
                TextUtil.gray("4x Slowness IV & Resistance III Tipped Arrow (0:05)")
            ),
            Items.TIPPED_ARROW,
            ImmutableList.of(
                (reg) -> tippedArrowWithPotion(Potions.STRONG_STRENGTH).copyWithCount(4),
                (reg) -> tippedArrowWithPotion(Potions.LONG_SWIFTNESS).copyWithCount(4),
                (reg) -> tippedArrowWithPotion(Potions.LONG_TURTLE_MASTER).copyWithCount(4)
            )
        ),
        FIREWORKS(
            TextUtil.green("Dangerous Fireworks!"),
            MiscUtil.makeLore(
                TextUtil.withColour("4x Dangerous Purple and Black Fireworks", Formatting.DARK_PURPLE)
            ),
            Items.FIREWORK_ROCKET,
            ImmutableList.of(
                (reg) -> {
                    ItemStack fireworks = Items.FIREWORK_ROCKET.getDefaultStack();

                    List<FireworkExplosionComponent> explosions = new ArrayList<>();

                    for (int i = 0; i < 7; i++) {
                        explosions.add(
                            new FireworkExplosionComponent(
                                FireworkExplosionComponent.Type.SMALL_BALL,
                                IntImmutableList.of(
                                    DyeColor.PURPLE.getFireworkColor(),
                                    DyeColor.BLACK.getFireworkColor()
                                ),
                                new IntArrayList(),
                                false,
                                false
                            )
                        );
                    }

                    fireworks.set(
                        DataComponentTypes.FIREWORKS,
                        new FireworksComponent(1, explosions)
                    );

                    fireworks.setCount(4);

                    return fireworks;
                }
            )
        ),
        SHULKER_BOX(
            TextUtil.green("Storage!"),
            MiscUtil.makeLore(
                TextUtil.withColour("1x Shulker Box", Formatting.LIGHT_PURPLE),
                TextUtil.withColour("1x Bundle", Formatting.GRAY)
            ),
            Items.SHULKER_BOX,
            ImmutableList.of(
                (reg) -> {
                    ItemStack shulker = Items.SHULKER_BOX.getDefaultStack();

                    shulker.set(
                        DataComponentTypes.CONTAINER,
                        ContainerComponent.fromStacks(List.of(Items.BUNDLE.getDefaultStack()))
                    );
                    return shulker;
                }
            )
        );

        private static final Random KIT_RANDOM = Random.create();

        private static ItemStack createRandomPotion() {
            IndexedIterable<RegistryEntry<StatusEffect>> effects = Registries.STATUS_EFFECT.getIndexedEntries();
            int effectIndex = KIT_RANDOM.nextInt(effects.size());
            RegistryEntry<StatusEffect> effect = effects.get(effectIndex);
            ItemStack potion = Items.SPLASH_POTION.getDefaultStack();
            boolean upgraded = KIT_RANDOM.nextBoolean();
            potion.set(
                DataComponentTypes.POTION_CONTENTS,
                new PotionContentsComponent(
                    Optional.empty(),
                    Optional.empty(),
                    List.of(
                        new StatusEffectInstance(
                            effect,
                            upgraded ? 20 * 10 : 20 * 30,
                            upgraded ? 2 - 1 : 1 - 1
                        )
                    )
                )
            );
            return potion;
        }

        private static ItemStack tippedArrowWithPotion(RegistryEntry<Potion> potion) {
            ItemStack tippedArrow = new ItemStack(Items.TIPPED_ARROW);
            tippedArrow.set(
                DataComponentTypes.POTION_CONTENTS,
                new PotionContentsComponent(potion)
            );
            return tippedArrow;
        }

        final Text name;
        final ItemStack representingItemStack;
        final ImmutableList<Function<RegistryWrapper.WrapperLookup,ItemStack>> items;

        Kit(Text name, LoreComponent description, Item representingItem, ImmutableList<Function<RegistryWrapper.WrapperLookup,ItemStack>> items) {
            this.name = name;
            this.items = items;
            this.representingItemStack = new ItemStack(representingItem);
            this.representingItemStack.set(
                DataComponentTypes.LORE,
                description
            );
        }

        @Override
        public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
            return this.representingItemStack.copy();
        }
    }
}
