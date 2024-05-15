package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import de.ayont.lpc.renderer.LPCChatRenderer;
import de.ayont.lpc.utils.Utils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import static java.util.regex.Pattern.*;

public class AsyncChatListener implements Listener {

    private final LPC plugin;
    private final MiniMessage miniMessage;
    private final LPCChatRenderer lpcChatRenderer;

    public AsyncChatListener(LPC plugin, MiniMessage miniMessage) {
        this.plugin = plugin;
        this.miniMessage = miniMessage;
        this.lpcChatRenderer = new LPCChatRenderer(plugin);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        final Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItemInMainHand();
        final Component displayName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ? item.getItemMeta().displayName() : Component.text(item.getType().toString().toLowerCase().replace("_", " "));
        if (item.getType().equals(Material.AIR)) {

            event.renderer((source, sourceDisplayName, message, viewer) -> lpcChatRenderer.render(source, sourceDisplayName, message, viewer));

            return;
        }

        if (Utils.contains(event.message(), "[item]")) {
            item.getType();
        }

        event.renderer(new LPCChatRenderer(plugin));

        event.renderer((source, sourceDisplayName, message, viewer) -> lpcChatRenderer.render(source, sourceDisplayName, message, viewer)
                .replaceText(TextReplacementConfig.builder().match(compile("\\[item]", CASE_INSENSITIVE))
                        .replacement(displayName).build()).hoverEvent(item));
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
