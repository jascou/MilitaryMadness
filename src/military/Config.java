package military;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Centralized configuration for filesystem locations and path resolution.
 * Uses java.nio.file.Path to stay OS-agnostic.
 */
public final class Config {
    private Config() {}

    public static Path mapsDir() {
        return Paths.get("Maps");
    }

    public static Path resourcesDir() {
        return Paths.get("Resources");
    }

    public static Path soundsDir() {
        return Paths.get("sounds");
    }
}
