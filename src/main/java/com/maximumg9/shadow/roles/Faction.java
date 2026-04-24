package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.TextUtil;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

public enum Faction {
    VILLAGER(
        TextUtil.green("Villager")
    ),
    NEUTRAL(
        TextUtil.gray("Neutral")
    ),
    SHADOW(
        TextUtil.red("Shadow")
    ),
    SPECTATOR(
        TextUtil.gray("Spectator")
    );
    
    public final Text name;
    Faction(Text name) {
        this.name = name;
    }

    public static Faction getFaction(String factionName) {
        List<Faction> possibleFactions = Arrays.stream(values()).filter((faction) -> faction.name().equals(factionName)).toList();

        if (possibleFactions.isEmpty()) {
            throw new IllegalArgumentException("No faction with name: " + factionName);
        }

        if (possibleFactions.size() > 1) {
            throw new IllegalStateException("Found more than 1 faction with name " + factionName);
        }

        return possibleFactions.getFirst();
    }
}
