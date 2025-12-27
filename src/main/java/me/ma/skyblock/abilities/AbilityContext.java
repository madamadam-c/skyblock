package me.ma.skyblock.abilities;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import lombok.Getter;
import me.ma.skyblock.stats.StatsService;
import me.ma.skyblock.stats.resources.ResourceService;

public class AbilityContext {
    @Getter private final Player caster;
    @Getter private final Location origin;
    @Getter private final StatsService statsService;
    @Getter private final ResourceService resourceService;
    @Getter private final List<LivingEntity> targets;

    public AbilityContext(Player caster, Location origin, StatsService statsService, ResourceService resourceService) {
        this(caster, origin, statsService, resourceService, null);
    }

    public AbilityContext(
        Player caster,
        Location origin,
        StatsService statsService,
        ResourceService resourceService,
        List<LivingEntity> targets
    ) {
        this.caster = caster;
        this.origin = origin;
        this.statsService = statsService;
        this.resourceService = resourceService;
        this.targets = targets;
    }
}
