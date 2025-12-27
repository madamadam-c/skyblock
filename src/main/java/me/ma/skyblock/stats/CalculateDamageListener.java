package me.ma.skyblock.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class CalculateDamageListener implements Listener {
    private final StatsService statsService;
    private final DamageCalculator damageCalculator;

    public CalculateDamageListener(DamageCalculator damageCalculator, StatsService statsService) {
        this.damageCalculator = damageCalculator;
        this.statsService = statsService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Player p)) return;
        var uuid = p.getUniqueId();

        double weaponDamage = event.getDamage();
        DamageCalculator.DamageResult r = damageCalculator.calculate(
            weaponDamage, 
            statsService.get(uuid, StatType.STRENGTH).getValue(),
            statsService.get(uuid, StatType.CRIT_CHANCE).getValue(),
            statsService.get(uuid, StatType.CRIT_DAMAGE).getValue()
        );

        event.setDamage(r.finalDamage());
    }
}
