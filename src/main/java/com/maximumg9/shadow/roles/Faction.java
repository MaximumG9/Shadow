package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.TextUtil;
import net.minecraft.text.Text;

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
}
