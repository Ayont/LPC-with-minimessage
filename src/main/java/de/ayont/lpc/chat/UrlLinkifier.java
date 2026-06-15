package de.ayont.lpc.chat;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Turns URLs found in a player's already-safe message into clickable (Paper) or merely styled
 * (Spigot) links — without ever widening the player parser.
 *
 * <p>Safety: the click target is the exact regex-matched substring, scheme-allowlisted, length
 * capped, and {@link ClickEvent#openUrl(String) openUrl} ONLY (never run_command / suggest), so a
 * player cannot craft a command-running or arbitrary-tooltip click. The link style and hover label
 * are trusted config. The player message itself is still only parsed by the restricted color
 * parser; this is a post-pass over the resulting component.
 */
public final class UrlLinkifier {

    private static final Pattern URL = Pattern.compile("(?i)\\b(?:https?://)?(?:[a-z0-9-]+\\.)+[a-z]{2,}(?:/\\S*)?");

    private final LPC plugin;
    private volatile boolean enabled;
    private volatile String style;
    private volatile String hoverText;
    private volatile List<String> allowedSchemes;
    private volatile int maxLength;

    public UrlLinkifier(LPC plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean("link-urls.enabled", true);
        this.style = plugin.getConfig().getString("link-urls.style", "<blue><underlined>");
        this.hoverText = plugin.getConfig().getString("link-urls.hover-text", "<gray>Open link");
        List<String> schemes = plugin.getConfig().getStringList("link-urls.allowed-schemes");
        this.allowedSchemes = schemes.isEmpty() ? List.of("http", "https") : List.copyOf(schemes);
        this.maxLength = plugin.getConfig().getInt("link-urls.max-length", 200);
    }

    /** Linkifies URLs in the message if enabled and permitted. */
    public Component apply(Player source, Component message, boolean clickable) {
        if (!enabled || !source.hasPermission("lpc.chatlinks")) {
            return message;
        }
        return linkify(message, clickable, style, hoverText, allowedSchemes, maxLength);
    }

    /** Pure transform: matches URLs and attaches an openUrl click (when clickable) + hover. */
    public static Component linkify(Component message, boolean clickable, String style, String hoverText,
                                    List<String> allowedSchemes, int maxLength) {
        // Parse operator-configured style/hover with a cosmetic-only parser so a misconfigured
        // link-urls.style / hover-text cannot attach interactive (click/hover) tags to every link.
        MiniMessage cosmetic = PlayerMessages.colorParser(true);
        Component hover = cosmetic.deserialize(hoverText);
        return message.replaceText(config -> config.match(URL).replacement((matchResult, builder) -> {
            String matched = matchResult.group();
            String href = matched.contains("://") ? matched : "https://" + matched;
            String scheme = href.substring(0, href.indexOf("://")).toLowerCase(Locale.ROOT);

            if (matched.length() > maxLength || !allowedSchemes.contains(scheme)) {
                return builder; // leave the matched text untouched
            }

            Component link = cosmetic.deserialize(style + "<url>", Placeholder.unparsed("url", matched));
            if (clickable) {
                link = link.clickEvent(ClickEvent.openUrl(href))
                        .hoverEvent(HoverEvent.showText(hover));
            }
            return link;
        }));
    }
}
