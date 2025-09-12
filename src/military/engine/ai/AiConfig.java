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
}
