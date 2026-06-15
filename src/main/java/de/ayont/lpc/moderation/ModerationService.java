package de.ayont.lpc.moderation;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gates chat messages on the async thread before rendering. Bundles mute, anti-spam cooldown,
 * repeat blocking, caps, profanity and anti-advert filters behind one {@link #process} call that
 * returns {@link ModResult} (ALLOW / BLOCK / TRANSFORM).
 *
 * <p>Operates only on pre-parse raw plaintext via {@link TextChecks}; it never builds a component
 * from player text or invokes the trusted MiniMessage parser / PlaceholderAPI on player content.
 */
public final class ModerationService {

    private record Recent(String normalized, long time) {
    }

    private final LPC plugin;
    private final MuteService muteService;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Recent> lastMessages = new ConcurrentHashMap<>();

    private volatile boolean enabled;
    private volatile boolean spamEnabled;
    private volatile long cooldownMillis;
    private volatile String spamMessage;
    private volatile boolean repeatEnabled;
    private volatile long repeatExpiryMillis;
    private volatile String repeatMessage;
    private volatile boolean capsEnabled;
    private volatile int capsMinLength;
    private volatile double capsMaxRatio;
    private volatile boolean capsBlock;
    private volatile String capsMessage;
    private volatile boolean profanityEnabled;
    private volatile boolean profanityBlock;
    private volatile List<String> profanityWords;
    private volatile List<String> profanityNormalized;
    private volatile char profanityMaskChar;
    private volatile String profanityMessage;
    private volatile boolean advertEnabled;
    private volatile boolean advertBlock;
    private volatile boolean advertDetectIp;
    private volatile List<String> advertAllowlist;
    private volatile String advertMask;
    private volatile String advertMessage;

    public ModerationService(LPC plugin, MuteService muteService) {
        this.plugin = plugin;
        this.muteService = muteService;
        reload();
    }

    public void reload() {
        var config = plugin.getConfig();
        this.enabled = config.getBoolean("moderation.enabled", false);

        this.spamEnabled = config.getBoolean("anti-spam.enabled", true);
        this.cooldownMillis = config.getLong("anti-spam.cooldown-millis", 1500);
        this.spamMessage = config.getString("anti-spam.message", "<red>Please wait <yellow><seconds>s</yellow> before chatting again.");

        this.repeatEnabled = config.getBoolean("repeat-filter.enabled", true);
        this.repeatExpiryMillis = config.getLong("repeat-filter.expiry-millis", 30_000);
        this.repeatMessage = config.getString("repeat-filter.message", "<red>Please don't repeat the same message.");

        this.capsEnabled = config.getBoolean("caps-filter.enabled", true);
        this.capsMinLength = config.getInt("caps-filter.min-length", 8);
        this.capsMaxRatio = config.getDouble("caps-filter.max-uppercase-ratio", 0.7);
        this.capsBlock = "BLOCK".equalsIgnoreCase(config.getString("caps-filter.action", "LOWERCASE"));
        this.capsMessage = config.getString("caps-filter.message", "<red>Please don't shout.");

        this.profanityEnabled = config.getBoolean("profanity-filter.enabled", false);
        this.profanityBlock = "BLOCK".equalsIgnoreCase(config.getString("profanity-filter.action", "MASK"));
        this.profanityWords = List.copyOf(config.getStringList("profanity-filter.words"));
        this.profanityNormalized = this.profanityWords.stream().map(TextChecks::normalize).toList();
        String maskChar = config.getString("profanity-filter.mask-char", "*");
        this.profanityMaskChar = maskChar.isEmpty() ? '*' : maskChar.charAt(0);
        this.profanityMessage = config.getString("profanity-filter.message", "<red>Watch your language.");

        this.advertEnabled = config.getBoolean("anti-advert.enabled", false);
        this.advertBlock = "BLOCK".equalsIgnoreCase(config.getString("anti-advert.action", "REDACT"));
        this.advertDetectIp = config.getBoolean("anti-advert.detect-ip", true);
        this.advertAllowlist = List.copyOf(config.getStringList("anti-advert.allowlist"));
        this.advertMask = config.getString("anti-advert.mask", "[link removed]");
        this.advertMessage = config.getString("anti-advert.message", "<red>Advertising is not allowed.");
    }

    /**
     * Evaluates all filters against the raw message.
     *
     * @return BLOCK (cancel + notice), TRANSFORM (cleaned text) or ALLOW.
     */
    public ModResult process(Player player, String raw) {
        long now = System.currentTimeMillis();

        if (muteService.isMuted(player, now)) {
            return ModResult.block(muteService.notice());
        }

        if (!enabled) {
            return ModResult.allow();
        }

        UUID id = player.getUniqueId();

        if (spamEnabled && !player.hasPermission("lpc.bypass.spam")) {
            Long last = cooldowns.get(id);
            if (last != null && now - last < cooldownMillis) {
                long secondsLeft = Math.max(1, (cooldownMillis - (now - last) + 999) / 1000);
                return ModResult.block(notice(spamMessage, "seconds", Long.toString(secondsLeft)));
            }
            cooldowns.put(id, now);
        }

        String normalized = TextChecks.normalize(raw);
        if (repeatEnabled && !player.hasPermission("lpc.bypass.repeat")) {
            Recent recent = lastMessages.get(id);
            if (recent != null && now - recent.time() < repeatExpiryMillis && recent.normalized().equals(normalized)) {
                return ModResult.block(notice(repeatMessage));
            }
            lastMessages.put(id, new Recent(normalized, now));
        }

        String text = raw;

        if (capsEnabled && !player.hasPermission("lpc.bypass.caps")
                && TextChecks.isShout(text, capsMinLength, capsMaxRatio)) {
            if (capsBlock) {
                return ModResult.block(notice(capsMessage));
            }
            text = text.toLowerCase(Locale.ROOT);
        }

        if (profanityEnabled && !player.hasPermission("lpc.bypass.profanity")
                && TextChecks.containsAny(TextChecks.normalize(text), profanityNormalized)) {
            if (profanityBlock) {
                return ModResult.block(notice(profanityMessage));
            }
            text = TextChecks.maskWords(text, profanityWords, profanityMaskChar);
        }

        if (advertEnabled && !player.hasPermission("lpc.bypass.advert")
                && TextChecks.containsAdvert(text, advertDetectIp, advertAllowlist)) {
            if (advertBlock) {
                return ModResult.block(notice(advertMessage));
            }
            text = TextChecks.maskAdvert(text, advertDetectIp, advertAllowlist, advertMask);
        }

        return text.equals(raw) ? ModResult.allow() : ModResult.transform(text);
    }

    private static Component notice(String raw) {
        return MiniMessage.miniMessage().deserialize(raw);
    }

    private static Component notice(String raw, String key, String value) {
        return MiniMessage.miniMessage().deserialize(raw, Placeholder.unparsed(key, value));
    }
}
