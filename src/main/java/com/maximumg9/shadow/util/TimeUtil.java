package com.maximumg9.shadow.util;

import net.minecraft.util.math.MathHelper;

import static com.maximumg9.shadow.util.MiscUtil.padLeft;

public abstract class TimeUtil {
    public static String ticksToText(long ticks, boolean leadingZeros) {
        return secondToText(MathHelper.ceil(ticks / 20.0), leadingZeros);
    }
    
    public static String secondToText(int seconds, boolean leadingZeros) {
        if (leadingZeros)
            return
                padLeft(String.valueOf(seconds / 60), '0', 2) +
                    ":" +
                    padLeft(String.valueOf(seconds % 60), '0', 2);
        
        if (seconds < 60) return String.valueOf(seconds);
        return
            (seconds / 60) +
                ":" +
                padLeft(String.valueOf(seconds % 60), '0', 2);
    }
}
