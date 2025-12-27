package me.ma.skyblock.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;

import lombok.Getter;
import me.ma.skyblock.Main;
import me.ma.skyblock.utils.LocationUtils;

public enum Ability {
    DEFAULT("DEFAULT", 0.0, (Player p) -> {}),
    INSTANT_TRANSMISSION("INSTANT_TRANSMISSION", 50.0, (Player p) -> {
        p.teleport(p.getLocation().add(new Vector(0, 1, 0)));
    }),
    BLINK("BLINK", 25.0, (Player p) -> {
        var w = p.getWorld();
        var dir = p.getLocation().getDirection().clone();
        dir.setY(0);
        if (dir.lengthSquared() < 1e-6) return;
        dir.normalize();
    
        var start = p.getEyeLocation();
        var hit = w.rayTraceBlocks(start, dir, 8.0);
        Location dest = null;
    
        if (hit != null && hit.getHitBlock() != null && hit.getHitBlockFace() != null) {
            var safeBlock = hit.getHitBlock().getRelative(hit.getHitBlockFace());
    
            int y = p.getLocation().getBlockY();
            dest = new Location(w,
                    safeBlock.getX() + 0.5,
                    y,
                    safeBlock.getZ() + 0.5,
                    p.getLocation().getYaw(),
                    p.getLocation().getPitch()
            );
    
            if (!LocationUtils.isSafeForPlayer(dest)) {
                for (int dy : new int[]{1, -1, 2, -2}) {
                    Location test = dest.clone().add(0, dy, 0);
                    if (LocationUtils.isSafeForPlayer(test)) { dest = test; break; }
                }
            }
        } else {
            dest = p.getLocation().clone().add(dir.multiply(8.0));
            dest.setYaw(p.getLocation().getYaw());
            dest.setPitch(p.getLocation().getPitch());
        }
    
        if (dest == null) return;
        if (!LocationUtils.isSafeForPlayer(dest)) return;
        p.teleport(dest);
    }),
    ICE_PRISON("ICE_PRISON", 45.0, (Player p) -> {
        var hit = p.getWorld().rayTraceBlocks(p.getEyeLocation(), p.getEyeLocation().getDirection(), 10.0);
        if (hit == null || hit.getHitBlock() == null) return;
    
        var base = hit.getHitBlock().getLocation().add(0, 1, 0);
    
        List<Block> placed = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) == 1 || Math.abs(dz) == 1) {
                    var b = base.clone().add(dx, 0, dz).getBlock();
                    if (b.getType().isAir()) {
                        b.setType(Material.ICE);
                        placed.add(b);
                    }
                }
            }
        }
    
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (var b : placed) {
                if (b.getType() == Material.ICE) b.setType(Material.AIR);
            }
        }, 60L);
    });

    private final double manaCost;
    private final Consumer<Player> ability;
    @Getter private final String id;

    Ability(String id, double baseCost, Consumer<Player> ability) {
        this.manaCost = baseCost;
        this.ability = ability;
        this.id = id;
    }

    public void run(Player player) {
        this.ability.accept(player);
    }

    public double getAbilityCost() {
        return this.manaCost;
    }

    public static Ability parse(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase(Locale.ROOT);
        // allow common aliases
        return switch (s) {
            case "INSTANT_TRANSMISSION" -> INSTANT_TRANSMISSION;
            case "BLINK" -> BLINK;
            case "ICE_PRISON" -> ICE_PRISON;
            default -> DEFAULT;
        };
    }
}
