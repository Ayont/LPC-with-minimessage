package de.ayont.lpc.moderation;

import net.kyori.adventure.text.Component;

/**
 * Outcome of moderating a single chat message.
 * <ul>
 *   <li>{@code ALLOW}  — let the message through unchanged.</li>
 *   <li>{@code BLOCK}  — cancel the message; {@link #notice()} is shown to the sender.</li>
 *   <li>{@code TRANSFORM} — continue with {@link #text()} substituted for the raw message.</li>
 * </ul>
 */
public final class ModResult {

    public enum Action { ALLOW, BLOCK, TRANSFORM }

    private static final ModResult ALLOW = new ModResult(Action.ALLOW, null, null);

    private final Action action;
    private final String text;
    private final Component notice;

    private ModResult(Action action, String text, Component notice) {
        this.action = action;
        this.text = text;
        this.notice = notice;
    }

    public static ModResult allow() {
        return ALLOW;
    }

    public static ModResult block(Component notice) {
        return new ModResult(Action.BLOCK, null, notice);
    }

    public static ModResult transform(String text) {
        return new ModResult(Action.TRANSFORM, text, null);
    }

    public Action action() {
        return action;
    }

    public boolean isBlocked() {
        return action == Action.BLOCK;
    }

    /** The cleaned message for {@code TRANSFORM}, otherwise {@code null}. */
    public String text() {
        return text;
    }

    /** The notice to show the sender for {@code BLOCK}, otherwise {@code null}. */
    public Component notice() {
        return notice;
    }
}
