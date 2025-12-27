package me.ma.skyblock.abilities;

import java.util.Locale;

import lombok.Getter;

public enum ActivationType {
    RIGHT_CLICK("RIGHT_CLICK"),
    LEFT_CLICK("LEFT_CLICK");

    @Getter private final String id;
    ActivationType(String id) {
        this.id = id;
    }

    public static ActivationType parse(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase(Locale.ROOT);
        // allow common aliases
        return switch (s) {
            case "RIGHT_CLICK" -> RIGHT_CLICK;
            case "LEFT_CLICK" -> LEFT_CLICK;
            default -> null;
        };
    }
}
