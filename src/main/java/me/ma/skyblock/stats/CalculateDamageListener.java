package me.ma.skyblock.stats;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import me.ma.skyblock.stats.DamageService.DamageRequest;

public final class CalculateDamageListener implements Listener {
    private final DamageService damageService;

    public CalculateDamageListener(DamageService damageService) {
        this.damageService = damageService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Player p)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM) return;

        if (p.getScoreboardTags().contains("damage_service")) {
            p.removeScoreboardTag("damage_service");
        } else {
            event.setCancelled(true);
            damageService.damage(new DamageRequest(p, (LivingEntity) event.getEntity(), false, 0.0));
        }
    }
}
