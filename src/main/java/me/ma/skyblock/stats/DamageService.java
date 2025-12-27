package me.ma.skyblock.stats;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.ma.skyblock.rng.RNG;

public final class DamageService {
    private final StatsService statsService;
    private final RNG rng;

    public DamageService(StatsService statsService, RNG rng) {
        this.statsService = statsService;
        this.rng = rng;
    }

    public DamageResult calculate(DamageRequest request, double... multipliers) {
        double strength = getStat(request.attacker(), StatType.STRENGTH);
        double critChance = getStat(request.attacker(), StatType.CRIT_CHANCE);
        double critDamage = getStat(request.attacker(), StatType.CRIT_DAMAGE);

        double totalMultiplier = 1.0;
        if (multipliers != null) {
            for (double multiplier : multipliers) {
                totalMultiplier *= multiplier;
            }
        }

        double initial = (5.0 + request.baseDamage()) * (1.0 + strength / 100.0) * totalMultiplier;
        boolean crit = rollPercent(clamp(critChance, 0.0, 100.0));
        double finalDamage = crit ? initial * (1.0 + critDamage / 100.0) : initial;

        return new DamageResult(
            request.attacker(),
            request.target(),
            request.baseDamage(),
            request.damageType(),
            request.tags(),
            initial,
            finalDamage,
            crit,
            totalMultiplier
        );
    }

    public double calculateAbilityDamage(Player attacker,
                                         double baseAbilityDamage,
                                         double abilityScaling,
                                         double additiveMultiplier,
                                         double multiplicativeMultiplier,
                                         double bonusModifiers) {
        double intelligence = getStat(attacker, StatType.INTELLIGENCE);
        double scaled = baseAbilityDamage * (1.0 + (intelligence / 100.0) * abilityScaling);
        double combined = scaled * additiveMultiplier * multiplicativeMultiplier;
        return combined + bonusModifiers;
    }

    private double getStat(LivingEntity attacker, StatType statType) {
        if (attacker instanceof Player player) {
            return statsService.get(player.getUniqueId(), statType).getValue();
        }
        return 0.0;
    }

    private boolean rollPercent(double percent) {
        return (rng.nextDouble() * 100.0) < percent;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
