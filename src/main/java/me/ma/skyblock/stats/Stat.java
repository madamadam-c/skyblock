package me.ma.skyblock.stats;

import lombok.Getter;
import lombok.Setter;

public class Stat {
    @Getter @Setter private double value;

    public Stat(double defaultValue) {
        this.value = defaultValue;
    }
}
