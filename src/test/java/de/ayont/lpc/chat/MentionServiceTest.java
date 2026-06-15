package de.ayont.lpc.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MentionServiceTest {

    private static final String FORMAT = "<gold>@<name></gold>";

    @Test
    @DisplayName("highlights a mentioned online name and reports the match")
    void highlight_matchesAndStyles() {
        MentionService.Result result = MentionService.highlight(
                Component.text("hey Notch how are you"), Set.of("Notch"), false, FORMAT, 5);
        assertTrue(result.mentioned().contains("Notch"));
        assertTrue(MiniMessage.miniMessage().serialize(result.message()).contains("gold"));
    }

    @Test
    @DisplayName("does not match names that are not online")
    void highlight_unknownName_noMatch() {
        MentionService.Result result = MentionService.highlight(
                Component.text("hey Steve"), Set.of("Notch"), false, FORMAT, 5);
        assertTrue(result.mentioned().isEmpty());
    }

    @Test
    @DisplayName("respects the require-@ setting")
    void highlight_requireAt() {
        MentionService.Result without = MentionService.highlight(
                Component.text("hey Notch"), Set.of("Notch"), true, FORMAT, 5);
        assertTrue(without.mentioned().isEmpty(), "plain name should not match when @ required");

        MentionService.Result with = MentionService.highlight(
                Component.text("hey @Notch"), Set.of("Notch"), true, FORMAT, 5);
        assertTrue(with.mentioned().contains("Notch"));
    }

    @Test
    @DisplayName("caps the number of highlighted mentions")
    void highlight_maxMentions() {
        MentionService.Result result = MentionService.highlight(
                Component.text("Notch Notch Notch"), Set.of("Notch"), false, FORMAT, 1);
        assertEquals(1, result.mentioned().size());
        assertFalse(result.mentioned().isEmpty());
    }
}
