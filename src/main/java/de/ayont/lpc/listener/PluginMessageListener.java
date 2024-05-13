package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import de.ayont.lpc.utils.UUIDUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {
    private final LPC plugin;

    public PluginMessageListener(LPC plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes));

        try {
            if (dataInputStream.readUTF().equals("ProxyChatBridge")) {
                String type = dataInputStream.readUTF();

                if (type.equals("Message")) {
                    String source = dataInputStream.readUTF();
                    UUID uuid = UUIDUtil.getUUID(dataInputStream.readUTF());
                    String name = dataInputStream.readUTF();
                    String format = dataInputStream.readUTF();
                    String message = dataInputStream.readUTF();
                    String players;

                    try {
                        players = dataInputStream.readUTF();
                    } catch (EOFException ignored) {
                        players = null;
                    }

                    if (players == null) {
                        plugin.getChatManager().sendMessageToPlayers(uuid, name, format, message, source);
                    } else {
                        List<UUID> uuidList = new ArrayList<>();

                        for (String uuidString : players.split(",")) {
                            UUID uuidPlayer = UUIDUtil.getUUID(uuidString);

                            if (uuidPlayer != null) {
                                uuidList.add(uuidPlayer);
                            }
                        }

                        plugin.getChatManager().sendMessageToPlayers(uuid, name, format, message, source, uuidList);
                    }
                } else if (type.equals("Broadcast")) {
                    plugin.getServer().broadcastMessage(dataInputStream.readUTF());
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
