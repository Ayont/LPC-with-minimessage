package de.ayont.lpc.scheduler;

import org.bukkit.entity.Entity;

/**
 * Cross-platform task scheduler abstraction.
 *
 * <p>On Paper and Folia the implementation uses the threaded-region schedulers
 * ({@code GlobalRegionScheduler}, {@code AsyncScheduler}, {@code EntityScheduler}).
 * On plain Spigot/Bukkit it falls back to the legacy {@link org.bukkit.scheduler.BukkitScheduler}.
 */
public interface Scheduler {

    /** Runs a task on the global region / main thread. */
    void run(Runnable task);

    /** Runs a task asynchronously, outside the server tick loop. */
    void runAsync(Runnable task);

    /** Runs a task on the global region / main thread after the specified delay in ticks. */
    void runDelayed(Runnable task, long delayTicks);

    /**
     * Runs a task on the region that owns the given entity.
     *
     * <p>On Folia this follows the entity across regions; on Spigot it simply executes on the
     * main thread.
     */
    void runOnEntity(Entity entity, Runnable task);

    /** Cancels all tasks owned by the plugin. */
    void cancelAll();
}
