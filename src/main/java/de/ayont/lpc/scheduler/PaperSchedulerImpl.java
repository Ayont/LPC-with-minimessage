package de.ayont.lpc.scheduler;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper / Folia implementation using the threaded-region schedulers.
 *
 * <p>These schedulers are available in modern Paper builds and are the only scheduler API that
 * works on Folia.
 */
public final class PaperSchedulerImpl implements Scheduler {

    private final JavaPlugin plugin;

    public PaperSchedulerImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(Runnable task) {
        plugin.getServer().getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
    }

    @Override
    public void runAsync(Runnable task) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    @Override
    public void runDelayed(Runnable task, long delayTicks) {
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
    }

    @Override
    public void runOnEntity(Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
    }

    @Override
    public void cancelAll() {
        plugin.getServer().getAsyncScheduler().cancelTasks(plugin);
        plugin.getServer().getGlobalRegionScheduler().cancelTasks(plugin);
    }
}
