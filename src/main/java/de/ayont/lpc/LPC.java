package de.ayont.lpc;

import de.ayont.lpc.commands.LPCCommand;
import de.ayont.lpc.listener.AsyncChatListener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.java.JavaPlugin;


public final class LPC extends JavaPlugin {

    private LuckPerms luckPerms;
    private final MiniMessage miniMessage;

    public LPC() {
        this.miniMessage = MiniMessage.builder().build();
    }

    @Override
    public void onEnable() {
        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        registerCommand();

        saveDefaultConfig();
        registerListeners();
    }

    public void registerCommand() {
        String commandName = "lpc";
        LPCCommand lpcCommand = new LPCCommand(this);

        this.getCommand(commandName).setExecutor(lpcCommand);
        this.getCommand(commandName).setTabCompleter(lpcCommand);
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new AsyncChatListener(this), this);
    }

}