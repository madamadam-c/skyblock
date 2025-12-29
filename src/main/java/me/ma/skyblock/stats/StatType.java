package me.ma.skyblock.stats;

import java.util.EnumMap;
import java.util.Locale;

import lombok.Getter;

public enum StatType {
    HEALTH(100.0, "health"), DEFENSE(0.0, "defense"), STRENGTH(0.0, "strength"), 
    INTELLIGENCE(0.0, "intelligence"), CRIT_CHANCE(30.0, "crit_chance"), CRIT_DAMAGE(50.0, "crit_damage"), 
    ABILITY_DAMAGE(0.0, "ability_damage"), DAMAGE(5.0, "damage"), SPEED(100.0, "speed");

    @Getter private final double defaultValue;
    @Getter private final String id;

    StatType(double defaultValue, String id) {
        this.defaultValue = defaultValue;
        this.id = id;
    }

    public static EnumMap<StatType, Stat> getDefaultMap() {
        EnumMap<StatType, Stat> map = new EnumMap<StatType, Stat>(StatType.class);
        for (var v : StatType.values()) {
            map.put(v, new Stat(v.getDefaultValue()));
        }

        return map;
    }

    public static StatType parse(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase(Locale.ROOT);
        // allow common aliases
        return switch (s) {
            case "HP", "HEALTH" -> HEALTH;
            case "STR", "STRENGTH" -> STRENGTH;
            case "INT", "INTELLIGENCE" -> INTELLIGENCE;
            case "CRIT", "CRITCHANCE", "CRIT_CHANCE", "CC" -> CRIT_CHANCE;
            case "CRITDMG", "CRIT_DAMAGE", "CRITDAMAGE", "CD" -> CRIT_DAMAGE;
            case "ABILITYDMG", "ABILITY_DAMAGE" -> ABILITY_DAMAGE;
            case "DMG", "DAMAGE" -> DAMAGE;
            case "SPD", "SPEED" -> SPEED;
            default -> null;
        };
    }
}
