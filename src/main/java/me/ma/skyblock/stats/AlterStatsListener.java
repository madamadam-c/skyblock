package me.ma.skyblock.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
    }

    @EventHandler(priority = EventPriority.HIGH) 
    public void onLeave(PlayerQuitEvent event) {
        statsService.removePlayer(event.getPlayer().getUniqueId());
        resourceService.removePlayer(event.getPlayer().getUniqueId());
    }
}
