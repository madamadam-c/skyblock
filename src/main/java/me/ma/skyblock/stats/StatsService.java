package me.ma.skyblock.stats;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StatsService {
    private final Map<UUID, EnumMap<StatType, Stat>> perPlayerStats = new ConcurrentHashMap<>();
    
    public Stat get(UUID playerID, StatType statType) {
        return perPlayerStats.computeIfAbsent(playerID, t -> defaults()).get(statType);
    }

    public void set(UUID playerID, StatType stat, double value) {
        perPlayerStats.computeIfAbsent(playerID, t -> defaults()).get(stat).setValue(value);
    }

    private EnumMap<StatType, Stat> defaults() {
        return new EnumMap<StatType, Stat>(Map.of(
            StatType.STRENGTH, new Stat(100.0),
            StatType.CRIT_CHANCE, new Stat(30.0),
            StatType.CRIT_DAMAGE, new Stat(50.0),
            StatType.INTELLIGENCE, new Stat(0.0),
            StatType.SPEED, new Stat(100.0)
        ));
    }

    public void addPlayer(UUID playerID) {
        perPlayerStats.put(playerID, defaults());
    }

    public void removePlayer(UUID playerID) {
        perPlayerStats.remove(playerID);
    }

    public void reset(UUID playerID) {
        perPlayerStats.put(playerID, defaults());
    }

    public void reset(UUID playerID, StatType statType) {
        perPlayerStats.get(playerID).put(statType, defaults().get(statType));
    }
}
