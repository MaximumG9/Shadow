package com.maximumg9.shadow.config;

import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.Formatting;

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
}
