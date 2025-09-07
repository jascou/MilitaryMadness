package military.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Optional telemetry/log-to-file initializer. Enabled via system property mm.telemetry.enabled=true.
 * Writes logs under ./logs/military.log by default. For tests, use initWithDirectory.
 */
public final class Telemetry {
    private Telemetry() {}

    private static volatile boolean initialized = false;

    public static void initIfEnabled() {
        if (initialized) return;
        if (!FeatureTogglesHelper.getBoolean("mm.telemetry.enabled", false)) return;
        try {
            initWithDirectory(Paths.get("logs"), true);
        } catch (Exception ex) {
            Logger.getLogger(Telemetry.class.getName()).log(Level.FINE, "Telemetry init failed: {0}", ex.toString());
        }
    }

    /**
     * Initialize logging to a file located in dir/military.log.
     * @param dir directory to place the log file in
     * @param append whether to append to an existing file
     */
    public static synchronized void initWithDirectory(Path dir, boolean append) throws IOException {
        if (initialized) return;
        Files.createDirectories(dir);
        Path logFile = dir.resolve("military.log");
        FileHandler handler = new FileHandler(logFile.toString(), append);
        handler.setFormatter(new SimpleFormatter());
        Logger root = Logger.getLogger("");
        root.addHandler(handler);
        initialized = true;
    }

    // Local helper to avoid exposing FeatureToggles beyond boolean read
    private static final class FeatureTogglesHelper {
        static boolean getBoolean(String key, boolean def) {
            try {
                String v = System.getProperty(key);
                if (v == null) return def;
                return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v);
            } catch (SecurityException se) {
                return def;
            }
        }
    }
}
