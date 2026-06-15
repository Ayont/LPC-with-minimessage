package de.ayont.lpc.update;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VersionsTest {

    @Test
    @DisplayName("detects a newer major version")
    void isNewer_higherMajor_true() {
        assertTrue(Versions.isNewer("4.0.0", "3.6.9"));
    }

    @Test
    @DisplayName("detects a newer patch version")
    void isNewer_higherPatch_true() {
        assertTrue(Versions.isNewer("4.0.1", "4.0.0"));
    }

    @Test
    @DisplayName("treats equal versions as not newer")
    void isNewer_equal_false() {
        assertFalse(Versions.isNewer("4.0.0", "4.0.0"));
    }

    @Test
    @DisplayName("treats an older version as not newer")
    void isNewer_older_false() {
        assertFalse(Versions.isNewer("3.6.9", "4.0.0"));
    }

    @Test
    @DisplayName("ignores non-numeric prefixes and suffixes")
    void isNewer_nonNumeric_comparesNumericParts() {
        assertFalse(Versions.isNewer("v4.0.0-beta", "4.0.0"));
    }

    @Test
    @DisplayName("treats missing trailing components as zero")
    void isNewer_differingLength_padsWithZero() {
        assertFalse(Versions.isNewer("4.0.0", "4.0"));
        assertTrue(Versions.isNewer("4.0.1", "4.0"));
    }

    @Test
    @DisplayName("handles null and blank versions without throwing")
    void isNewer_nullOrBlank_safe() {
        assertFalse(Versions.isNewer(null, "4.0.0"));
        assertTrue(Versions.isNewer("4.0.0", null));
        assertFalse(Versions.isNewer("", ""));
    }
}
