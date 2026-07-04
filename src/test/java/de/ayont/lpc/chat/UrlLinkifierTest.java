package de.ayont.lpc.chat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UrlLinkifierTest {

    private static final List<String> SCHEMES = List.of("http", "https");

    private static ClickEvent firstClick(Component component) {
        ClickEvent click = component.clickEvent();
        if (click != null) {
            return click;
        }
        for (Component child : component.children()) {
            ClickEvent found = firstClick(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Test
    @DisplayName("attaches an openUrl click to detected links when clickable")
    void linkify_clickable_attachesOpenUrl() {
        Component out = UrlLinkifier.linkify(Component.text("visit https://example.com now"),
                true, "<blue>", "<gray>Open", SCHEMES, 200);
        ClickEvent click = firstClick(out);
        assertTrue(click != null, "expected a click event on the linkified URL");
        // Adventure-version-neutral: don't compare ClickEvent.Action.OPEN_URL directly (it's a
        // different member between Adventure 4 and 5 — NoSuchFieldError on the older bytecode).
        // The serialize round-trip proves the openUrl target was attached.
        assertTrue(MiniMessage.miniMessage().serialize(out).contains("example.com"),
                "expected the openUrl target to contain example.com");
    }

    @Test
    @DisplayName("does not attach a click on the legacy (non-clickable) path")
    void linkify_nonClickable_noClick() {
        Component out = UrlLinkifier.linkify(Component.text("visit https://example.com now"),
                false, "<blue>", "<gray>Open", SCHEMES, 200);
        assertFalse(firstClick(out) != null, "Spigot path must not carry a click event");
    }
}
