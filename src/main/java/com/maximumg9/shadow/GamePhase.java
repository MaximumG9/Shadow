package com.maximumg9.shadow;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public enum GamePhase {
    NOT_PLAYING(true),
    LOCATION_SELECTED(false),
    PLAYING(false),
    WON(true);
    
    public final boolean canSelectLocation;
    
    GamePhase(boolean canSelectLocation) {
        this.canSelectLocation = canSelectLocation;
    }
    
    public static CompletableFuture<Suggestions> suggest(CommandContext<?> ctx, SuggestionsBuilder builder) {
        Arrays.stream(values())
            .filter(
                (phase) ->
                    Objects.requireNonNull(phase.name())
                        .startsWith(builder.getRemaining())
            ).forEach(
                (phase) ->
                    builder.suggest(phase.name())
            );
        
        return builder.buildFuture();
    }
    
    @NotNull
    public static GamePhase getPhase(CommandContext<?> ctx, String name) {
        String roleName = StringArgumentType.getString(ctx, name);
        
        List<GamePhase> possiblePhases = Arrays.stream(values()).filter((phase) -> phase.name().equals(roleName)).toList();
        
        if (possiblePhases.size() != 1) {
            throw new IllegalStateException("Phases are broken, multiple phases with same name");
        }
        
        return possiblePhases.getFirst();
    }
}
