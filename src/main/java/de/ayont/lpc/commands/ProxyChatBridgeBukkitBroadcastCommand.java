package de.ayont.lpc.commands;

import de.ayont.lpc.LPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ProxyChatBridgeBukkitBroadcastCommand implements CommandExecutor {
    private final LPC plugin;

    public ProxyChatBridgeBukkitBroadcastCommand(LPC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String string, String[] args) {
        if (commandSender.hasPermission("proxychatbridge.broadcast")) {
            if (plugin.getServer().getOnlinePlayers().size() > 0) {
                if (args.length > 1) {
                    Player player = commandSender instanceof Player ? (Player) commandSender :
                            plugin.getServer().getOnlinePlayers().iterator().next();

                    if (player != null) {
                        String group = args[0];
                        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).replace("&", "§");

                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> plugin.getChatManager()
                                .broadcastMessage(group, message, player));
                        commandSender.sendMessage(ChatColor.WHITE + "✉" + ChatColor.DARK_GRAY + " » "
                                + ChatColor.RESET + "Broadcast sent to group (" + group + ").");
                    } else {
                        commandSender.sendMessage(ChatColor.WHITE + "✉" + ChatColor.DARK_GRAY + " » "
                                + ChatColor.RESET + "No valid player found to send plugin message.");
                    }
                } else {
                    commandSender.sendMessage(ChatColor.WHITE + "✉" + ChatColor.DARK_GRAY + " » "
                            + ChatColor.RESET + "/pcbbb <group> <message>");
                }
            } else {
                commandSender.sendMessage(ChatColor.WHITE + "✉" + ChatColor.DARK_GRAY + " » "
                        + ChatColor.RESET + "This command only works if a player is online.");
            }
        } else {
            commandSender.sendMessage(ChatColor.WHITE + "✉" + ChatColor.DARK_GRAY + " » "
                    + ChatColor.RESET + "No permission.");
        }

        return true;
    }
}
