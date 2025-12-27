package me.ma.skyblock.stats;

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
        damageService.applyMeleeDamage(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMeleePopup(EntityDamageByEntityEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        damageService.showDamagePopup(player, target, event.getDamage());
    }
}
