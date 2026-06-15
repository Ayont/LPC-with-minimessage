package de.ayont.lpc.moderation;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks muted players. A player counts as muted if they hold the configured LuckPerms/permission
 * node (managed by an external punishment system) or have an active in-memory temp-mute set via
 * {@code /lpc mute}. Temp-mutes use {@link Long#MAX_VALUE} as the "permanent" sentinel.
 */
public final class MuteService {

    private final LPC plugin;
    private final Map<UUID, Long> tempMutes = new ConcurrentHashMap<>();

    private volatile boolean enabled;
    private volatile boolean commandsEnabled;
    private volatile String permissionNode;
    private volatile String message;

    public MuteService(LPC plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean("mute.enabled", false);
        this.commandsEnabled = plugin.getConfig().getBoolean("mute.enable-commands", true);
        this.permissionNode = plugin.getConfig().getString("mute.permission-node", "lpc.muted");
        this.message = plugin.getConfig().getString("mute.message", "<red>You are currently muted.");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean areCommandsEnabled() {
        return enabled && commandsEnabled;
    }

    /** @return true if the player is muted by permission node or active temp-mute. */
    public boolean isMuted(org.bukkit.entity.Player player, long now) {
        if (!enabled) {
            return false;
        }
        if (!permissionNode.isEmpty() && player.hasPermission(permissionNode)) {
            return true;
        }
        return isTempMuted(player.getUniqueId(), now);
    }

    /** Pure temp-mute check with lazy expiry. */
    public boolean isTempMuted(UUID id, long now) {
        Long until = tempMutes.get(id);
        if (until == null) {
            return false;
        }
        if (until != Long.MAX_VALUE && until <= now) {
            tempMutes.remove(id);
            return false;
        }
        return true;
    }

    /** Temp-mutes a player for the given duration; a non-positive duration mutes permanently. */
    public void mute(UUID id, long now, long durationMillis) {
        tempMutes.put(id, durationMillis <= 0 ? Long.MAX_VALUE : now + durationMillis);
    }

    public void unmute(UUID id) {
        tempMutes.remove(id);
    }

    public Component notice() {
        return MiniMessage.miniMessage().deserialize(message);
    }
}
