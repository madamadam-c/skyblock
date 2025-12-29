package me.ma.skyblock.stats;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import com.jeff_media.morepersistentdatatypes.DataType;

import me.ma.skyblock.Main;
import me.ma.skyblock.stats.resources.ResourceType;

public final class StatsService {
    private final Map<UUID, EnumMap<StatType, Stat>> perPlayerStats = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerEquipmentModifiers> perPlayerEquipmentModifiers = new ConcurrentHashMap<>();
    
    public Stat get(UUID playerID, StatType statType) {
        return perPlayerStats.computeIfAbsent(playerID, t -> StatType.getDefaultMap()).get(statType);
    }

    public void set(UUID playerID, StatType stat, double value) {
        perPlayerStats.computeIfAbsent(playerID, t -> StatType.getDefaultMap()).get(stat).setValue(value);

        if (Bukkit.getPlayer(playerID) != null) {
            var player = Bukkit.getPlayer(playerID);

            switch (stat) {
                case HEALTH -> {
                    double health = player.getHealth();
                    double curMaxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                    player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(value);
                    player.setHealthScaled(true);
                    player.setHealthScale(20*2);

                    if (health >= value || player.getHealth() == curMaxHealth) {
                        player.setHealth(value);
                    }

                    Main.getPlugin().getResourceService().get(playerID, ResourceType.HEALTH).setMaxValue(value);
                    Main.getPlugin().getResourceService().get(playerID, ResourceType.HEALTH).setValue(player.getHealth());
                }
                case SPEED -> {
                    player.setWalkSpeed(0.2f * Math.min(400.0f, (float)value) / 100.0f);
                    player.setFlySpeed(0.1f * Math.min(400.0f, (float)value) / 100.0f);
                }
                case INTELLIGENCE -> {
                    var mana = Main.getPlugin().getResourceService().get(playerID, ResourceType.MANA);
                    double val = mana.getValue(), maxval = mana.getMaxValue();

                    Main.getPlugin().getResourceService().get(playerID, ResourceType.MANA).setMaxValue(value + 100.0);
                    if (val >= (value + 100.0) || val == maxval) {
                        Main.getPlugin().getResourceService().set(playerID, ResourceType.MANA, value + 100.0);
                    }
                }
                default -> {}
            };
        }
    }

    public void updatePlayerEquipmentModifiers(Player player) {
        var inv = player.getInventory();
        var key1 = Main.getPlugin().getItemStatsKey();

        Map<String, Double> h = new HashMap<>(), c = new HashMap<>(), l = new HashMap<>(), b = new HashMap<>(), m = new HashMap<>();
        if (inv.getHelmet() != null && inv.getHelmet().hasItemMeta()) {
            var r = inv.getHelmet().getItemMeta().getPersistentDataContainer().getOrDefault(
                key1, DataType.asMap(DataType.STRING, DataType.DOUBLE), new HashMap<>());
            h = r;
        }

        if (inv.getChestplate() != null && inv.getChestplate().hasItemMeta()) {
            var r = inv.getChestplate().getItemMeta().getPersistentDataContainer().getOrDefault(
                key1, DataType.asMap(DataType.STRING, DataType.DOUBLE), new HashMap<>());
            c = r;
        }

        if (inv.getLeggings() != null && inv.getLeggings().hasItemMeta()) {
            var r = inv.getLeggings().getItemMeta().getPersistentDataContainer().getOrDefault(
                key1, DataType.asMap(DataType.STRING, DataType.DOUBLE), new HashMap<>());
            l = r;
        }

        if (inv.getBoots() != null && inv.getBoots().hasItemMeta()) {
            var r = inv.getBoots().getItemMeta().getPersistentDataContainer().getOrDefault(
                key1, DataType.asMap(DataType.STRING, DataType.DOUBLE), new HashMap<>());
            b = r;
        }

        if (inv.getItemInMainHand() != null && inv.getItemInMainHand().hasItemMeta()) {
            var r = inv.getItemInMainHand().getItemMeta().getPersistentDataContainer().getOrDefault(
                key1, DataType.asMap(DataType.STRING, DataType.DOUBLE), new HashMap<>());
            m = r;
        }
        
        Map<String, Double> changes = new HashMap<>();
        var modifiers = perPlayerEquipmentModifiers.getOrDefault(player.getUniqueId(), null);

        for (var map : List.of(modifiers.helmet(), modifiers.chestplate(), modifiers.leggings(), modifiers.boots(), modifiers.mainHand())) {
            for (var x : map.entrySet()) {
                double nv = -x.getValue();
                if (changes.containsKey(x.getKey())) {
                    nv += changes.get(x.getKey());
                }

                changes.put(x.getKey(), nv);
            }
        }

        for (var map : List.of(h, c, l, b, m)) {
            for (var x : map.entrySet()) {
                double nv = x.getValue();
                if (changes.containsKey(x.getKey())) {
                    nv += changes.get(x.getKey());
                }

                changes.put(x.getKey(), nv);
            }
        }

        perPlayerEquipmentModifiers.put(player.getUniqueId(), new PlayerEquipmentModifiers(m, h, c, l, b));
        for (var x : changes.entrySet()) {
            StatType type = StatType.parse(x.getKey());
            double newValue = get(player.getUniqueId(), type).getValue() + x.getValue();
            set(player.getUniqueId(), type, newValue);
        }
    }

    public void addPlayer(UUID playerID) {
        perPlayerStats.put(playerID, StatType.getDefaultMap());
        perPlayerEquipmentModifiers.put(playerID, new PlayerEquipmentModifiers(
            new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>()
        ));

        set(playerID, StatType.HEALTH, StatType.HEALTH.getDefaultValue());
        set(playerID, StatType.INTELLIGENCE, StatType.INTELLIGENCE.getDefaultValue());
        set(playerID, StatType.SPEED, StatType.SPEED.getDefaultValue());
    }

    public void removePlayer(UUID playerID) {
        perPlayerStats.remove(playerID);
        perPlayerEquipmentModifiers.remove(playerID);
    }

    public void reset(UUID playerID) {
        perPlayerStats.put(playerID, StatType.getDefaultMap());
    }

    public void reset(UUID playerID, StatType statType) {
        perPlayerStats.get(playerID).put(statType, new Stat(statType.getDefaultValue()));
    }

    public record PlayerEquipmentModifiers(
        Map<String, Double> mainHand, Map<String, Double> helmet, Map<String, Double> chestplate,
        Map<String, Double> leggings, Map<String, Double> boots
    ) {}
}