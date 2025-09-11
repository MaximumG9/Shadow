package com.maximumg9.shadow.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtil {
    public static MutableText gray(String text) {
        return Text.literal(text).styled(style -> style.withColor(Formatting.GRAY).withItalic(false));
    }

    public static MutableText withColour(String text, Formatting colour) {
        return Text.literal(text).styled(style -> style.withColor(colour));
    }
    
    public static MutableText red(String text) {
        return Text.literal(text).styled(style -> style.withColor(Formatting.RED));
    }

    public static MutableText gold(String text) {
        return Text.literal(text).styled(style -> style.withColor(Formatting.GOLD));
    }
    
    public static MutableText green(String text) {
        return Text.literal(text).styled(style -> style.withColor(Formatting.GREEN));
    }

    public static MutableText blue(String text) {
        return Text.literal(text).styled(style -> style.withColor(Formatting.BLUE));
    }
    
    public static MutableText hearts(float hearts) {
        return TextUtil.red(String.format("%.1f❤", hearts));
    }
}
