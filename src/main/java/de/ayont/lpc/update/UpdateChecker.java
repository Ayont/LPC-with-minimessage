package de.ayont.lpc.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Checks Modrinth for a newer release of the plugin and notifies permitted players on join.
 * Network access happens asynchronously; all results are read-only.
 */
public final class UpdateChecker implements Listener {

    private static final String PROJECT_SLUG = "lpc-chat";
    private static final String VERSIONS_URL =
            "https://api.modrinth.com/v2/project/" + PROJECT_SLUG + "/version";
    private static final String DOWNLOAD_URL = "https://modrinth.com/plugin/" + PROJECT_SLUG;
    private static final String NOTIFY_PERMISSION = "lpc.update";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final LPC plugin;
    private final String currentVersion;
    // Written only by the async check; updateAvailable is set true AFTER latestVersion, so a reader
    // that sees updateAvailable == true is guaranteed to see a non-null latestVersion.
    private volatile String latestVersion;
    private volatile boolean updateAvailable;

    @SuppressWarnings("deprecation") // getDescription() is cross-platform (Paper + Spigot)
    public UpdateChecker(LPC plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    /** Runs the version check off the main thread. */
    public void checkAsync() {
        plugin.getScheduler().runAsync(this::check);
    }

    private void check() {
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(VERSIONS_URL))
                    .header("User-Agent", "LPC-chat/" + currentVersion + " (Modrinth update check)")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                plugin.getLogger().fine("Update check skipped: HTTP " + response.statusCode());
                return;
            }

            JsonArray versions = JsonParser.parseString(response.body()).getAsJsonArray();
            if (versions.isEmpty()) {
                return;
            }

            JsonObject newest = versions.get(0).getAsJsonObject();
            this.latestVersion = newest.get("version_number").getAsString();

            if (Versions.isNewer(latestVersion, currentVersion)) {
                this.updateAvailable = true;
                plugin.getLogger().info("A new version of LPC is available: " + latestVersion
                        + " (current: " + currentVersion + "). Download: " + DOWNLOAD_URL);
            } else {
                plugin.getLogger().info("LPC is up to date (" + currentVersion + ").");
            }
        } catch (Exception exception) {
            plugin.getLogger().fine("Update check failed: " + exception.getMessage());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!updateAvailable || !event.getPlayer().hasPermission(NOTIFY_PERMISSION)) {
            return;
        }
        // The version string comes from the Modrinth API, so insert it as unparsed text, never as
        // MiniMessage markup.
        Component message = MiniMessage.miniMessage().deserialize(
                "<gradient:#B754F4:#FC00FF>[LPC]</gradient> <yellow>A new version <white><version></white> "
                        + "is available! <click:open_url:'" + DOWNLOAD_URL + "'>"
                        + "<underlined>Download on Modrinth</underlined></click>",
                Placeholder.unparsed("version", latestVersion));
        plugin.send(event.getPlayer(), message);
    }
}
