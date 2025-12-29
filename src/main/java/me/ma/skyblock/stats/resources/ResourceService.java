package me.ma.skyblock.stats.resources;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import me.ma.skyblock.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public final class ResourceService {
    private final Map<UUID, EnumMap<ResourceType, Resource>> perPlayerResources = new ConcurrentHashMap<>();

    public ResourceService() {
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            for (var player : Bukkit.getOnlinePlayers()) {
                UUID playerID = player.getUniqueId();

                for (var key : perPlayerResources.get(playerID).keySet()) {
                    if (key == ResourceType.HEALTH) {
                        set(playerID, key, player.getHealth());
                        continue;
                    }

                    double increase = get(playerID, key).getMaxValue() * (0.02);
                    set(playerID, key, Math.min(get(playerID, key).getMaxValue(), get(playerID, key).getValue() + increase));
                }

                var inf = get(playerID, ResourceType.MANA);
                var health = get(playerID, ResourceType.HEALTH);

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(
                    "HEALTH: " + (int) health.getValue() + " / " + (int) health.getMaxValue()
                    + " " +
                    "MANA: " + (int) inf.getValue() + " / " + (int) inf.getMaxValue()
                ));
            }
        }, 20, 20);
    }
    
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
    }

    public void removePlayer(UUID playerID) {
        perPlayerResources.remove(playerID);
    }

    public void reset(UUID playerID) {
        perPlayerResources.put(playerID, defaults());
    }

    public void reset(UUID playerID, ResourceType resourceType) {
        perPlayerResources.get(playerID).put(resourceType, defaults().get(resourceType));
    }
}
