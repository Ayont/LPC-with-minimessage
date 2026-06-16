package de.ayont.lpc.scheduler;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Spigot/Bukkit fallback implementation using the legacy {@link org.bukkit.scheduler.BukkitScheduler}.
 */
public final class BukkitSchedulerImpl implements Scheduler {

    private final JavaPlugin plugin;

    public BukkitSchedulerImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public void runAsync(Runnable task) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runDelayed(Runnable task, long delayTicks) {
        plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void runOnEntity(Entity entity, Runnable task) {
        // Spigot has no entity scheduler; the main thread is the only safe choice.
        run(task);
    }

    @Override
    public void cancelAll() {
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }
}
