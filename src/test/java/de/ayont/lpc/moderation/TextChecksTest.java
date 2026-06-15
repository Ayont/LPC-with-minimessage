package de.ayont.lpc.moderation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TextChecksTest {

    @Test
    @DisplayName("uppercaseRatio counts only letters")
    void uppercaseRatio_ignoresNonLetters() {
        assertEquals(1.0, TextChecks.uppercaseRatio("AB!! 12"));
        assertEquals(0.0, TextChecks.uppercaseRatio("12345"));
        assertEquals(0.5, TextChecks.uppercaseRatio("Ab"));
    }

    @Test
    @DisplayName("isShout requires both length and ratio")
    void isShout_lengthAndRatio() {
        assertTrue(TextChecks.isShout("STOP RIGHT THERE", 8, 0.6));
        assertFalse(TextChecks.isShout("HI", 8, 0.6));
        assertFalse(TextChecks.isShout("this is calm enough", 8, 0.6));
    }

    @Test
    @DisplayName("normalize folds leetspeak, case, spacing and repeats")
    void normalize_foldsObfuscation() {
        assertEquals("helo", TextChecks.normalize("Heeello"));
        assertEquals("helo", TextChecks.normalize("h3LL0"));
        assertEquals("youareanob", TextChecks.normalize("you are a n00b!"));
    }

    @Test
    @DisplayName("containsAny matches normalised blocked words")
    void containsAny_matchesNormalised() {
        List<String> words = List.of(TextChecks.normalize("noob"));
        assertTrue(TextChecks.containsAny(TextChecks.normalize("you n0ob"), words));
        assertFalse(TextChecks.containsAny(TextChecks.normalize("you are nice"), words));
    }

    @Test
    @DisplayName("maskWords replaces blocked words case-insensitively")
    void maskWords_replacesLiteral() {
        assertEquals("Hello ****", TextChecks.maskWords("Hello NOOB", List.of("noob"), '*'));
    }

    @Test
    @DisplayName("containsAdvert detects non-allowlisted URLs and respects allowlist")
    void containsAdvert_urlAndAllowlist() {
        assertTrue(TextChecks.containsAdvert("join evil.net now", false, List.of()));
        assertFalse(TextChecks.containsAdvert("see youtube.com/watch", false, List.of("youtube.com")));
        assertFalse(TextChecks.containsAdvert("no links here", false, List.of()));
    }

    @Test
    @DisplayName("containsAdvert optionally detects IPs")
    void containsAdvert_ip() {
        assertTrue(TextChecks.containsAdvert("connect 192.168.0.1:25565", true, List.of()));
        assertFalse(TextChecks.containsAdvert("connect 192.168.0.1", false, List.of()));
    }

    @Test
    @DisplayName("maskAdvert redacts non-allowlisted links only")
    void maskAdvert_redactsLinks() {
        assertEquals("join [link] now", TextChecks.maskAdvert("join evil.net now", false, List.of(), "[link]"));
        assertEquals("watch youtube.com today",
                TextChecks.maskAdvert("watch youtube.com today", false, List.of("youtube.com"), "[link]"));
    }
}
