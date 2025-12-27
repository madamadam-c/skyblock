package me.ma.skyblock.stats;

import java.util.Locale;

public enum StatType {
    HEALTH,
    DEFENSE,
    TRUE_DEFENSE,
    ABILITY_DAMAGE,
    SPEED,
    CRIT_CHANCE,
    CRIT_DAMAGE,
    STRENGTH,
    INTELLIGENCE,
    DAMAGE;

    public static StatType parse(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase(Locale.ROOT);
        // allow common aliases
        return switch (s) {
            case "HP", "HEALTH" -> HEALTH;
            case "DEF", "DEFENSE" -> DEFENSE;
            case "TRUEDEF", "TRUE_DEF", "TRUEDEFENSE", "TRUE_DEFENSE" -> TRUE_DEFENSE;
            case "ABILITYDMG", "ABILITY_DAMAGE", "ABILITYDAMAGE", "AD" -> ABILITY_DAMAGE;
            case "SPD", "SPEED" -> SPEED;
            case "STR", "STRENGTH" -> STRENGTH;
            case "CRIT", "CRITCHANCE", "CRIT_CHANCE", "CC" -> CRIT_CHANCE;
            case "CRITDMG", "CRIT_DAMAGE", "CRITDAMAGE", "CD" -> CRIT_DAMAGE;
            case "INT", "INTELLIGENCE" -> INTELLIGENCE;
            case "DMG", "DAMAGE" -> DAMAGE;
            default -> null;
        };
    }
}
