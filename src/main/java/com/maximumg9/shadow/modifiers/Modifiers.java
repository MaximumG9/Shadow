package com.maximumg9.shadow.modifiers;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public enum Modifiers {
    QUICK_START(QuickStart.FACTORY),
    SHADOW_THE_HEDGEHOG(ShadowTheHedgehog.FACTORY);
    
    public final String name;
    public final ModifierFactory<?> factory;
    
    Modifiers(ModifierFactory<?> factory) {
        this(factory.makeModifier(null).getRawName(), factory);
    }
    
    Modifiers(String name, ModifierFactory<?> factory) {
        this.name = name;
        this.factory = factory;
    }
    
    public static CompletableFuture<Suggestions> suggest(CommandContext<?> ignoredCtx, SuggestionsBuilder builder) {
        Arrays.stream(values())
            .filter(
                (modifier) ->
                    Objects.requireNonNull(modifier.name)
                        .startsWith(builder.getRemaining())
            ).forEach(
                (modifier) ->
                    builder.suggest(modifier.name)
            );
        
        return builder.buildFuture();
    }
    
    public static Modifiers getModifier(String modifierName) {
        List<Modifiers> possibleModifiers = Arrays.stream(values()).filter((modifier) -> modifier.name.equals(modifierName)).toList();
        
        if (possibleModifiers.isEmpty()) {
            throw new IllegalArgumentException("No modifier with name: " + modifierName);
        }
        
        if (possibleModifiers.size() > 1) {
            throw new IllegalStateException("Modifiers are broken, multiple modifiers with same name: " + modifierName);
        }
        
        return possibleModifiers.getFirst();
    }
    
    @NotNull
    public static Modifiers getModifier(CommandContext<?> ctx, String argumentName) {
        String modifierName = StringArgumentType.getString(ctx, argumentName);
        return getModifier(modifierName);
    }
}
