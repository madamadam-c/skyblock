package me.ma.skyblock.stats;

import java.util.Set;

import org.bukkit.entity.LivingEntity;

public record DamageResult(
    LivingEntity attacker,
    LivingEntity target,
    double baseDamage,
    DamageType damageType,
    Set<String> tags,
    double initialDamage,
    double finalDamage,
    boolean critical,
    double totalMultiplier
) {
    public DamageResult {
        tags = (tags == null) ? Set.of() : Set.copyOf(tags);
    }
}
