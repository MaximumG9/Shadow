package com.maximumg9.shadow.roles;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum Faction {
    VILLAGER(
        Text.literal("Villager")
            .styled(style -> style.withColor(Formatting.GREEN))
    ),
    NEUTRAL(
        Text.literal("Neutral")
            .styled(style -> style.withColor(Formatting.GRAY))
    ),
    SHADOW(
        Text.literal("Shadow")
            .styled(style -> style.withColor(Formatting.RED))
    ),
    SPECTATOR(
        Text.literal("Spectator")
            .styled(style -> style.withColor(Formatting.GRAY))
    );
    
    public final Text name;
    Faction(Text name) {
        this.name = name;
    }
}
