package de.ayont.lpc.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ComponentSanitizerTest {

    private static boolean hasInteractive(Component c) {
        if (c.style().clickEvent() != null || c.style().hoverEvent() != null || c.style().insertion() != null) {
            return true;
        }
        for (Component child : c.children()) {
            if (hasInteractive(child)) {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("strips a click event")
    void stripsClick() {
        Component in = MiniMessage.miniMessage().deserialize("<click:run_command:'/op @s'>x</click>");
        Component out = ComponentSanitizer.stripInteractive(in);
        assertFalse(hasInteractive(out), "click must be removed");
    }

    @Test
    @DisplayName("strips a hover event")
    void stripsHover() {
        Component in = MiniMessage.miniMessage().deserialize("<hover:show_text:'pwned'>x</hover>");
        Component out = ComponentSanitizer.stripInteractive(in);
        assertFalse(hasInteractive(out), "hover must be removed");
    }

    @Test
    @DisplayName("strips an insertion")
    void stripsInsertion() {
        Component in = MiniMessage.miniMessage().deserialize("<insertion:evil>x</insertion>");
        Component out = ComponentSanitizer.stripInteractive(in);
        assertFalse(hasInteractive(out), "insertion must be removed");
    }

    @Test
    @DisplayName("recurses into nested children")
    void stripsNested() {
        Component in = MiniMessage.miniMessage().deserialize("<red>outer <click:run_command:'/say hi'>inner</click> tail</red>");
        Component out = ComponentSanitizer.stripInteractive(in);
        assertFalse(hasInteractive(out), "nested interactive events must be removed");
    }

    @Test
    @DisplayName("keeps cosmetic styling (colour, decoration)")
    void keepsCosmetic() {
        Component in = MiniMessage.miniMessage().deserialize("<red><bold>hi</bold></red>");
        Component out = ComponentSanitizer.stripInteractive(in);
        // colour + decoration must survive — only interactive events are stripped
        boolean hasRedOrBold = hasColor(out, NamedTextColor.RED) || hasDecoration(out);
        assertTrue(hasRedOrBold, "cosmetic styling must be preserved");
        assertFalse(hasInteractive(out));
    }

    @Test
    @DisplayName("null input returns an empty component")
    void nullSafe() {
        Component out = ComponentSanitizer.stripInteractive(null);
        assertNotNull(out);
        assertEquals(Component.empty(), out);
    }

    @Test
    @DisplayName("plain text passes through unchanged in content")
    void plainTextUnchanged() {
        Component in = Component.text("just text");
        Component out = ComponentSanitizer.stripInteractive(in);
        assertEquals("just text", plain(out));
    }

    private static boolean hasColor(Component c, NamedTextColor color) {
        if (color.equals(c.color())) {
            return true;
        }
        for (Component child : c.children()) {
            if (hasColor(child, color)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasDecoration(Component c) {
        if (c.style().decorations().values().stream().anyMatch(Boolean.TRUE::equals)) {
            return true;
        }
        for (Component child : c.children()) {
            if (hasDecoration(child)) {
                return true;
            }
        }
        return false;
    }

    private static String plain(Component c) {
        StringBuilder sb = new StringBuilder();
        appendPlain(c, sb);
        return sb.toString();
    }

    private static void appendPlain(Component c, StringBuilder sb) {
        if (c instanceof net.kyori.adventure.text.TextComponent tc) {
            sb.append(tc.content());
        }
        for (Component child : c.children()) {
            appendPlain(child, sb);
        }
    }
}
