package me.ma.skyblock.stats.resources;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import me.ma.skyblock.Main;
import me.ma.skyblock.stats.StatType;
import me.ma.skyblock.tick.TickEngine;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public final class ResourceService {
    private final Map<UUID, EnumMap<ResourceType, Resource>> perPlayerResources = new ConcurrentHashMap<>();
    private final TickEngine tickEngine;

    public ResourceService(TickEngine tickEngine) {
        this.tickEngine = tickEngine;
        this.tickEngine.runEvery(20, this::tickSecond);
    }
    
    public Resource get(UUID playerID, ResourceType resourceType) {
        return perPlayerResources.computeIfAbsent(playerID, t -> defaults()).get(resourceType);
    }

    public void set(UUID playerID, ResourceType resource, double value) {
        perPlayerResources.computeIfAbsent(playerID, t -> defaults()).get(resource).setValue(value);
    }

    private EnumMap<ResourceType, Resource> defaults() {
        return new EnumMap<ResourceType, Resource>(Map.of(
            ResourceType.MANA, new Resource(100.0, 100.0)
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

    private void tickSecond() {
        for (var entry : perPlayerResources.entrySet()) {
            UUID playerID = entry.getKey();
            var resources = entry.getValue();
            var mana = resources.get(ResourceType.MANA);
            mana.setMaxValue(Main.getPlugin().getStatsService().get(playerID, StatType.INTELLIGENCE).getValue() + 100.0);

            for (var key : resources.keySet()) {
                double increase = get(playerID, key).getMaxValue() * (0.02);
                set(playerID, key, Math.min(get(playerID, key).getMaxValue(), get(playerID, key).getValue() + increase));
            }

            var player = Bukkit.getPlayer(playerID);
            if (player != null) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(
                    "MANA: " + mana.getValue() + " / " + mana.getMaxValue()
                ));
            }
        }
    }
}
