package com.maximumg9.shadow.util;

import net.minecraft.text.Text;

public enum WinState {
    DRAGON_KILLED(TextUtil.green("Dragon Killed"),TextUtil.green("Villagers win")),
    SHADOWS_KILLED(TextUtil.green("Shadows Killed"),TextUtil.green("Villagers win")),
    VILLAGERS_KILLED(TextUtil.red("Villagers Killed"),TextUtil.red("Shadows win")),
    TIE(TextUtil.green("Tie"),TextUtil.green("Everyone loses"));

    public final Text winCause;
    public final Text mainWinners;
    WinState(Text winCause, Text mainWinners) {
        this.winCause = winCause;
        this.mainWinners = mainWinners;
    }
}
