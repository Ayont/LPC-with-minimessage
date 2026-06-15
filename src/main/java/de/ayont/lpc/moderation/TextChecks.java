package de.ayont.lpc.moderation;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure (Bukkit-free) text analysis used by {@link ModerationService}. Every method operates on raw
 * plaintext only and has no side effects, so the moderation rules are fully unit-testable and can
 * run safely on the async chat thread. All patterns here are constant (never built from player
 * input) to avoid ReDoS.
 */
public final class TextChecks {

    private static final Pattern URL = Pattern.compile(
            "(?i)(?:https?://)?(?:[a-z0-9-]+\\.)+[a-z]{2,}(?:/\\S*)?");
    private static final Pattern IP = Pattern.compile(
            "(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d{1,5})?");

    private TextChecks() {
    }

    /** @return the fraction (0..1) of letters that are uppercase; 0 when there are no letters. */
    public static double uppercaseRatio(String text) {
        int letters = 0;
        int upper = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) {
                    upper++;
                }
            }
        }
        return letters == 0 ? 0.0 : (double) upper / letters;
    }

    /** @return true if the text is long enough and its uppercase ratio exceeds the threshold. */
    public static boolean isShout(String text, int minLength, double maxRatio) {
        return text.length() >= minLength && uppercaseRatio(text) > maxRatio;
    }

    /**
     * Normalises text for fuzzy matching: lowercases, maps common leetspeak to letters, strips
     * non-alphanumerics, and collapses runs of the same character. Used for both profanity detection
     * and repeat detection so {@code "F U  C K!!"} and {@code "fuuuck"} normalise alike.
     */
    public static String normalize(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        char previous = 0;
        for (int i = 0; i < lower.length(); i++) {
            char mapped = switch (lower.charAt(i)) {
                case '0' -> 'o';
                case '1' -> 'i';
                case '3' -> 'e';
                case '4', '@' -> 'a';
                case '5', '$' -> 's';
                case '7' -> 't';
                default -> lower.charAt(i);
            };
            if (Character.isLetterOrDigit(mapped) && mapped != previous) {
                sb.append(mapped);
                previous = mapped;
            } else if (!Character.isLetterOrDigit(mapped)) {
                previous = 0;
            }
        }
        return sb.toString();
    }

    /** @return true if the normalised message contains any of the (already-normalised) words. */
    public static boolean containsAny(String normalizedMessage, Collection<String> normalizedWords) {
        for (String word : normalizedWords) {
            if (!word.isEmpty() && normalizedMessage.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Masks each occurrence of a blocked word in the original text with {@code maskChar} repeated to
     * the word's length. Matching is case-insensitive on the literal word.
     */
    public static String maskWords(String original, Collection<String> words, char maskChar) {
        String result = original;
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            String mask = String.valueOf(maskChar).repeat(word.length());
            result = Pattern.compile(Pattern.quote(word), Pattern.CASE_INSENSITIVE)
                    .matcher(result)
                    .replaceAll(Matcher.quoteReplacement(mask));
        }
        return result;
    }

    /** @return true if the text contains a non-allowlisted URL (or IP, when {@code detectIp}). */
    public static boolean containsAdvert(String text, boolean detectIp, Collection<String> allowlist) {
        Matcher urlMatcher = URL.matcher(text);
        while (urlMatcher.find()) {
            if (!isAllowlisted(urlMatcher.group(), allowlist)) {
                return true;
            }
        }
        if (detectIp) {
            Matcher ipMatcher = IP.matcher(text);
            while (ipMatcher.find()) {
                if (!isAllowlisted(ipMatcher.group(), allowlist)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Replaces every non-allowlisted URL/IP with {@code replacement}. */
    public static String maskAdvert(String text, boolean detectIp, Collection<String> allowlist, String replacement) {
        String result = replaceMatches(URL.matcher(text), allowlist, replacement);
        if (detectIp) {
            result = replaceMatches(IP.matcher(result), allowlist, replacement);
        }
        return result;
    }

    private static String replaceMatches(Matcher matcher, Collection<String> allowlist, String replacement) {
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String match = matcher.group();
            matcher.appendReplacement(sb, isAllowlisted(match, allowlist)
                    ? Matcher.quoteReplacement(match)
                    : Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static boolean isAllowlisted(String match, Collection<String> allowlist) {
        String lower = match.toLowerCase(Locale.ROOT);
        for (String allowed : allowlist) {
            if (!allowed.isEmpty() && lower.contains(allowed.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
