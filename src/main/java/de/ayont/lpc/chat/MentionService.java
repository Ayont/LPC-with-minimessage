package de.ayont.lpc.chat;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Highlights mentioned online player names in a message and (on Paper) pings the mentioned players
 * with a sound + action bar.
 *
 * <p>Safety: matching uses a regex built ONLY from a validated, exact, length-capped snapshot of
 * live online names — never from arbitrary player text. The highlight is an operator-authored
 * MiniMessage template with the matched name injected via {@link Placeholder#unparsed}, so no player
 * input is ever re-parsed with the full tag set.
 */
public final class MentionService {

    /** Result of a highlight pass: the decorated component plus the set of matched names. */
    public record Result(Component message, Set<String> mentioned) {
    }

    private static final int MAX_NAME_LENGTH = 16;

    private final LPC plugin;
    private volatile boolean enabled;
    private volatile boolean requireAt;
    private volatile String highlightFormat;
    private volatile int maxMentions;
    private volatile String actionBar;
    private volatile String soundName;
    private volatile float volume;
    private volatile float pitch;

    public MentionService(LPC plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean("mentions.enabled", true);
        this.requireAt = plugin.getConfig().getBoolean("mentions.require-at-symbol", false);
        this.highlightFormat = plugin.getConfig().getString("mentions.highlight-format", "<gold>@<name></gold>");
        this.maxMentions = plugin.getConfig().getInt("mentions.max-mentions-per-message", 5);
        this.actionBar = plugin.getConfig().getString("mentions.actionbar-text",
                "<gold>You were mentioned by <white><sender></white>");
        this.soundName = plugin.getConfig().getString("mentions.ping-sound", "entity.experience_orb.pickup");
        this.volume = (float) plugin.getConfig().getDouble("mentions.ping-volume", 1.0);
        this.pitch = (float) plugin.getConfig().getDouble("mentions.ping-pitch", 1.0);
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Highlights names from {@code names} in the message; returns the decorated message + matches. */
    public Result highlight(Component message, Set<String> names) {
        if (!enabled || names.isEmpty()) {
            return new Result(message, Set.of());
        }
        return highlight(message, names, requireAt, highlightFormat, maxMentions);
    }

    /** Pure transform used by {@link #highlight(Component, Set)} and unit tests. */
    public static Result highlight(Component message, Set<String> names, boolean requireAt,
                                   String highlightFormat, int maxMentions) {
        StringBuilder alternation = new StringBuilder();
        for (String name : names) {
            if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
                continue;
            }
            if (alternation.length() > 0) {
                alternation.append('|');
            }
            alternation.append(Pattern.quote(name));
        }
        if (alternation.length() == 0) {
            return new Result(message, Set.of());
        }

        String prefix = requireAt ? "@" : "";
        Pattern pattern = Pattern.compile("(?i)" + prefix + "\\b(" + alternation + ")\\b");
        Set<String> mentioned = new LinkedHashSet<>();
        int[] count = {0};
        MiniMessage mm = MiniMessage.miniMessage();

        Component out = message.replaceText(config -> config.match(pattern).replacement((matchResult, builder) -> {
            if (count[0] >= maxMentions) {
                return builder;
            }
            String matchedName = matchResult.group(1);
            String canonical = canonical(names, matchedName);
            mentioned.add(canonical);
            count[0]++;
            return mm.deserialize(highlightFormat, Placeholder.unparsed("name", canonical));
        }));

        return new Result(out, mentioned);
    }

    private static String canonical(Set<String> names, String matched) {
        for (String name : names) {
            if (name.equalsIgnoreCase(matched)) {
                return name;
            }
        }
        return matched;
    }

    /** Schedules a main-thread ping for every mentioned online player. Safe to call async. */
    public void pingAll(Set<String> mentioned, String senderName) {
        if (!enabled || mentioned.isEmpty()) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (String name : mentioned) {
                Player target = plugin.getServer().getPlayerExact(name);
                if (target != null) {
                    ping(target, senderName);
                }
            }
        });
    }

    /** Pings a mentioned player with sound + action bar. Call on the main thread. */
    public void ping(Player target, String senderName) {
        if (target.hasPermission("lpc.mention.exempt")) {
            return;
        }
        if (soundName != null && !soundName.isEmpty()) {
            // String overload takes the namespaced sound name and works on Paper + Spigot.
            target.playSound(target.getLocation(), soundName, volume, pitch);
        }
        if (plugin.isPaper() && actionBar != null && !actionBar.isEmpty()) {
            Component bar = MiniMessage.miniMessage().deserialize(actionBar, Placeholder.unparsed("sender", senderName));
            target.sendActionBar(bar);
        }
    }

    /** Builds a length-capped snapshot of online names for matching. */
    public static Set<String> onlineNames(Iterable<? extends Player> online) {
        Set<String> names = new HashSet<>();
        for (Player player : online) {
            String name = player.getName();
            if (!name.isEmpty() && name.length() <= MAX_NAME_LENGTH) {
                names.add(name);
            }
        }
        return names;
    }
}
