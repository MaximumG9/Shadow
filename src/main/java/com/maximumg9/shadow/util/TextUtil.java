package com.maximumg9.shadow.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtil {
    public static MutableText gray(String text) {
        return Text.literal(text).styled(style -> style.withColor(Formatting.GRAY).withItalic(false));
    }
    
    public static MutableText error(String message) {
        return Text.literal("✕ " + message).styled(style -> style.withColor(Formatting.RED));
    }
    
    public static MutableText success(String message) {
        return Text.literal("» " + message).styled(style -> style.withColor(Formatting.GREEN));
    }
    
    public static MutableText hearts(float hearts) {
        return Text.literal(String.format("%.1f❤", hearts)).styled(style -> style.withColor(Formatting.RED));
    }
}
