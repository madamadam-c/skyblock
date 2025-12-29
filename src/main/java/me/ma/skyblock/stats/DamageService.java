package me.ma.skyblock.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import me.ma.skyblock.Main;
import me.ma.skyblock.rng.RNG;

public final class DamageService {
    private final RNG rng;
    private final StatsService statsService;
    private Map<PlayerEntityKey, TextDisplay> playerDamageText = new HashMap<>();

    public DamageService(RNG rng, StatsService statsService) {
        this.rng = rng;
        this.statsService = statsService;
    }

    public DamageResult damage(DamageRequest request) {
        DamageResult result;

        if (request.isAbilityDamage()) {
            double intelligence = statsService.get(request.player().getUniqueId(), StatType.INTELLIGENCE).getValue();
            double playerAbilityDamage = statsService.get(request.player().getUniqueId(), StatType.ABILITY_DAMAGE).getValue();
            result = calculateAbility(request.baseAbilityDamage(), intelligence, playerAbilityDamage);
        } else {
            double weaponDamage = statsService.get(request.player().getUniqueId(), StatType.DAMAGE).getValue();
            double strength = statsService.get(request.player().getUniqueId(), StatType.STRENGTH).getValue();
            double critChance = statsService.get(request.player().getUniqueId(), StatType.CRIT_CHANCE).getValue();
            double critDamage = statsService.get(request.player().getUniqueId(), StatType.CRIT_DAMAGE).getValue();
            
            result = calculateMelee(weaponDamage, strength, critChance, critDamage);
        }

        if (request.target() instanceof Player) {
            result = applyPlayerDefenseMultiplier(request, result);
        }

        request.player().addScoreboardTag("damage_service");
        request.target().damage(result.finalDamage(), request.player());
        showPopUp(request.player(), request.target(), result.finalDamage());
        return result;
    }

    private void showPopUp(Player p, LivingEntity le, double damage) {
        PlayerEntityKey key = new PlayerEntityKey(p.getUniqueId(), le);
        if (playerDamageText.containsKey(key)) {
            if (playerDamageText.get(key).isValid()) {
                playerDamageText.get(key).remove();
            }

            playerDamageText.remove(key);
        }

        Location spawnLoc = le.getEyeLocation().
            add(
            p.getLocation().toVector().
            subtract(le.getEyeLocation().toVector()).
            normalize().
            multiply(1.25)
        );

        TextDisplay display = ((TextDisplay) p.getWorld().spawnEntity(spawnLoc, EntityType.TEXT_DISPLAY));
        display.setText(Integer.toString((int) damage));
        display.setBillboard(Billboard.CENTER);
        
        playerDamageText.put(key, display);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), task -> {
            if (display.isValid()) display.remove();
            playerDamageText.remove(key, display);
        }, 60);
    }
    
    public DamageResult calculateMelee(double weaponDamage, double strength, double critChance, double critDamage) {
        double initial = (weaponDamage) * (1.0 + strength / 100.0);

        boolean crit = rollPercent(clamp(critChance, 0, 100.0));
        double finalDamage = crit ? initial * (1.0 + critDamage / 100.0) : initial;

        return new DamageResult(initial, finalDamage, crit);
    }

    public DamageResult calculateAbility(double baseAbilityDamage, double intelligence, double playerAbilityDamage) {
        double initial = baseAbilityDamage;
        double multiplier = 1.0 + (playerAbilityDamage) / (100.0);
        double finalDamage = initial * (1 + (intelligence) / (100.0)) * multiplier;

        return new DamageResult(initial, finalDamage, false);
    }

    public DamageResult applyPlayerDefenseMultiplier(DamageRequest request, DamageResult result) {
        double defense = statsService.get(((Player) request.target()).getUniqueId(), StatType.DEFENSE).getValue();
        double mult = (1.0 - (defense) / (defense + 100.0));
        
        return new DamageResult(result.initialDamage(), result.finalDamage() * mult, false);
    }

    private boolean rollPercent(double percent) {
        return (rng.nextDouble() * 100.0) < percent;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private record PlayerEntityKey(UUID playerID, LivingEntity le) {}
    public record DamageResult(double initialDamage, double finalDamage, boolean critical) {}
    public record DamageRequest(Player player, LivingEntity target, boolean isAbilityDamage, double baseAbilityDamage) {}
}
