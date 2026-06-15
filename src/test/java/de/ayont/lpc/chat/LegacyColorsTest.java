package de.ayont.lpc.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LegacyColorsTest {

    @Test
    @DisplayName("converts ampersand color code to MiniMessage tag")
    void toMiniMessage_ampersandColor_converts() {
        assertEquals("<green>Hello", LegacyColors.toMiniMessage("&aHello"));
    }

    @Test
    @DisplayName("normalizes section sign to ampersand before converting")
    void toMiniMessage_sectionSign_converts() {
        assertEquals("<red>Red", LegacyColors.toMiniMessage("§cRed"));
    }

    @Test
    @DisplayName("converts chained format codes")
    void toMiniMessage_chainedCodes_converts() {
        assertEquals("<bold><underlined>Bold", LegacyColors.toMiniMessage("&l&nBold"));
    }

    @Test
    @DisplayName("returns null unchanged")
    void toMiniMessage_null_returnsNull() {
        assertNull(LegacyColors.toMiniMessage(null));
    }

    @Test
    @DisplayName("returns empty string unchanged")
    void toMiniMessage_empty_returnsEmpty() {
        assertEquals("", LegacyColors.toMiniMessage(""));
    }

    @Test
    @DisplayName("leaves plain text untouched")
    void toMiniMessage_plainText_unchanged() {
        assertEquals("no codes here", LegacyColors.toMiniMessage("no codes here"));
    }

    @Test
    @DisplayName("converts uppercase legacy codes")
    void toMiniMessage_uppercaseCodes_converts() {
        assertEquals("<green><bold>Hi", LegacyColors.toMiniMessage("&A&LHi"));
    }
}
