package de.ayont.lpc.renderer;

import de.ayont.lpc.LPC;
import io.papermc.paper.chat.ChatRenderer;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.track.Track;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LPCChatRenderer implements ChatRenderer {

    private final LuckPerms luckPerms;
    private final LPC plugin;
    private final MiniMessage miniMessage;
    private final boolean hasPapi;

    private final Map<String, String> legacyToMiniMessageColors = new HashMap<>() {
        {
            put("&0", "<black>");
            put("&1", "<dark_blue>");
            put("&2", "<dark_green>");
            put("&3", "<dark_aqua>");
            put("&4", "<dark_red>");
            put("&5", "<dark_purple>");
            put("&6", "<gold>");
            put("&7", "<gray>");
            put("&8", "<dark_gray>");
            put("&9", "<blue>");
            put("&a", "<green>");
            put("&b", "<aqua>");
            put("&c", "<red>");
            put("&d", "<light_purple>");
            put("&e", "<yellow>");
            put("&f", "<white>");
        }
    };

    public LPCChatRenderer(LPC plugin) {
        this.luckPerms = LuckPermsProvider.get();
        this.plugin = plugin;
        this.miniMessage = MiniMessage.builder().build();
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        hasPapi = pluginManager.getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        final CachedMetaData metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(source);
        final String group = Objects.requireNonNull(metaData.getPrimaryGroup(), "Primary group cannot be null");

        boolean hasPermission = source.hasPermission("lpc.chatcolor");

        String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);

        if (hasPermission) {
            for (Map.Entry<String, String> entry : legacyToMiniMessageColors.entrySet()) {
                plainMessage = plainMessage.replace(entry.getKey(), entry.getValue());
            }
        }

        String formatKey = "group-formats." + group;
        String format = plugin.getConfig().getString(formatKey);

        if (format == null) {
            for (String trackName : plugin.getConfig().getConfigurationSection("track-formats").getKeys(false)) {
                Track track = this.luckPerms.getTrackManager().getTrack(trackName);
                if (track == null) continue;
                if (track.containsGroup(group)) {
                    formatKey = "track-formats." + trackName;
                    format = plugin.getConfig().getString(formatKey);
                    break;
                }
            }
        }

        if (format == null) {
            format = plugin.getConfig().getString("chat-format");
        }

        format = format.replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("{prefixes}", String.join(" ", metaData.getPrefixes().values()))
                .replace("{suffixes}", String.join(" ", metaData.getSuffixes().values()))
                .replace("{world}", source.getWorld().getName())
                .replace("{name}", source.getName())
                .replace("{displayname}", PlainTextComponentSerializer.plainText().serialize(source.displayName()))
                .replace("{username-color}", metaData.getMetaValue("username-color") != null ? Objects.requireNonNull(metaData.getMetaValue("username-color")) : "")
                .replace("{message-color}", metaData.getMetaValue("message-color") != null ? Objects.requireNonNull(metaData.getMetaValue("message-color")) : "");

        if (!hasPermission) {
            for (Map.Entry<String, String> entry : legacyToMiniMessageColors.entrySet()) {
                plainMessage = plainMessage.replace(entry.getValue(), entry.getKey());
            }
        }

        format = format.replace("{message}", plainMessage);

        if (hasPapi) {
            format = PlaceholderAPI.setPlaceholders(source, format);
        }

        return miniMessage.deserialize(format);
    }
}