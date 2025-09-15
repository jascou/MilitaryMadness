package military.engine.ai;

import military.engine.Team;

/**
 * Global AI configuration parsed from CLI or set by UI.
 * Minimal holder to enable/disable AI and provide team/seed settings.
 */
public final class AiConfig {
    private static volatile boolean enabled = false;
    private static volatile Team aiTeam = Team.RED; // default if enabled
    private static volatile Long seed = null; // null -> engine picks default RNG
    // Delay in milliseconds between AI actions for UX; 0 for tests/headless
    private static volatile int delayMs = 300;
    // Strategy parameters (phase 5):
    // aggressiveness: how strongly to prefer attacks and closing distance (>=0)
    private static volatile double aggressiveness = 1.0;
    // caution: how much to avoid ending adjacent to enemies (>=0; 0 means fearless)
    private static volatile double caution = 1.0;
    // capturePriority: weight for moving toward enemy bases versus enemy units (>=0)
    private static volatile double capturePriority = 1.0;

    private AiConfig() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static Team getAiTeam() {
        return aiTeam;
    }

    public static void setAiTeam(Team team) {
        if (team != null) {
            aiTeam = team;
        }
    }

    /**
     * Optional deterministic seed for AI decisions. If null, default RNG is used.
     */
    public static Long getSeed() {
        return seed;
    }

    public static void setSeed(Long s) {
        seed = s;
    }

    /**
     * Configurable delay in milliseconds between AI actions for visualization.
     * Set to 0 in tests for deterministic speed.
     */
    public static int getDelayMs() {
        return delayMs;
    }

    public static void setDelayMs(int ms) {
        if (ms < 0) ms = 0;
        delayMs = ms;
    }

    // Strategy params getters/setters
    public static double getAggressiveness() { return aggressiveness; }
    public static void setAggressiveness(double v) { aggressiveness = sanitizeNonNegative(v, 0.0, 10.0); }

    public static double getCaution() { return caution; }
    public static void setCaution(double v) { caution = sanitizeNonNegative(v, 0.0, 10.0); }

    public static double getCapturePriority() { return capturePriority; }
    public static void setCapturePriority(double v) { capturePriority = sanitizeNonNegative(v, 0.0, 10.0); }

    private static double sanitizeNonNegative(double v, double min, double max) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 1.0;
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
