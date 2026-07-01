package de.ayont.lpc.chat;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;

/**
 * Defence-in-depth helper that recursively strips <em>interactive</em> styling from a component tree:
 * {@code clickEvent}, {@code hoverEvent} and {@code insertion}.
 *
 * <p>Player-facing text is already parsed with a restricted (cosmetic-only) MiniMessage instance, so
 * a player can never legitimately produce these events in their own message. This pass guarantees
 * that invariant at the component level too — so even if a future code path, a renamed item (anvil),
 * or a third-party plugin hands LPC a component that already carries an interactive event, it can
 * never reach another player's chat as a clickable / hoverable / shift-insertable element. Cosmetic
 * styling (colours, decorations) is preserved.
 *
 * <p>Always-on by design — there is no config toggle, because this is a security boundary.
 */
public final class ComponentSanitizer {

    private ComponentSanitizer() {
    }

    /**
     * Returns a copy of the component tree with every interactive event removed. Cosmetic styling
     * (colour, decoration, font, etc.) is kept untouched. A {@code null} input returns
     * {@link Component#empty()}.
     *
     * @param component the component to harden (may be {@code null})
     * @return a component guaranteed to carry no click / hover / insertion events
     */
    public static Component stripInteractive(Component component) {
        if (component == null) {
            return Component.empty();
        }
        Component stripped = component.clickEvent(null).hoverEvent(null).insertion(null);
        List<Component> children = stripped.children();
        if (children.isEmpty()) {
            return stripped;
        }
        List<Component> sanitized = new ArrayList<>(children.size());
        for (Component child : children) {
            sanitized.add(stripInteractive(child));
        }
        return stripped.children(sanitized);
    }
}
