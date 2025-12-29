package me.ma.skyblock.stats.resources;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import me.ma.skyblock.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public final class ResourceService {
    private final Map<UUID, EnumMap<ResourceType, Resource>> perPlayerResources = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> playerRunnableMap = new HashMap<>();
    
    public Resource get(UUID playerID, ResourceType resourceType) {
        return perPlayerResources.computeIfAbsent(playerID, t -> defaults()).get(resourceType);
    }

    public void set(UUID playerID, ResourceType resource, double value) {
        perPlayerResources.computeIfAbsent(playerID, t -> defaults()).get(resource).setValue(value);
    }

    private EnumMap<ResourceType, Resource> defaults() {
        return new EnumMap<ResourceType, Resource>(Map.of(
            ResourceType.MANA, new Resource(100.0, 100.0),
            ResourceType.HEALTH, new Resource(100.0, 100.0)
        ));
    }

    //TODO: add replenish/regenerate runnables + calculator + tie max to intelligence
    //TODO: item that can use up mana
    
    public void addPlayer(UUID playerID) {
        perPlayerResources.put(playerID, defaults());

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            for (var key : perPlayerResources.get(playerID).keySet()) {
                if (key == ResourceType.HEALTH) {
                    if (Bukkit.getPlayer(playerID) != null) {
                        set(playerID, key, Bukkit.getPlayer(playerID).getHealth());
                    }
                    continue;
                }

                double increase = get(playerID, key).getMaxValue() * (0.02);
                set(playerID, key, Math.min(get(playerID, key).getMaxValue(), get(playerID, key).getValue() + increase));
            }

            var player = Bukkit.getPlayer(playerID);
            if (player != null) {
                var inf = get(playerID, ResourceType.MANA);
                var health = get(playerID, ResourceType.HEALTH);

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(
                    "HEALTH: " + (int) health.getValue() + " / " + (int) health.getMaxValue()
                    + " " +
                    "MANA: " + (int) inf.getValue() + " / " + (int) inf.getMaxValue()
                ));
            }
        }, 20, 20);

        playerRunnableMap.put(playerID, task);
    }

    public void removePlayer(UUID playerID) {
        perPlayerResources.remove(playerID);

        if (playerRunnableMap.containsKey(playerID)) {
            playerRunnableMap.get(playerID).cancel();
            playerRunnableMap.remove(playerID);
        }
    }

    public void reset(UUID playerID) {
        perPlayerResources.put(playerID, defaults());
    }

    public void reset(UUID playerID, ResourceType resourceType) {
        perPlayerResources.get(playerID).put(resourceType, defaults().get(resourceType));
    }
}
