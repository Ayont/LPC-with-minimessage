package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import de.ayont.lpc.chat.ChatFormatService;
import de.ayont.lpc.chat.ItemPlaceholder;
import de.ayont.lpc.chat.MentionService;
import de.ayont.lpc.moderation.ModResult;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Paper chat listener. Moderates the raw message, decorates the safe message component (emoji, URLs,
 * mention highlighting), then installs a per-viewer {@link io.papermc.paper.chat.ChatRenderer}.
 */
public class AsyncChatListener implements Listener {

    private final LPC plugin;
    private final ChatFormatService service;

    public AsyncChatListener(LPC plugin) {
        this.plugin = plugin;
        this.service = plugin.getChatFormatService();
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.isDisabledWorld(player.getWorld().getName())) {
            return;
        }

        String raw = PlainTextComponentSerializer.plainText().serialize(event.message());
        ModResult moderation = plugin.getModerationService().process(player, raw);
        if (moderation.isBlocked()) {
            event.setCancelled(true);
            if (moderation.notice() != null) {
                plugin.send(player, moderation.notice());
            }
            return;
        }
        String effectiveRaw = moderation.action() == ModResult.Action.TRANSFORM ? moderation.text() : raw;

        boolean allowColor = player.hasPermission("lpc.chatcolor");
        Component base = service.messageComponent(effectiveRaw, allowColor);
        base = plugin.getEmojiReplacer().apply(player, base);
        base = plugin.getUrlLinkifier().apply(player, base, true);

        MentionService.Result mention = plugin.getMentionService()
                .highlight(base, MentionService.onlineNames(plugin.getServer().getOnlinePlayers()));
        plugin.getMentionService().pingAll(mention.mentioned(), player.getName());

        Component finalMessage = mention.message();
        Component displayName = plugin.displayNameOf(player);
        event.renderer((source, sourceDisplayName, message, viewer) ->
                ItemPlaceholder.apply(plugin, source, service.render(source, finalMessage, displayName), true));
    }
}
