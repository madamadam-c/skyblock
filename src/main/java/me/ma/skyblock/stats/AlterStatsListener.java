package me.ma.skyblock.stats;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;

import me.ma.skyblock.Main;
import me.ma.skyblock.stats.resources.ResourceService;

public class AlterStatsListener implements Listener {
    private final StatsService statsService;
    private final ResourceService resourceService;

    public AlterStatsListener(StatsService statsService, ResourceService resourceService) {
        this.statsService = statsService;
        this.resourceService = resourceService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        statsService.addPlayer(event.getPlayer().getUniqueId());
        resourceService.addPlayer(event.getPlayer().getUniqueId());

        updatePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH) 
    public void onLeave(PlayerQuitEvent event) {
        statsService.removePlayer(event.getPlayer().getUniqueId());
        resourceService.removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSwitchHandItem(PlayerItemHeldEvent event) {
        updatePlayer(event.getPlayer());
    }

    @EventHandler
    public void onInventoryChange(InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof PlayerInventory inv)) return;

        updatePlayer((Player) inv.getHolder());
    }

    private void updatePlayer(Player player) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            statsService.updatePlayerEquipmentModifiers(player);
        }, 1L);
    }
}
