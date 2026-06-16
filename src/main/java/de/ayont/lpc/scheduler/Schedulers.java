package de.ayont.lpc.scheduler;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Factory for creating a {@link Scheduler} appropriate for the current server platform.
 */
public final class Schedulers {

    private Schedulers() {
    }

    /**
     * Creates the best available scheduler implementation.
     *
     * <p>Detects Folia/Paper by looking for {@code io.papermc.paper.threadedregions.scheduler.AsyncScheduler}.
     * On Spigot this class is absent, so the legacy {@link BukkitSchedulerImpl} is used.
     */
    public static Scheduler create(JavaPlugin plugin) {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            return new PaperSchedulerImpl(plugin);
        } catch (ClassNotFoundException ignored) {
            return new BukkitSchedulerImpl(plugin);
        }
    }
}
