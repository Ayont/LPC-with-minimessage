package de.ayont.lpc.commands;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LPCCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "version", "help", "mute", "unmute");
    private static final List<String> TARGET_SUBCOMMANDS = List.of("mute", "unmute");
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final LPC plugin;

    public LPCCommand(LPC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "version" -> handleVersion(sender);
            case "mute" -> handleMute(sender, args);
            case "unmute" -> handleUnmute(sender, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("lpc.reload")) {
            plugin.send(sender, mini("<red>You don't have permission to do that."));
            return;
        }
        plugin.reloadConfig();
        plugin.reloadServices();
        String raw = plugin.getConfig().getString("reload-message", "<green>Reloaded LPC configuration!");
        plugin.send(sender, mini(raw));
    }

    @SuppressWarnings("deprecation") // getDescription() is cross-platform
    private void handleVersion(CommandSender sender) {
        String platform = plugin.isFolia() ? "Folia" : plugin.isPaper() ? "Paper" : "Spigot";
        plugin.send(sender, mini("<gradient:#B754F4:#FC00FF>LPC</gradient> <gray>v<white>"
                + plugin.getDescription().getVersion() + "</white> <dark_gray>— <gray>MiniMessage chat formatter."));
        plugin.send(sender, mini("<dark_gray>Platform: <white>" + platform
                + "</white> <dark_gray>| <gray>Target: Minecraft <white>26.2</white> · Java <white>25</white> · Adventure <white>5</white>"));
    }

    private void handleMute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lpc.mute")) {
            plugin.send(sender, mini("<red>You don't have permission to do that."));
            return;
        }
        if (!plugin.getMuteService().areCommandsEnabled()) {
            plugin.send(sender, mini("<red>Mute commands are disabled in the config."));
            return;
        }
        if (args.length < 2) {
            plugin.send(sender, mini("<red>Usage: /lpc mute <player> [duration e.g. 10m]"));
            return;
        }
        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            plugin.send(sender, mini("<red>Player <white><name></white> is not online.", "name", args[1]));
            return;
        }
        long duration = args.length >= 3 ? parseDuration(args[2]) : 0L;
        plugin.getMuteService().mute(target.getUniqueId(), System.currentTimeMillis(), duration);
        String suffix = duration > 0 ? " for " + args[2] : " permanently";
        plugin.send(sender, mini("<green>Muted <white><name></white>" + suffix + ".", "name", target.getName()));
    }

    private void handleUnmute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lpc.mute")) {
            plugin.send(sender, mini("<red>You don't have permission to do that."));
            return;
        }
        if (args.length < 2) {
            plugin.send(sender, mini("<red>Usage: /lpc unmute <player>"));
            return;
        }
        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            plugin.send(sender, mini("<red>Player <white><name></white> is not online.", "name", args[1]));
            return;
        }
        plugin.getMuteService().unmute(target.getUniqueId());
        plugin.send(sender, mini("<green>Unmuted <white><name></white>.", "name", target.getName()));
    }

    private void sendHelp(CommandSender sender) {
        plugin.send(sender, mini("<gradient:#B754F4:#FC00FF>LPC</gradient> <gray>commands:"));
        plugin.send(sender, mini("<dark_gray>- <white>/lpc reload</white> <dark_gray>» <gray>Reload the configuration"));
        plugin.send(sender, mini("<dark_gray>- <white>/lpc version</white> <dark_gray>» <gray>Show the plugin version"));
        if (plugin.getMuteService().areCommandsEnabled()) {
            plugin.send(sender, mini("<dark_gray>- <white>/lpc mute <player> [duration]</white> <dark_gray>» <gray>Mute a player"));
            plugin.send(sender, mini("<dark_gray>- <white>/lpc unmute <player></white> <dark_gray>» <gray>Unmute a player"));
        }
    }

    /** Parses durations like {@code 30s}, {@code 10m}, {@code 2h}, {@code 1d}, or plain seconds. */
    static long parseDuration(String input) {
        if (input == null || input.isBlank()) {
            return 0L;
        }
        String trimmed = input.trim();
        char unit = trimmed.charAt(trimmed.length() - 1);
        try {
            if (Character.isDigit(unit)) {
                return Long.parseLong(trimmed) * 1000L;
            }
            long amount = Long.parseLong(trimmed.substring(0, trimmed.length() - 1).trim());
            return switch (Character.toLowerCase(unit)) {
                case 's' -> amount * 1000L;
                case 'm' -> amount * 60_000L;
                case 'h' -> amount * 3_600_000L;
                case 'd' -> amount * 86_400_000L;
                default -> 0L;
            };
        } catch (NumberFormatException invalid) {
            return 0L;
        }
    }

    private static Component mini(String raw) {
        return MM.deserialize(raw);
    }

    private static Component mini(String raw, String key, String value) {
        return MM.deserialize(raw, Placeholder.unparsed(key, value));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return SUBCOMMANDS.stream().filter(sub -> sub.startsWith(prefix)).toList();
        }
        if (args.length == 2 && TARGET_SUBCOMMANDS.contains(args[0].toLowerCase())) {
            String prefix = args[1].toLowerCase();
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .toList();
        }
        return List.of();
    }
}
