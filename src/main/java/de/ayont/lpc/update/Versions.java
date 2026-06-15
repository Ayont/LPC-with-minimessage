package de.ayont.lpc.update;

/**
 * Pure (Bukkit-free) helpers for comparing dotted numeric version strings, kept separate from
 * {@link UpdateChecker} so the comparison logic is unit-testable without the server API.
 */
public final class Versions {

    private Versions() {
    }

    /**
     * Compares two dotted numeric version strings. Non-numeric characters are ignored, so values
     * like {@code 4.0.0} and {@code v4.0.0-beta} compare on their numeric components only. Missing
     * trailing components are treated as zero ({@code 4.0} == {@code 4.0.0}).
     *
     * @param latest  the candidate "newer" version
     * @param current the currently installed version
     * @return {@code true} if {@code latest} is strictly newer than {@code current}
     */
    public static boolean isNewer(String latest, String current) {
        int[] a = parse(latest);
        int[] b = parse(current);
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) {
                return x > y;
            }
        }
        return false;
    }

    private static int[] parse(String version) {
        if (version == null || version.isBlank()) {
            return new int[0];
        }
        String[] parts = version.replaceAll("[^0-9.]", "").split("\\.");
        int[] numbers = new int[parts.length];
        int count = 0;
        for (String part : parts) {
            if (!part.isEmpty()) {
                numbers[count++] = Integer.parseInt(part);
            }
        }
        int[] trimmed = new int[count];
        System.arraycopy(numbers, 0, trimmed, 0, count);
        return trimmed;
    }
}
