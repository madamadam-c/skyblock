package me.ma.skyblock.stats;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.ma.skyblock.Main;
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

    public void applyMeleeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamageSource().getDirectEntity() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (player.getScoreboardTags().contains("ability_damage")) return;

        double weaponDamage = event.getDamage();
        DamageRequest request = new DamageRequest(
            player,
            target,
            weaponDamage,
            DamageType.MELEE,
            Set.of("MELEE")
        );

        DamageResult result = calculate(request);
        event.setDamage(result.finalDamage());
    }

    public void showDamagePopup(Player attacker, LivingEntity target, double damage) {
        UUID key = attacker.getUniqueId();
        if (Main.getPlugin().getPlayerDamageText().containsKey(key)) {
            if (Main.getPlugin().getPlayerDamageText().get(key).isValid()) {
                Main.getPlugin().getPlayerDamageText().get(key).remove();
            }

            Main.getPlugin().getPlayerDamageText().remove(key);
        }

        Location spawnLoc = target.getEyeLocation().
            add(
            attacker.getLocation().toVector().
            subtract(target.getEyeLocation().toVector()).
            normalize().
            multiply(1.25)
        );

        TextDisplay display = ((TextDisplay) attacker.getWorld().spawnEntity(spawnLoc, EntityType.TEXT_DISPLAY));
        display.setText(Double.toString(damage));
        display.setBillboard(Billboard.CENTER);

        Main.getPlugin().getPlayerDamageText().put(key, display);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), task -> {
            if (display.isValid()) display.remove();
            Main.getPlugin().getPlayerDamageText().remove(key, display);
        }, 60);
    }

    public double calculateAbilityDamage(Player attacker,
                                         double baseAbilityDamage,
                                         double abilityScaling,
                                         double additiveMultiplier,
                                         double multiplicativeMultiplier,
                                         double bonusModifiers) {
        double intelligence = getStat(attacker, StatType.INTELLIGENCE);
        double abilityDamage = getStat(attacker, StatType.ABILITY_DAMAGE);
        double abilityDamageMultiplier = 1.0 + (abilityDamage / 100.0);
        double scaled = baseAbilityDamage * (1.0 + (intelligence / 100.0) * abilityScaling);
        double combined = scaled * additiveMultiplier * multiplicativeMultiplier;
        return (combined + bonusModifiers) * abilityDamageMultiplier;
    }

    public void applyAbilityDamage(Player caster,
                                   LivingEntity target,
                                   double baseDamage,
                                   double scaling,
                                   double addMult,
                                   double multMult,
                                   double bonus) {
        double damage = calculateAbilityDamage(caster, baseDamage, scaling, addMult, multMult, bonus);
        String tag = "ability_damage";
        boolean hadTag = caster.getScoreboardTags().contains(tag);
        if (!hadTag) {
            caster.addScoreboardTag(tag);
        }
        try {
            target.damage(damage, caster);
        } finally {
            if (!hadTag) {
                caster.removeScoreboardTag(tag);
            }
        }
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
