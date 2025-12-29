package me.ma.skyblock.stats;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.jeff_media.morepersistentdatatypes.DataType;

import me.ma.skyblock.Main;

public final class ItemStatCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("help", "list", "add", "remove", "clear");
    private static final List<String> STAT_NAMES = List.of(Arrays.stream(StatType.class.getEnumConstants()).map(Enum::name).toArray(String[]::new));

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(p, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            p.sendMessage("Hold an item in your main hand.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            p.sendMessage("That item has no meta.");
            return true;
        }

        HashMap<String, Double> map = getStatMap(meta);

        switch (sub) {
            case "list" -> {
                if (map.isEmpty()) {
                    p.sendMessage("This item has no stats.");
                    return true;
                }
                p.sendMessage("Stats on held item:");
                map.forEach((statId, abilityId) ->
                    p.sendMessage("  " + statId + " -> " + abilityId)
                );
                return true;
            }

            case "add" -> {
                if (args.length < 3) {
                    p.sendMessage("Usage: /" + label + " add <stat> <value>");
                    return true;
                }

                StatType at = StatType.parse(args[1]);
                if (at == null) {
                    p.sendMessage("Unknown stat type: " + args[1]);
                    p.sendMessage("Valid: " + String.join(", ", STAT_NAMES));
                    return true;
                }

                try {
                    double val = Double.parseDouble(args[2]);
                    map.put(at.getId(), val);
                    setStatMap(meta, map);
                    item.setItemMeta(meta);

                    p.sendMessage("Added stat: " + at.getId() + " -> " + val);
                } catch(Exception e) {
                    p.sendMessage("Couldn't parse this number: " + args[2]);
                }
                
                Main.getPlugin().getStatsService().updatePlayerEquipmentModifiers(p);
                return true;
            }

            case "remove" -> {
                if (args.length < 2) {
                    p.sendMessage("Usage: /" + label + " remove <stat>");
                    return true;
                }

                StatType at = StatType.parse(args[1]);
                if (at == null) {
                    p.sendMessage("Unknown stat type: " + args[1]);
                    p.sendMessage("Valid: " + String.join(", ", STAT_NAMES));
                    return true;
                }

                Double removed = map.remove(at.getId());
                if (removed == null) {
                    p.sendMessage("No stat " + at.getId() + " on this item.");
                    return true;
                }

                setStatMap(meta, map);
                item.setItemMeta(meta);

                p.sendMessage("Removed stat on " + at.getId() + ".");
                Main.getPlugin().getStatsService().updatePlayerEquipmentModifiers(p);
                return true;
            }

            case "clear" -> {
                map.clear();
                setStatMap(meta, map);
                item.setItemMeta(meta);
                p.sendMessage("Cleared all abilities from this item.");
                Main.getPlugin().getStatsService().updatePlayerEquipmentModifiers(p);
                return true;
            }

            default -> {
                p.sendMessage("Unknown subcommand. Use /" + label + " help");
                return true;
            }
        }
    }

    private static void sendHelp(Player p, String label) {
        p.sendMessage("Stat commands:");
        p.sendMessage("  /" + label + " list");
        p.sendMessage("  /" + label + " add <stat> <value>");
        p.sendMessage("  /" + label + " remove <stat>");
        p.sendMessage("  /" + label + " clear");
        p.sendMessage("Stats: " + String.join(", ", STAT_NAMES));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return filterPrefix(SUBS, args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (args.length == 2 && (sub.equals("add") || sub.equals("remove"))) {
            return filterPrefix(STAT_NAMES, args[1]);
        }

        // if (args.length == 3 && sub.equals("add")) {
        //     return filterPrefix(ABILITIES, args[2]);
        // }

        return Collections.emptyList();
    }

    private static List<String> filterPrefix(List<String> options, String prefixRaw) {
        String prefix = prefixRaw == null ? "" : prefixRaw.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(prefix))
                .collect(Collectors.toList());
    }

    private static HashMap<String, Double> getStatMap(ItemMeta meta) {
        return (HashMap<String, Double>) meta.getPersistentDataContainer()
                .getOrDefault(
                        Main.getPlugin().getItemStatsKey(),
                        DataType.asMap(DataType.STRING, DataType.DOUBLE),
                        new HashMap<>()
                );
    }

    private static void setStatMap(ItemMeta meta, HashMap<String, Double> map) {
        meta.getPersistentDataContainer()
            .set(Main.getPlugin().getItemStatsKey(),
                    DataType.asMap(DataType.STRING, DataType.DOUBLE),
                    map
            );
    }
}
