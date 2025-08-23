package com.maximumg9.shadow.roles;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum SubFaction {
    VILLAGER_KILLING(
        Text.literal("Villager")
            .styled(style -> style.withColor(Formatting.GREEN)).append(
                Text.literal(" Killing").styled(style -> style.withColor(Formatting.BLUE))
            )
    ),
    VILLAGER_SUPPORT(
        Text.literal("Villager")
            .styled(style -> style.withColor(Formatting.GREEN)).append(
                Text.literal(" Support").styled(style -> style.withColor(Formatting.BLUE))
            )
    ),
    VILLAGER_OUTLIER(
        Text.literal("Villager")
            .styled(style -> style.withColor(Formatting.GREEN)).append(
                Text.literal(" Outlier").styled(style -> style.withColor(Formatting.BLUE))
            )
    ),
    NEUTRAL_CHAOS(
        Text.literal("Neutral")
            .styled(style -> style.withColor(Formatting.GRAY)).append(
                Text.literal(" Chaos").styled(style -> style.withColor(Formatting.BLUE))
            )
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
    
    SubFaction(Text name) {
        this.name = name;
    }
}
