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
import me.ma.skyblock.stats.DamageCalculator;
import me.ma.skyblock.stats.StatCommand;
import me.ma.skyblock.stats.StatsService;
import me.ma.skyblock.stats.resources.ResourceCommand;
import me.ma.skyblock.stats.resources.ResourceService;

public class Main extends JavaPlugin {
    @Getter private static Main plugin;
    private DamageCalculator damageCalculator;
    @Getter private StatsService statsService;
    private ResourceService resourceService;

    @Getter private final NamespacedKey abilitiesKey = new NamespacedKey(this, "abilities");
    @Getter private Map<UUID, TextDisplay> playerDamageText;
    
    @Override
    public void onEnable() {
        plugin = this;

        playerDamageText = new HashMap<>();
        damageCalculator = new DamageCalculator(new ThreadLocalRNG());
        statsService = new StatsService();
        resourceService = new ResourceService();

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {

    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new UseAbilityListener(resourceService), this);
        this.getServer().getPluginManager().registerEvents(new ShowDamageListener(), this);
        this.getServer().getPluginManager().registerEvents(new CalculateDamageListener(damageCalculator, statsService), this);
        this.getServer().getPluginManager().registerEvents(new AlterStatsListener(statsService, resourceService), this);
    }

    private void registerCommands() {
        this.getCommand("stat").setExecutor(new StatCommand(statsService));
        this.getCommand("resource").setExecutor(new ResourceCommand(resourceService));
        this.getCommand("ability").setExecutor(new ItemAbilityCommand());
    }
}