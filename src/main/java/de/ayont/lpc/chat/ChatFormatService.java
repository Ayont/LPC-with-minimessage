package de.ayont.lpc.chat;

import de.ayont.lpc.LPC;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.track.Track;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Resolves and renders chat lines for both the Paper and Spigot listeners.
 *
 * <p>Security model: the configured format, LuckPerms prefixes/suffixes and PlaceholderAPI output
 * are trusted (server-controlled) and parsed with the full MiniMessage tag set. The player's own
 * message is built separately via {@link #messageComponent(String, boolean)} with a restricted
 * parser and injected as a pre-resolved component, so it can never be re-parsed as MiniMessage nor
 * expanded by PlaceholderAPI.
 *
 * <p>Thread-safety: {@link #render} runs on the async chat thread, so all config-derived state is
 * snapshotted into {@code volatile} fields by {@link #reload()} (called on the main thread). The
 * live {@link FileConfiguration} is never read from the async path.
 */
public final class ChatFormatService {

    /** Built-in fallback used when no usable format is configured. */
    public static final String DEFAULT_FORMAT = "{prefix}{name}<dark_gray> »<reset> {message}";

    private static final String MESSAGE_TOKEN = "{message}";
    private static final String MESSAGE_TAG_PREFIX = "lpcmsg";
    private static final String STYLE_INNER_TAG = "lpcstyled";
    private static final String DEFAULT_GROUP = "default";
    // A gradient spec may only contain colour stops (hex/named), separators and phase markers — never
    // tag-breaking characters, so a LuckPerms meta value cannot inject MiniMessage tags.
    private static final Pattern GRADIENT_SPEC = Pattern.compile("[#a-zA-Z0-9:,_-]+");

    private final LPC plugin;
    private final LuckPerms luckPerms;
    private final MiniMessage trustedMiniMessage;

    // Snapshotted config state (read on the async chat thread).
    private volatile String chatFormat;
    private volatile Map<String, String> groupFormats;
    private volatile Map<String, String> trackFormats;
    private volatile MiniMessage colorParser;
    private volatile boolean hasPapi;
    private volatile boolean messageStylesEnabled;
    private volatile Map<String, String> groupMessageStyles;
    private volatile Map<String, String> trackMessageStyles;
    private volatile String defaultMessageStyle;
    private volatile boolean gradientNamesEnabled;
    private volatile String gradientMetaKey;
    private volatile Map<String, String> groupNameGradients;

    public ChatFormatService(LPC plugin) {
        this.plugin = plugin;
        this.luckPerms = LuckPermsProvider.get();
        this.trustedMiniMessage = MiniMessage.miniMessage();
        reload();
    }

    /** Re-reads and snapshots config-derived state. Call after {@code reloadConfig()}. */
    public void reload() {
        FileConfiguration config = plugin.getConfig();
        this.chatFormat = config.getString("chat-format", DEFAULT_FORMAT);
        this.groupFormats = readStringSection(config, "group-formats");
        this.trackFormats = readStringSection(config, "track-formats");
        this.colorParser = PlayerMessages.colorParser(config.getBoolean("allow-gradient-tags", true));
        this.hasPapi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        this.messageStylesEnabled = config.getBoolean("message-styles.enabled", false);
        this.groupMessageStyles = readStringSection(config, "group-message-styles");
        this.trackMessageStyles = readStringSection(config, "track-message-styles");
        this.defaultMessageStyle = config.getString("default-message-style", "");
        this.gradientNamesEnabled = config.getBoolean("gradient-names.enabled", false);
        this.gradientMetaKey = config.getString("gradient-names.default-meta-key", "username-gradient");
        this.groupNameGradients = readStringSection(config, "group-name-gradients");
    }

    private static Map<String, String> readStringSection(FileConfiguration config, String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return Map.of();
        }
        Map<String, String> values = new HashMap<>();
        for (String key : section.getKeys(false)) {
            String value = section.getString(key);
            if (value != null) {
                values.put(key, value);
            }
        }
        return Map.copyOf(values);
    }

    /**
     * Builds the safe component for a player's raw message.
     *
     * @param raw        the raw message text
     * @param allowColor whether the player may use colour codes / cosmetic tags
     * @return a safe component (literal text when colour is not allowed)
     */
    public Component messageComponent(String raw, boolean allowColor) {
        return PlayerMessages.render(colorParser, raw, allowColor);
    }

    /**
     * Renders the full chat line as a component.
     *
     * @param source      the chatting player
     * @param message     the player's message, already built via {@link #messageComponent}
     * @param displayName the player's display name component
     * @return the fully rendered chat line
     */
    public Component render(Player source, Component message, Component displayName) {
        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(source);
        String group = metaData.getPrimaryGroup();
        if (group == null) {
            group = DEFAULT_GROUP;
        }

        Component safeDisplayName = displayName != null ? displayName : Component.text(source.getName());
        Component styledMessage = applyMessageStyle(group, message);

        // Reserve the message position FIRST, on the trusted config format, with an unguessable
        // per-render tag. Doing this before meta-token/PlaceholderAPI substitution means nothing in a
        // prefix, suffix or placeholder value can collide with the message token or inject the tag.
        String messageTag = MESSAGE_TAG_PREFIX + UUID.randomUUID().toString().replace("-", "");
        String format = resolveFormat(group).replace(MESSAGE_TOKEN, "<" + messageTag + ">");

        format = applyMetaTokens(format, source, metaData, safeDisplayName);

        if (hasPapi) {
            format = PlaceholderAPI.setPlaceholders(source, format);
        }

        return trustedMiniMessage.deserialize(format, Placeholder.component(messageTag, styledMessage));
    }

    /**
     * Renders an operator-authored template (e.g. join/quit/death message) for a player. No player
     * chat text is involved; {@code extra} resolvers can inject pre-built components such as the
     * vanilla death message via a MiniMessage placeholder.
     */
    public Component renderTemplate(Player player, String template, Component displayName, TagResolver... extra) {
        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        Component safeDisplayName = displayName != null ? displayName : Component.text(player.getName());
        String format = applyMetaTokens(template, player, metaData, safeDisplayName);
        if (hasPapi) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }
        return trustedMiniMessage.deserialize(format, extra);
    }

    private Component applyMessageStyle(String group, Component message) {
        if (!messageStylesEnabled) {
            return message;
        }
        String style = resolveMessageStyle(group);
        if (style == null || style.isEmpty()) {
            return message;
        }
        // The style template is operator-authored (trusted); the safe message is nested via a
        // placeholder so it is wrapped, never re-parsed.
        String wrapped = style.replace(MESSAGE_TOKEN, "<" + STYLE_INNER_TAG + ">");
        return trustedMiniMessage.deserialize(wrapped, Placeholder.component(STYLE_INNER_TAG, message));
    }

    private String resolveMessageStyle(String group) {
        String style = groupMessageStyles.get(group);
        if (style == null) {
            style = resolveByTrack(trackMessageStyles, group);
        }
        if (style == null) {
            style = defaultMessageStyle;
        }
        return style;
    }

    private String resolveFormat(String group) {
        String format = groupFormats.get(group);
        if (format == null) {
            format = resolveByTrack(trackFormats, group);
        }
        if (format == null) {
            format = chatFormat;
        }
        return format == null || format.isEmpty() ? DEFAULT_FORMAT : format;
    }

    private String resolveByTrack(Map<String, String> byTrack, String group) {
        for (Map.Entry<String, String> entry : byTrack.entrySet()) {
            Track track = luckPerms.getTrackManager().getTrack(entry.getKey());
            if (track != null && track.containsGroup(group)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String applyMetaTokens(String format, Player source, CachedMetaData metaData, Component displayName) {
        return format
                .replace("{prefix}", orEmpty(metaData.getPrefix()))
                .replace("{suffix}", orEmpty(metaData.getSuffix()))
                .replace("{prefixes}", String.join(" ", metaData.getPrefixes().values()))
                .replace("{suffixes}", String.join(" ", metaData.getSuffixes().values()))
                .replace("{world}", source.getWorld().getName())
                .replace("{gradient-name}", gradientName(source, metaData))
                .replace("{name}", source.getName())
                .replace("{displayname}", trustedMiniMessage.serialize(displayName))
                .replace("{username-color}", orEmpty(metaData.getMetaValue("username-color")))
                .replace("{message-color}", orEmpty(metaData.getMetaValue("message-color")));
    }

    private String gradientName(Player source, CachedMetaData metaData) {
        if (!gradientNamesEnabled) {
            return source.getName();
        }
        String spec = metaData.getMetaValue(gradientMetaKey);
        if (spec == null || spec.isBlank()) {
            String group = metaData.getPrimaryGroup();
            if (group != null) {
                spec = groupNameGradients.get(group);
            }
        }
        if (spec == null || spec.isBlank() || !GRADIENT_SPEC.matcher(spec).matches()) {
            return source.getName();
        }
        // spec is validated colour-stop text; the player name is a safe account name.
        return "<gradient:" + spec + ">" + source.getName() + "</gradient>";
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
