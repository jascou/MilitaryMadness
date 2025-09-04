package military.util;

import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Small facade over java.util.prefs to persist simple user preferences
 * such as sound enabled and last selected map. Defaults fall back to
 * FeatureToggles when not set.
 */
public final class PreferencesManager {
    private static final Logger LOGGER = Logs.getLogger(PreferencesManager.class);
    private static final String NODE_PATH = "/military/madness";

    private static final String KEY_SOUND_ENABLED = "sound.enabled";
    private static final String KEY_LAST_MAP = "last.map";

    private static Preferences prefs() {
        return Preferences.userRoot().node(NODE_PATH);
    }

    private PreferencesManager() {}

    public static boolean isSoundEnabled(boolean defaultIfMissing) {
        try {
            return prefs().getBoolean(KEY_SOUND_ENABLED, defaultIfMissing);
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "Preferences read failed for sound.enabled: {0}", ex.toString());
            return defaultIfMissing;
        }
    }

    public static void setSoundEnabled(boolean enabled) {
        try {
            prefs().putBoolean(KEY_SOUND_ENABLED, enabled);
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "Preferences write failed for sound.enabled: {0}", ex.toString());
        }
    }

    public static String getLastMapName() {
        try {
            return prefs().get(KEY_LAST_MAP, null);
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "Preferences read failed for last.map: {0}", ex.toString());
            return null;
        }
    }

    public static void setLastMapName(String mapName) {
        if (mapName == null) return;
        try {
            prefs().put(KEY_LAST_MAP, mapName);
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "Preferences write failed for last.map: {0}", ex.toString());
        }
    }
}
