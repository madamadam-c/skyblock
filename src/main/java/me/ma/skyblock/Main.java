package me.ma.skyblock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.ma.skyblock.abilities.ItemAbilityCommand;
import me.ma.skyblock.abilities.UseAbilityListener;
import me.ma.skyblock.rng.ThreadLocalRNG;
import me.ma.skyblock.showdamage.ShowDamageListener;
import me.ma.skyblock.stats.AlterStatsListener;
import me.ma.skyblock.stats.CalculateDamageListener;
import me.ma.skyblock.stats.DamageService;
import me.ma.skyblock.stats.StatCommand;
import me.ma.skyblock.stats.StatsService;
import me.ma.skyblock.stats.resources.ResourceCommand;
import me.ma.skyblock.stats.resources.ResourceService;
import me.ma.skyblock.tick.TickEngine;

public class Main extends JavaPlugin {
    @Getter private static Main plugin;
    @Getter private DamageService damageService;
    @Getter private StatsService statsService;
    private ResourceService resourceService;
    private TickEngine tickEngine;

    @Getter private final NamespacedKey abilitiesKey = new NamespacedKey(this, "abilities");
    @Getter private Map<UUID, TextDisplay> playerDamageText;
    
    @Override
    public void onEnable() {
        plugin = this;

        playerDamageText = new HashMap<>();
        statsService = new StatsService();
        tickEngine = new TickEngine(this);
        tickEngine.start();
        damageService = new DamageService(statsService, new ThreadLocalRNG());
        resourceService = new ResourceService(tickEngine);

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        if (tickEngine != null) {
            tickEngine.stop();
        }
    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new UseAbilityListener(statsService, resourceService), this);
        this.getServer().getPluginManager().registerEvents(new ShowDamageListener(), this);
        this.getServer().getPluginManager().registerEvents(new CalculateDamageListener(damageService), this);
        this.getServer().getPluginManager().registerEvents(new AlterStatsListener(statsService, resourceService), this);
    }

    private void registerCommands() {
        this.getCommand("stat").setExecutor(new StatCommand(statsService));
        this.getCommand("resource").setExecutor(new ResourceCommand(resourceService));
        this.getCommand("ability").setExecutor(new ItemAbilityCommand());
    }
}
