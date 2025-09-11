package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.TextUtil;
import net.minecraft.text.Text;

public enum SubFaction {
    VILLAGER_KILLING(
        TextUtil.green("Villager")
            .append(TextUtil.blue(" Killing"))
    ),
    VILLAGER_SUPPORT(
        TextUtil.green("Villager")
            .append(TextUtil.blue(" Support"))
    ),
    VILLAGER_OUTLIER(
        TextUtil.green("Villager")
            .append(TextUtil.blue(" Outlier"))
    ),
    NEUTRAL_CHAOS(
        TextUtil.gray("Neutral")
            .append(TextUtil.blue(" Chaos"))
    ),
    SHADOW(
        TextUtil.red("Shadow")
    ),
    SPECTATOR(
        TextUtil.gray("Spectator")
    );
    
    public final Text name;
    
    SubFaction(Text name) {
        this.name = name;
    }
}
