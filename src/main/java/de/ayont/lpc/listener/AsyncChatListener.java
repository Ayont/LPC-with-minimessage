package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import de.ayont.lpc.renderer.LPCChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;;

public class AsyncChatListener implements Listener {

    private final LPC plugin;

    public AsyncChatListener(LPC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.renderer(new LPCChatRenderer(plugin));
    }
}
