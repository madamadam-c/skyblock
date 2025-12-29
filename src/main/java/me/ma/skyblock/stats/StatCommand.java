package me.ma.skyblock.stats;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class StatCommand implements CommandExecutor, TabCompleter {
    private final StatsService statsService;

    private static final List<String> SUBS = List.of("get", "set", "reset");
    private static final List<String> STAT_NAMES = List.of(Arrays.stream(StatType.class.getEnumConstants()).map(Enum::name).toArray(String[]::new));

    public StatCommand(StatsService statsService) {
        this.statsService = statsService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage:");
            sender.sendMessage("/stat get <player> [" + String.join("|", STAT_NAMES) + "]");
            sender.sendMessage("/stat set <player> <stat> <value>");
            sender.sendMessage("/stat reset <player> [stat]");
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
            sender.sendMessage("Usage: /stat get <player> [stat]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Player must be online (for now).");
            return true;
        }

        UUID uuid = target.getUniqueId();
        if (args.length == 2) {
            sender.sendMessage(target.getName() + " stats:");
            for (var s : StatType.values()) {
                sender.sendMessage("  " + s.name().toLowerCase() + "=" + statsService.get(uuid, s).getValue());
            }
            return true;
        }

        StatType st = StatType.parse(args[2]);
        if (st == null) {
            sender.sendMessage("Unknown stat: " + args[2]);
            return true;
        }

        sender.sendMessage(target.getName() + " " + st.name().toLowerCase() + "=" + statsService.get(uuid, st).getValue());
        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /stat set <player> <stat> <value>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Player must be online (for now).");
            return true;
        }

        StatType st = StatType.parse(args[2]);
        if (st == null) {
            sender.sendMessage("Unknown stat: " + args[2]);
            return true;
        }

        double value;
        try {
            value = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Value must be a number.");
            return true;
        }

        statsService.set(target.getUniqueId(), st, value);
        sender.sendMessage("Set " + target.getName() + " " + st.name().toLowerCase() + " = " + value);
        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /stat reset <player> [stat]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Player must be online (for now).");
            return true;
        }

        if (args.length == 2) {
            statsService.reset(target.getUniqueId());
            sender.sendMessage("Reset all stats for " + target.getName() + " to defaults.");
            return true;
        }

        StatType st = StatType.parse(args[2]);
        if (st == null) {
            sender.sendMessage("Unknown stat: " + args[2]);
            return true;
        }

        statsService.reset(target.getUniqueId(), st);
        sender.sendMessage("Reset " + st.name().toLowerCase() + " for " + target.getName() + " to default.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return filterPrefix(SUBS, args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        // /stat <sub> <player>
        if (args.length == 2 && (sub.equals("get") || sub.equals("set") || sub.equals("reset"))) {
            return filterPrefix(onlinePlayerNames(), args[1]);
        }

        // /stat get <player> <stat>
        if (sub.equals("get") && args.length == 3) {
            return filterPrefix(STAT_NAMES, args[2]);
        }

        // /stat set <player> <stat>
        if (sub.equals("set") && args.length == 3) {
            return filterPrefix(STAT_NAMES, args[2]);
        }

        // /stat reset <player> <stat>
        if (sub.equals("reset") && args.length == 3) {
            return filterPrefix(STAT_NAMES, args[2]);
        }

        // /stat set <player> <stat> <value>
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
