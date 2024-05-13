package de.ayont.lpc.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ExternalChatReceiveEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID uuid;
    private final String source;
    private String name;
    private String format;
    private String message;
    private boolean cancel;

    public ExternalChatReceiveEvent(UUID uuid, String name, String format, String message, String source) {
        super(true);

        this.uuid = uuid;
        this.name = name;
        this.format = format;
        this.message = message;
        this.source = source;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Nullable
    public UUID getUUID() {
        return uuid;
    }

    @Nullable
    public Player getPlayer() {
        return uuid != null ? Bukkit.getPlayer(uuid) : null;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getFormat() {
        return format;
    }

    public void setFormat(@NotNull String format) {
        this.format = format;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public void setMessage(@NotNull String message) {
        this.message = message;
    }

    @Nullable
    public String getSource() {
        return source;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
