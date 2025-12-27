package me.ma.skyblock.utils;

import org.bukkit.Location;

public class LocationUtils {
    public static boolean isSafeForPlayer(Location loc) {
        var w = loc.getWorld();
        if (w == null) return false;
    
        var feet = w.getBlockAt(loc);
        var head = w.getBlockAt(loc.clone().add(0, 1, 0));
    
        return feet.isPassable() && head.isPassable();
    }
}
