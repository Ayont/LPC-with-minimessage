package de.ayont.lpc.chat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmojiReplacerTest {

    @Test
    @DisplayName("replaces configured shortcuts with their values")
    void replace_substitutesShortcuts() {
        Component out = EmojiReplacer.replace(Component.text("hello :heart:"), Map.of(":heart:", "❤"));
        String serialized = MiniMessage.miniMessage().serialize(out);
        assertTrue(serialized.contains("❤"), "emoji should be inserted: " + serialized);
        assertFalse(serialized.contains(":heart:"), "shortcut should be gone: " + serialized);
    }

    @Test
    @DisplayName("leaves text without shortcuts unchanged")
    void replace_noShortcut_unchanged() {
        Component out = EmojiReplacer.replace(Component.text("plain text"), Map.of(":heart:", "❤"));
        assertTrue(MiniMessage.miniMessage().serialize(out).contains("plain text"));
    }
}
