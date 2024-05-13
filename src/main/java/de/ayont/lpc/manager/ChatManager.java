package de.ayont.lpc.manager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.ayont.lpc.LPC;
import de.ayont.lpc.event.ExternalChatReceiveEvent;
import de.ayont.lpc.event.ExternalChatSendEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ChatManager {
    private final LPC plugin;

    public ChatManager(LPC plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(UUID uuid, String name, String format, String message, Player player) {
        ExternalChatSendEvent externalChatSendEvent = new ExternalChatSendEvent(uuid, name, format, message);

        plugin.getServer().getPluginManager().callEvent(externalChatSendEvent);

        if (!externalChatSendEvent.isCancelled()) {
            sendChatData(externalChatSendEvent.getUUID(), externalChatSendEvent.getName(),
                    externalChatSendEvent.getFormat(), externalChatSendEvent.getMessage(), player);
        }
    }

    public void broadcastMessage(String group, String message, Player player) {
        ExternalChatSendEvent externalChatSendEvent = new ExternalChatSendEvent(null, null, null, message);

        plugin.getServer().getPluginManager().callEvent(externalChatSendEvent);

        if (!externalChatSendEvent.isCancelled()) {
            sendBroadcastData(group, message, player);
        }
    }

    public void sendMessageToPlayers(UUID uuid, String name, String format, String message, String source) {
        sendMessageToPlayers(uuid, name, format, message, source, null);
    }

    public void sendMessageToPlayers(UUID uuid, String name, String format, String message, String source,
                                     List<UUID> uuidList) {
        String finalSource = !source.equals("") ? source : null;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            ExternalChatReceiveEvent externalChatReceiveEvent = new ExternalChatReceiveEvent(uuid, name, format,
                    message, finalSource);

            plugin.getServer().getPluginManager().callEvent(externalChatReceiveEvent);

            if (!externalChatReceiveEvent.isCancelled()) {
                String messageFormatted = externalChatReceiveEvent.getFormat().replace("%1$s",
                        externalChatReceiveEvent.getName()).replace("%2$s",
                        externalChatReceiveEvent.getMessage());

                plugin.getLogger().info(ChatColor.stripColor(messageFormatted));

                if (uuidList == null) {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        player.sendMessage(messageFormatted);
                    }
                } else {
                    for (UUID uuidPlayer : uuidList) {
                        Player player = plugin.getServer().getPlayer(uuidPlayer);

                        if (player != null) {
                            player.sendMessage(messageFormatted);
                        }
                    }
                }
            }
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private void sendChatData(UUID uuid, String name, String format, String message, Player player) {
        ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

        byteArrayDataOutput.writeUTF("ProxyChatBridge");
        byteArrayDataOutput.writeUTF("Message");
        byteArrayDataOutput.writeUTF(uuid != null ? uuid.toString() : "");
        byteArrayDataOutput.writeUTF(name);
        byteArrayDataOutput.writeUTF(format);
        byteArrayDataOutput.writeUTF(message);

        player.sendPluginMessage(plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
    }

    @SuppressWarnings("UnstableApiUsage")
    private void sendBroadcastData(String group, String message, Player player) {
        ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

        byteArrayDataOutput.writeUTF("ProxyChatBridge");
        byteArrayDataOutput.writeUTF("Broadcast");
        byteArrayDataOutput.writeUTF(group);
        byteArrayDataOutput.writeUTF(message);

        player.sendPluginMessage(plugin, "BungeeCord", byteArrayDataOutput.toByteArray());
    }
}
