package me.ma.skyblock.abilities;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.jeff_media.morepersistentdatatypes.DataType;

import me.ma.skyblock.Main;

public final class ItemAbilityCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("help", "list", "add", "remove", "clear");
    private static final List<String> ACTIVATIONS = Arrays.stream(ActivationType.values())
            .map(a -> a.getId().toLowerCase(Locale.ROOT))
            .sorted()
            .collect(Collectors.toList());

    private static final List<String> ABILITIES = Arrays.stream(Ability.values())
            .filter(a -> a != Ability.DEFAULT)
            .map(a -> a.getId().toLowerCase(Locale.ROOT))
            .sorted()
            .collect(Collectors.toList());

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

        HashMap<String, String> map = getAbilityMap(meta);

        switch (sub) {
            case "list" -> {
                if (map.isEmpty()) {
                    p.sendMessage("This item has no abilities.");
                    return true;
                }
                p.sendMessage("Abilities on held item:");
                map.forEach((activationId, abilityId) ->
                    p.sendMessage("  " + activationId + " -> " + abilityId)
                );
                return true;
            }

            case "add" -> {
                if (args.length < 3) {
                    p.sendMessage("Usage: /" + label + " add <activation> <ability>");
                    return true;
                }

                ActivationType at = ActivationType.parse(args[1]);
                if (at == null) {
                    p.sendMessage("Unknown activation type: " + args[1]);
                    p.sendMessage("Valid: " + String.join(", ", ACTIVATIONS));
                    return true;
                }

                Ability ab = Ability.parse(args[2]);
                if (ab == null || ab == Ability.DEFAULT) {
                    p.sendMessage("Unknown ability: " + args[2]);
                    p.sendMessage("Valid: " + String.join(", ", ABILITIES));
                    return true;
                }

                map.put(at.getId(), ab.getId());
                setAbilityMap(meta, map);
                item.setItemMeta(meta);

                p.sendMessage("Added ability: " + at.getId() + " -> " + ab.getId());
                return true;
            }

            case "remove" -> {
                if (args.length < 2) {
                    p.sendMessage("Usage: /" + label + " remove <activation>");
                    return true;
                }

                ActivationType at = ActivationType.parse(args[1]);
                if (at == null) {
                    p.sendMessage("Unknown activation type: " + args[1]);
                    p.sendMessage("Valid: " + String.join(", ", ACTIVATIONS));
                    return true;
                }

                String removed = map.remove(at.getId());
                if (removed == null) {
                    p.sendMessage("No ability bound to " + at.getId() + " on this item.");
                    return true;
                }

                setAbilityMap(meta, map);
                item.setItemMeta(meta);

                p.sendMessage("Removed ability on " + at.getId() + ".");
                return true;
            }

            case "clear" -> {
                map.clear();
                setAbilityMap(meta, map);
                item.setItemMeta(meta);
                p.sendMessage("Cleared all abilities from this item.");
                return true;
            }

            default -> {
                p.sendMessage("Unknown subcommand. Use /" + label + " help");
                return true;
            }
        }
    }

    private static void sendHelp(Player p, String label) {
        p.sendMessage("Ability commands:");
        p.sendMessage("  /" + label + " list");
        p.sendMessage("  /" + label + " add <activation> <ability>");
        p.sendMessage("  /" + label + " remove <activation>");
        p.sendMessage("  /" + label + " clear");
        p.sendMessage("Activations: " + String.join(", ", ACTIVATIONS));
        p.sendMessage("Abilities: " + String.join(", ", ABILITIES));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return filterPrefix(SUBS, args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (args.length == 2 && (sub.equals("add") || sub.equals("remove"))) {
            return filterPrefix(ACTIVATIONS, args[1]);
        }

        if (args.length == 3 && sub.equals("add")) {
            return filterPrefix(ABILITIES, args[2]);
        }

        return Collections.emptyList();
    }

    private static List<String> filterPrefix(List<String> options, String prefixRaw) {
        String prefix = prefixRaw == null ? "" : prefixRaw.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(prefix))
                .collect(Collectors.toList());
    }

    private static HashMap<String, String> getAbilityMap(ItemMeta meta) {
        return (HashMap<String, String>) meta.getPersistentDataContainer()
                .getOrDefault(
                        Main.getPlugin().getAbilitiesKey(),
                        DataType.asMap(DataType.STRING, DataType.STRING),
                        new HashMap<>()
                );
    }

    private static void setAbilityMap(ItemMeta meta, HashMap<String, String> map) {
        meta.getPersistentDataContainer()
                .set(Main.getPlugin().getAbilitiesKey(),
                        DataType.asMap(DataType.STRING, DataType.STRING),
                        map
                );
    }
}
