package de.ayont.lpc;

import de.ayont.lpc.commands.LPCCommand;
import de.ayont.lpc.listener.AsyncChatListener;
import org.bukkit.plugin.java.JavaPlugin;


public final class LPC extends JavaPlugin {


    @Override
    public void onEnable() {
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