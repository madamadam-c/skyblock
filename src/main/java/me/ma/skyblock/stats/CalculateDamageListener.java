package me.ma.skyblock.stats;

import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class CalculateDamageListener implements Listener {
    private final DamageService damageService;

    public CalculateDamageListener(DamageService damageService) {
        this.damageService = damageService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamageSource().getDirectEntity() instanceof Player p)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (p.getScoreboardTags().contains("ability_damage")) return;

        double weaponDamage = event.getDamage();
        DamageRequest request = new DamageRequest(
            p,
            target,
            weaponDamage,
            DamageType.MELEE,
            Set.of("MELEE")
        );

        DamageResult result = damageService.calculate(request);
        event.setDamage(result.finalDamage());
    }
}
