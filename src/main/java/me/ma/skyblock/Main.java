package me.ma.skyblock;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.ma.skyblock.abilities.ItemAbilityCommand;
import me.ma.skyblock.abilities.UseAbilityListener;
import me.ma.skyblock.rng.ThreadLocalRNG;
import me.ma.skyblock.stats.AlterStatsListener;
import me.ma.skyblock.stats.CalculateDamageListener;
import me.ma.skyblock.stats.DamageService;
import me.ma.skyblock.stats.ItemStatCommand;
import me.ma.skyblock.stats.StatCommand;
import me.ma.skyblock.stats.StatsService;
import me.ma.skyblock.stats.resources.ResourceCommand;
import me.ma.skyblock.stats.resources.ResourceService;

public class Main extends JavaPlugin {
    @Getter private static Main plugin;
    @Getter private DamageService damageService;
    @Getter private StatsService statsService;
    @Getter private ResourceService resourceService;

    @Getter private final NamespacedKey abilitiesKey = new NamespacedKey(this, "abilities");
    @Getter private final NamespacedKey itemStatsKey = new NamespacedKey(this, "item_stats");
    
    @Override
    public void onEnable() {
        plugin = this;

        statsService = new StatsService();
        resourceService = new ResourceService();
        damageService = new DamageService(new ThreadLocalRNG(), statsService);

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {

    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new UseAbilityListener(resourceService), this);
        this.getServer().getPluginManager().registerEvents(new CalculateDamageListener(damageService), this);
        this.getServer().getPluginManager().registerEvents(new AlterStatsListener(statsService, resourceService), this);
    }

    private void registerCommands() {
        this.getCommand("stat").setExecutor(new StatCommand(statsService));
        this.getCommand("resource").setExecutor(new ResourceCommand(resourceService));
        this.getCommand("ability").setExecutor(new ItemAbilityCommand());
        this.getCommand("itemstat").setExecutor(new ItemStatCommand());
    }
}