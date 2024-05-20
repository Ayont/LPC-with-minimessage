package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import de.ayont.lpc.renderer.LPCChatRenderer;
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

        if(!plugin.getConfig().getBoolean("use-item-placeholder", false) || !player.hasPermission("lpc.itemplaceholder")){
            event.renderer(lpcChatRenderer);
            return;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        final Component displayName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ? item.getItemMeta().displayName() : Component.text(item.getType().toString().toLowerCase().replace("_", " "));
        if (item.getType().equals(Material.AIR) || displayName == null) {
            event.renderer(lpcChatRenderer);
            return;
        }

        event.renderer((source, sourceDisplayName, message, viewer) -> lpcChatRenderer.render(source, sourceDisplayName, message, viewer)
                .replaceText(TextReplacementConfig.builder().match(compile("\\[item]", CASE_INSENSITIVE))
                        .replacement(displayName.hoverEvent(item)).build()));
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
