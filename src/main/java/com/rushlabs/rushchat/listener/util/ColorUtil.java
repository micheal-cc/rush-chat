package com.rushlabs.rushchat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    public static final MiniMessage MINI_MESSAGE_PARSER = MiniMessage.miniMessage();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // Matches a sequence of Legacy codes (&a, &#123456) or Opening MiniMessage tags (<red>, <gradient:...>) at the end of the string.
    // Excludes closing tags </...> to prevent breaking tag structure.
    private static final Pattern TAIL_FORMAT_PATTERN = Pattern.compile("((?:<(?!/)[^>]+>)|(?:&[0-9a-fA-FK-ORklmnor])|(?:&#[0-9a-fA-F]{6}))+$");

    public static Component parse(String text) {
        if (text == null) return Component.empty();

        String converted = convertLegacyToMiniMessage(text);

        return MINI_MESSAGE_PARSER.deserialize(converted);
    }

    /**
     * Extracts the last active color/format codes OR MiniMessage tags from the end of a string.
     * This allows styles like "<b><gradient:...>" to bleed over into the next component.
     */
    public static String getLastColors(String text) {
        Matcher matcher = TAIL_FORMAT_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private static String convertLegacyToMiniMessage(String text) {
        // Convert &#RRGGBB to <#RRGGBB>
        text = HEX_PATTERN.matcher(text).replaceAll("<#$1>");

        StringBuilder sb = new StringBuilder();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < len) {
                char code = text.charAt(i + 1);
                String tag = getTagFromCode(code);
                if (tag != null) {
                    sb.append(tag);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static String getTagFromCode(char code) {
        switch (Character.toLowerCase(code)) {
            case '0': return "<black>";
            case '1': return "<dark_blue>";
            case '2': return "<dark_green>";
            case '3': return "<dark_aqua>";
            case '4': return "<dark_red>";
            case '5': return "<dark_purple>";
            case '6': return "<gold>";
            case '7': return "<gray>";
            case '8': return "<dark_gray>";
            case '9': return "<blue>";
            case 'a': return "<green>";
            case 'b': return "<aqua>";
            case 'c': return "<red>";
            case 'd': return "<light_purple>";
            case 'e': return "<yellow>";
            case 'f': return "<white>";
            case 'k': return "<obfuscated>";
            case 'l': return "<bold>";
            case 'm': return "<strikethrough>";
            case 'n': return "<underlined>";
            case 'o': return "<italic>";
            case 'r': return "<reset>";
            default: return null;
        }
    }
}