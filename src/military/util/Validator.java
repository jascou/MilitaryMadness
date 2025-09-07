package military.util;

import java.util.OptionalInt;

/**
 * Simple input validation helpers.
 */
public final class Validator {
    private Validator() {}

    public static OptionalInt parsePositiveIntWithin(String s, int minInclusive, int maxInclusive) {
        if (s == null) return OptionalInt.empty();
        try {
            int v = Integer.parseInt(s.trim());
            if (v < minInclusive || v > maxInclusive) return OptionalInt.empty();
            return OptionalInt.of(v);
        } catch (NumberFormatException ex) {
            return OptionalInt.empty();
        }
    }

    public static boolean isValidMapName(String s) {
        if (s == null) return false;
        String t = s.trim();
        if (t.isEmpty()) return false;
        return t.matches("[A-Za-z0-9_-]{1,64}");
    }
}
