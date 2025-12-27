package me.ma.skyblock.stats.resources;

import lombok.Getter;
import lombok.Setter;

public class Resource {
    @Getter @Setter private double value, maxValue;
    
    public Resource(double defaultValue, double maxValue) {
        this.value = defaultValue;
        this.maxValue = maxValue;
    }
}
