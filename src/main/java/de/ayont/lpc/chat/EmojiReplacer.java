package de.ayont.lpc.chat;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Replaces admin-configured text shortcuts (e.g. {@code :heart:}) in a player's already-safe
 * message component with literal replacement text/glyphs. Because the replacement table is entirely
 * server-defined and substitution runs via {@link Component#replaceText}, players can only trigger
 * keys the admin configured and can never inject MiniMessage tags or placeholders.
 */
public final class EmojiReplacer {

    private final LPC plugin;
    private volatile boolean enabled;
    private volatile boolean requirePermission;
    private volatile Map<String, String> replacements = Map.of();

    public EmojiReplacer(LPC plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean("emoji.enabled", true);
        this.requirePermission = plugin.getConfig().getBoolean("emoji.require-permission", false);
        Map<String, String> map = new LinkedHashMap<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("emoji.replacements");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String value = section.getString(key);
                if (value != null && !key.isEmpty()) {
                    map.put(key, value);
                }
            }
        }
        this.replacements = Map.copyOf(map);
    }

    /** Applies emoji replacement to the message if enabled and permitted. */
    public Component apply(Player source, Component message) {
        if (!enabled || replacements.isEmpty()) {
            return message;
        }
        if (requirePermission && !source.hasPermission("lpc.emoji")) {
            return message;
        }
        return replace(message, replacements);
    }

    /** Pure transform: replaces each shortcut literally within the component tree. */
    public static Component replace(Component message, Map<String, String> replacements) {
        Component out = message;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            out = out.replaceText(builder -> builder.matchLiteral(key).replacement(value));
        }
        return out;
    }
}
