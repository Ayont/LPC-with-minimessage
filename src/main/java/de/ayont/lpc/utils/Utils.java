package de.ayont.lpc.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Utils {

    public static String get(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static boolean contains(Component component, String s) {
        return get(component).toLowerCase().contains(s);
    }
}
