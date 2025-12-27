package me.ma.skyblock.stats.resources;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class ResourceCommand implements CommandExecutor, TabCompleter {
    private final ResourceService resourceService;

    private static final List<String> SUBS = List.of("get", "set", "reset");
    private static final List<String> resource_NAMES = List.of("mana");

    public ResourceCommand(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage:");
            sender.sendMessage("/resource get <player> [strength|crit_chance|crit_damage]");
            sender.sendMessage("/resource set <player> <resource> <value>");
            sender.sendMessage("/resource reset <player> [resource]");
            return true;
        }

        String sub = args[0].toLowerCase();

        return switch (sub) {
            case "get" -> handleGet(sender, args);
            case "set" -> handleSet(sender, args);
            case "reset" -> handleReset(sender, args);
            default -> {
                sender.sendMessage("Unknown subcommand. Use: get/set/reset");
                yield true;
            }
        };
    }

    private boolean handleGet(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /resource get <player> [resource]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Player must be online (for now).");
            return true;
        }

        UUID uuid = target.getUniqueId();
        if (args.length == 2) {
            sender.sendMessage(target.getName() + " resources:");
            sender.sendMessage("  mana=" + resourceService.get(uuid, ResourceType.MANA).getValue() + "/" + resourceService.get(uuid, ResourceType.MANA).getMaxValue());
            return true;
        }

        ResourceType st = ResourceType.parse(args[2]);
        if (st == null) {
            sender.sendMessage("Unknown resource: " + args[2]);
            return true;
        }

        sender.sendMessage(target.getName() + " " + st.name().toLowerCase() + "=" + resourceService.get(uuid, st).getValue());
        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /resource set <player> <resource> <value>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Player must be online (for now).");
            return true;
        }

        ResourceType st = ResourceType.parse(args[2]);
        if (st == null) {
            sender.sendMessage("Unknown resource: " + args[2]);
            return true;
        }

        double value;
        try {
            value = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Value must be a number.");
            return true;
        }

        resourceService.set(target.getUniqueId(), st, value);
        sender.sendMessage("Set " + target.getName() + " " + st.name().toLowerCase() + " = " + value);
        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /resource reset <player> [resource]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Player must be online (for now).");
            return true;
        }

        if (args.length == 2) {
            resourceService.reset(target.getUniqueId());
            sender.sendMessage("Reset all resources for " + target.getName() + " to defaults.");
            return true;
        }

        ResourceType st = ResourceType.parse(args[2]);
        if (st == null) {
            sender.sendMessage("Unknown resource: " + args[2]);
            return true;
        }

        resourceService.reset(target.getUniqueId(), st);
        sender.sendMessage("Reset " + st.name().toLowerCase() + " for " + target.getName() + " to default.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return filterPrefix(SUBS, args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        // /resource <sub> <player>
        if (args.length == 2 && (sub.equals("get") || sub.equals("set") || sub.equals("reset"))) {
            return filterPrefix(onlinePlayerNames(), args[1]);
        }

        // /resource get <player> <resource>
        if (sub.equals("get") && args.length == 3) {
            return filterPrefix(resource_NAMES, args[2]);
        }

        // /resource set <player> <resource>
        if (sub.equals("set") && args.length == 3) {
            return filterPrefix(resource_NAMES, args[2]);
        }

        // /resource reset <player> <resource>
        if (sub.equals("reset") && args.length == 3) {
            return filterPrefix(resource_NAMES, args[2]);
        }

        // /resource set <player> <resource> <value>
        if (sub.equals("set") && args.length == 4) {
            // helpful suggestions; still lets them type anything
            return filterPrefix(List.of("0", "10", "50", "100", "150", "200"), args[3]);
        }

        return Collections.emptyList();
    }

    private static List<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private static List<String> filterPrefix(List<String> options, String prefixRaw) {
        String prefix = prefixRaw == null ? "" : prefixRaw.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(prefix))
                .collect(Collectors.toList());
    }
}