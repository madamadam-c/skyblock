package me.ma.skyblock.stats.resources;

import java.util.Locale;

public enum ResourceType {
    MANA;

    public static ResourceType parse(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase(Locale.ROOT);
        // allow common aliases
        return switch (s) {
            case "MANA" -> MANA;
            default -> null;
        };
    }
}
