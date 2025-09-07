package military.util;

/**
 * Feature toggles (config-driven via system properties) to de-risk refactors by allowing
 * opting-in to new behaviors while keeping legacy flows as defaults.
 *
 * System properties (all default to false unless noted):
 * - mm.maps.versionedSave=true|false : use versioned map save format (MMAPv1) in MapService.saveMap (default: false)
 * - mm.sound.enabled=true|false : enable sound playback by default (default: true)
 */
public final class FeatureToggles {
    private FeatureToggles() {}

    private static boolean getBoolean(String key, boolean def) {
        try {
            String v = System.getProperty(key);
            if (v == null) return def;
            return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v);
        } catch (SecurityException se) {
            return def;
        }
    }

    /**
     * If true, DefaultMapService.saveMap will write MMAPv1 headered files.
     * Defaults to false to preserve legacy round-trip expectations in tests.
     */
    public static boolean mapsVersionedSaveEnabled() {
        return getBoolean("mm.maps.versionedSave", false);
    }

    /**
     * If false, SoundUtility/SoundPlayer implementations may start with sounds disabled.
     * Defaults to true to preserve current behavior.
     */
    public static boolean soundEnabledByDefault() {
        return getBoolean("mm.sound.enabled", true);
    }
}
