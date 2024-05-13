package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import de.ayont.lpc.renderer.LPCChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;;

public class AsyncChatListener implements Listener {

    private final LPC plugin;
    private final MiniMessage miniMessage;

    public AsyncChatListener(LPC plugin, MiniMessage miniMessage) {
        this.plugin = plugin;
        this.miniMessage = miniMessage;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.renderer(new LPCChatRenderer(plugin));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (plugin.getServer().getOnlinePlayers().size() == event.getRecipients().size()) {
            Player player = event.getPlayer();

            String rawMessage = event.getMessage();

            LPCChatRenderer chatRenderer = new LPCChatRenderer(plugin);

            Component messageComponent = PlainTextComponentSerializer.plainText().deserialize(rawMessage);
            String format = event.getFormat().replace("<", "").replace(">", "");

            Component renderedMessage = chatRenderer.render(player, Component.empty(), messageComponent, player);

            String formattedComponentMessage = LegacyComponentSerializer.legacyAmpersand().serialize(renderedMessage);

            plugin.getChatManager().sendMessage(player.getUniqueId(), "", format, formattedComponentMessage, player);
        }
    }
}
