package de.ayont.lpc.chat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerMessagesTest {

    private final MiniMessage parser = PlayerMessages.colorParser(true);

    private static boolean hasInteractiveEvent(Component component) {
        if (component.style().clickEvent() != null || component.style().hoverEvent() != null) {
            return true;
        }
        for (Component child : component.children()) {
            if (hasInteractiveEvent(child)) {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("treats message as literal text when colour is not permitted")
    void render_noPermission_literalText() {
        Component result = PlayerMessages.render(parser, "<red>hi", false);
        assertInstanceOf(TextComponent.class, result);
        assertEquals("<red>hi", ((TextComponent) result).content(), "tags must remain literal");
        assertFalse(hasInteractiveEvent(result));
    }

    @Test
    @DisplayName("applies legacy colour codes when permitted")
    void render_withPermission_appliesColor() {
        Component result = PlayerMessages.render(parser, "&ahi", true);
        String roundTrip = MiniMessage.miniMessage().serialize(result);
        assertTrue(roundTrip.contains("green"), "expected green colour, got: " + roundTrip);
    }

    @Test
    @DisplayName("never produces a click event from a player message")
    void render_clickInjection_blocked() {
        Component result = PlayerMessages.render(parser, "<click:run_command:'/op @s'>x</click>", true);
        assertFalse(hasInteractiveEvent(result), "click injection must be blocked");
    }

    @Test
    @DisplayName("never produces a hover event from a player message")
    void render_hoverInjection_blocked() {
        Component result = PlayerMessages.render(parser, "<hover:show_text:'pwned'>x</hover>", true);
        assertFalse(hasInteractiveEvent(result), "hover injection must be blocked");
    }

    @Test
    @DisplayName("handles null input without throwing")
    void render_nullInput_returnsComponent() {
        Component result = PlayerMessages.render(parser, null, true);
        assertNotNull(result);
    }
}
