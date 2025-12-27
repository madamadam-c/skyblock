package me.ma.skyblock.stats;

import me.ma.skyblock.rng.RNG;

public final class DamageCalculator {
    private final RNG rng;
    public DamageCalculator(RNG rng) {
        this.rng = rng;
    }
    
    public DamageResult calculate(double weaponDamage, double strength, double critChance, double critDamage) {
        double initial = (5.0 + weaponDamage) * (1.0 + strength / 100.0);

        boolean crit = rollPercent(clamp(critChance, 0, 100.0));
        double finalDamage = crit ? initial * (1.0 + critDamage / 100.0) : initial;

        return new DamageResult(initial, finalDamage, crit);
    }

    private boolean rollPercent(double percent) {
        return (rng.nextDouble() * 100.0) < percent;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public record DamageResult(double initialDamage, double finalDamage, boolean critical) {}
}
