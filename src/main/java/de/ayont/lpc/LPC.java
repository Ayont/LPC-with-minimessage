package de.ayont.lpc;

import de.ayont.lpc.commands.LPCCommand;
import de.ayont.lpc.commands.ProxyChatBridgeBukkitBroadcastCommand;
import de.ayont.lpc.listener.AsyncChatListener;
import de.ayont.lpc.listener.PluginMessageListener;
import de.ayont.lpc.manager.ChatManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public final class LPC extends JavaPlugin {

    private LuckPerms luckPerms;
    private final MiniMessage miniMessage;
    private ChatManager chatManager;

    public LPC() {
        this.miniMessage = MiniMessage.builder().build();
    }

    @Override
    public void onEnable() {
        chatManager = new ChatManager(this);
        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        registerCommand();
        saveDefaultConfig();
        registerListeners();
        registerChannels();
    }

    @Override
    public void onDisable() {
        unregisterListeners();
        unregisterChannels();
    }

    public void registerCommand() {
        String commandName = "lpc";
        LPCCommand lpcCommand = new LPCCommand(this);

        this.getCommand(commandName).setExecutor(lpcCommand);
        this.getCommand(commandName).setTabCompleter(lpcCommand);
        PluginCommand proxyChatBridgeBroadcastPluginCommand = getCommand("proxychatbridgebukkitbroadcast");
        if (proxyChatBridgeBroadcastPluginCommand != null) {
            proxyChatBridgeBroadcastPluginCommand.setExecutor(new ProxyChatBridgeBukkitBroadcastCommand(this));
        }
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new AsyncChatListener(this, miniMessage), this);
    }

    private void registerChannels() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessageListener(this));
    }

    private void unregisterChannels() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    private void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }


    public ChatManager getChatManager() {
        return chatManager;
    }

}