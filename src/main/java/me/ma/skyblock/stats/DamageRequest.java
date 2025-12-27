package me.ma.skyblock.stats;

import java.util.Set;

import org.bukkit.entity.LivingEntity;

public record DamageRequest(
    LivingEntity attacker,
    LivingEntity target,
    double baseDamage,
    DamageType damageType,
    Set<String> tags
) {
    public DamageRequest {
        tags = (tags == null) ? Set.of() : Set.copyOf(tags);
    }
}
