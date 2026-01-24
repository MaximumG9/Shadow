package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.roles.neutral.Pinata;
import com.maximumg9.shadow.roles.neutral.Spectator;
import com.maximumg9.shadow.roles.shadow.Oracle;
import com.maximumg9.shadow.roles.shadow.PoseidonsWrath;
import com.maximumg9.shadow.roles.shadow.ShadowRole;
import com.maximumg9.shadow.roles.villager.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public enum Roles {
    SPECTATOR(Spectator.FACTORY),

    VILLAGER(Villager.FACTORY),

    SHERIFF(Sheriff.FACTORY),
    LOOKER(Looker.FACTORY),
    LIFEWEAVER(Lifeweaver.FACTORY),
    LIFELINK(Lifelink.FACTORY),

    POSEIDONS_WRATH(PoseidonsWrath.FACTORY),
    SHADOW(ShadowRole.FACTORY),
    ORACLE(Oracle.FACTORY),

    PINATA(Pinata.FACTORY);
    
    
    public final String name;
    public final RoleFactory<?> factory;
    public final Faction faction;
    
    Roles(RoleFactory<?> factory) {
        this(factory.makeRole(null).getRawName(), factory, factory.makeRole(null).getFaction());
    }
    
    Roles(String name, RoleFactory<?> factory, Faction faction) {
        this.name = name;
        this.factory = factory;
        this.faction = faction;
    }
    
    public static CompletableFuture<Suggestions> suggest(CommandContext<?> ignoredCtx, SuggestionsBuilder builder) {
        Arrays.stream(values())
            .filter(
                (role) ->
                    Objects.requireNonNull(role.name)
                        .startsWith(builder.getRemaining())
            ).forEach(
                (role) ->
                    builder.suggest(role.name)
            );
        
        return builder.buildFuture();
    }
    
    public static Roles getRole(String roleName) {
        List<Roles> possibleRoles = Arrays.stream(values()).filter((role) -> role.name.equals(roleName)).toList();
        
        if (possibleRoles.isEmpty()) {
            throw new IllegalArgumentException("No role with name: " + roleName);
        }
        
        if (possibleRoles.size() > 1) {
            throw new IllegalStateException("Roles are broken, multiple roles with same name: " + roleName);
        }
        
        return possibleRoles.getFirst();
    }
    
    @NotNull
    public static Roles getRole(CommandContext<?> ctx, String argumentName) {
        String roleName = StringArgumentType.getString(ctx, argumentName);
        return getRole(roleName);
    }
}
