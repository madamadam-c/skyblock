package me.ma.skyblock.stats;

import java.util.Locale;

public enum StatType {
    STRENGTH, CRIT_CHANCE, CRIT_DAMAGE, INTELLIGENCE, SPEED;

    public static StatType parse(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase(Locale.ROOT);
        // allow common aliases
        return switch (s) {
            case "STR", "STRENGTH" -> STRENGTH;
            case "CRIT", "CRITCHANCE", "CRIT_CHANCE", "CC" -> CRIT_CHANCE;
            case "CRITDMG", "CRIT_DAMAGE", "CRITDAMAGE", "CD" -> CRIT_DAMAGE;
            case "INT", "INTELLIGENCE" -> INTELLIGENCE;
            case "SPEED", "SPD" -> SPEED;
            default -> null;
        };
    }
}
