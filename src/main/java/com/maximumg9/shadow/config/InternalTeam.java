package com.maximumg9.shadow.config;

import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;

public enum InternalTeam {
    PLAYER("Players", AbstractTeam.VisibilityRule.NEVER),
    SPECTATOR("Spectators", AbstractTeam.VisibilityRule.NEVER, Formatting.DARK_GRAY),

    MARKED("DuskMarked", AbstractTeam.VisibilityRule.NEVER, Formatting.RED),
    SHIELDED("Shielded", AbstractTeam.VisibilityRule.NEVER, Formatting.AQUA);

    public final String teamName;
    public final AbstractTeam.VisibilityRule nametagVisibility;
    public final Formatting color;

    InternalTeam(String teamName, AbstractTeam.VisibilityRule nametagVisibility, Formatting color) {
        this.teamName = teamName;
        this.nametagVisibility = nametagVisibility;
        this.color = color;
    }

    InternalTeam(String name, AbstractTeam.VisibilityRule nametagVisibility) { this(name, nametagVisibility, Formatting.WHITE); }

    public static InternalTeam getTeam(String teamName) {
        List<InternalTeam> possibleTeams = Arrays.stream(values()).filter((faction) -> faction.name().equals(teamName)).toList();

        if (possibleTeams.isEmpty()) {
            throw new IllegalArgumentException("No team with name: " + teamName);
        }

        if (possibleTeams.size() > 1) {
            throw new IllegalStateException("Found more than 1 team with name " + teamName);
        }

        return possibleTeams.getFirst();
    }
}
