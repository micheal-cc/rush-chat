package com.rushlabs.rushchat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    public static final MiniMessage MINI_MESSAGE_PARSER = MiniMessage.miniMessage();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // Simplified Regex: Removed unnecessary non-capturing groups (?:...)
    private static final Pattern TAIL_FORMAT_PATTERN = Pattern.compile("(<(?!/)[^>]+>|&[0-9a-fA-FK-ORklmnor]|&#[0-9a-fA-F]{6})+$");

    public static Component parse(String text) {
        if (text == null) return Component.empty();

        String converted = convertLegacyToMiniMessage(text);

        return MINI_MESSAGE_PARSER.deserialize(converted);
    }

    public static String getLastColors(String text) {
        Matcher matcher = TAIL_FORMAT_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private static String convertLegacyToMiniMessage(String text) {
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

    // Updated to use Java Enhanced Switch (Switch Expression)
    private static String getTagFromCode(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> null;
        };
    }
}