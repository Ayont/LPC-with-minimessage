package de.ayont.lpc.utils;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Utils {

    @Nullable
    public static UUID getUUID(String string) {
        try {
            return UUID.fromString(string);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
