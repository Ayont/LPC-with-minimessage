package de.ayont.lpc.chat;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Single source of truth for the legacy color/format code ({@code &a}, {@code §a}) to
 * MiniMessage tag mapping. Previously this table was copy-pasted across three classes.
 */
public final class LegacyColors {

    /** Immutable, ordered legacy code -> MiniMessage tag mapping (lowercase codes). */
    public static final Map<String, String> LEGACY_TO_MINIMESSAGE;

    /** Matches a legacy code character (any case) following an ampersand. */
    private static final Pattern LEGACY_CODE = Pattern.compile("&([0-9A-Fa-fK-ORk-or])");

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("&0", "<black>");
        map.put("&1", "<dark_blue>");
        map.put("&2", "<dark_green>");
        map.put("&3", "<dark_aqua>");
        map.put("&4", "<dark_red>");
        map.put("&5", "<dark_purple>");
        map.put("&6", "<gold>");
        map.put("&7", "<gray>");
        map.put("&8", "<dark_gray>");
        map.put("&9", "<blue>");
        map.put("&a", "<green>");
        map.put("&b", "<aqua>");
        map.put("&c", "<red>");
        map.put("&d", "<light_purple>");
        map.put("&e", "<yellow>");
        map.put("&f", "<white>");
        map.put("&l", "<bold>");
        map.put("&o", "<italic>");
        map.put("&n", "<underlined>");
        map.put("&m", "<strikethrough>");
        map.put("&k", "<obfuscated>");
        map.put("&r", "<reset>");
        LEGACY_TO_MINIMESSAGE = Map.copyOf(map);
    }

    private LegacyColors() {
    }

    /**
     * Converts legacy color/format codes (using either {@code &} or {@code §}, any case) within the
     * given text to their MiniMessage tag equivalents. Returns the input unchanged when {@code null}
     * or empty.
     *
     * @param input raw text potentially containing legacy codes
     * @return text with legacy codes rewritten as MiniMessage tags
     */
    public static String toMiniMessage(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String out = input.replace('§', '&');
        // Normalise the case of code characters (e.g. &A -> &a) so uppercase codes are also converted.
        out = LEGACY_CODE.matcher(out).replaceAll(match -> "&" + match.group(1).toLowerCase(Locale.ROOT));
        for (Map.Entry<String, String> entry : LEGACY_TO_MINIMESSAGE.entrySet()) {
            out = out.replace(entry.getKey(), entry.getValue());
        }
        return out;
    }
}
