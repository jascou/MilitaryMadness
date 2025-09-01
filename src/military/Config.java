package military;

import java.nio.file.Path;

/**
 * Centralized configuration for filesystem locations and path resolution.
 * Uses java.nio.file.Path to stay OS-agnostic.
 */
public final class Config {
    private Config() {}

    public static Path mapsDir() {
        return Path.of("Maps");
    }

    public static Path resourcesDir() {
        return Path.of("Resources");
    }

    public static Path soundsDir() {
        return Path.of("sounds");
    }
}
