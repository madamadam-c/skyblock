package me.ma.skyblock.showdamage;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.ma.skyblock.Main;

public class ShowDamageListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMeleeAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Player p)) return;
        if (!(event.getEntity() instanceof LivingEntity le)) return;

        UUID key = p.getUniqueId();
        if (Main.getPlugin().getPlayerDamageText().containsKey(key)) {
            if (Main.getPlugin().getPlayerDamageText().get(key).isValid()) {
                Main.getPlugin().getPlayerDamageText().get(key).remove();
            }

            Main.getPlugin().getPlayerDamageText().remove(key);
        }

        Location spawnLoc = le.getEyeLocation().
            add(
            p.getLocation().toVector().
            subtract(le.getEyeLocation().toVector()).
            normalize().
            multiply(1.25)
        );

        TextDisplay display = ((TextDisplay) p.getWorld().spawnEntity(spawnLoc, EntityType.TEXT_DISPLAY));
        display.setText(Double.toString(event.getDamage()));
        display.setBillboard(Billboard.CENTER);
        
        Main.getPlugin().getPlayerDamageText().put(key, display);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), task -> {
            if (display.isValid()) display.remove();
            Main.getPlugin().getPlayerDamageText().remove(key, display);
        }, 60);
    }
}
