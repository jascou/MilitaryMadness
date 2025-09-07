package military.engine;

import military.util.ResourceLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight plugin point to register custom unit creators by name without changing core engine code.
 * If no plugin is registered for a given unit name, falls back to loading stats from Resources/Units.txt.
 */
public final class UnitPluginRegistry {
    private UnitPluginRegistry() {}

    /** Functional interface to create Units. */
    public interface UnitCreator {
        Unit create(String name, boolean team);
    }

    private static final Map<String, UnitCreator> REGISTRY = new ConcurrentHashMap<>();

    /** Register or replace a creator for the given unit name (case-sensitive). */
    public static void register(String unitName, UnitCreator creator) {
        if (unitName == null || creator == null) return;
        REGISTRY.put(unitName, creator);
    }

    /** Remove a previously registered unit creator. */
    public static void unregister(String unitName) {
        if (unitName == null) return;
        REGISTRY.remove(unitName);
    }

    /** Clear all registered creators (intended for tests). */
    public static void clear() { REGISTRY.clear(); }

    /**
     * Creates a Unit by name using a registered plugin if present; otherwise falls back to Units.txt.
     * If the name is unknown in Units.txt, returns a minimal default Unit.
     */
    public static Unit create(String name, boolean team) {
        UnitCreator creator = REGISTRY.get(name);
        if (creator != null) {
            try { return creator.create(name, team); } catch (Exception ignored) { /* fallback below */ }
        }
        // Fallback to Units.txt format used by LocationManager
        try (InputStream unitStream = ResourceLoader.openTextFromResources("Units.txt");
             Scanner unitReader = new Scanner(new InputStreamReader(unitStream, StandardCharsets.UTF_8))) {
            while (unitReader.hasNext()) {
                String token = unitReader.next();
                if (name.equals(token)) {
                    String type = unitReader.next();
                    boolean isRange = unitReader.nextBoolean();
                    boolean isAir = unitReader.nextBoolean();
                    int landAttack = unitReader.nextInt();
                    int airAttack = unitReader.nextInt();
                    int range = unitReader.nextInt();
                    int defense = unitReader.nextInt();
                    int shift = unitReader.nextInt();
                    return new Unit(name, type, isRange, isAir, team, landAttack, airAttack, range, defense, shift);
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger log = military.util.Logs.getLogger(UnitPluginRegistry.class);
            log.fine("Units.txt lookup failed for '" + name + "': " + ex.toString());
        }
        // Unknown name -> minimal default
        return new Unit(name, name, false, false, team, 0, 0, 1, 0, 0);
    }
}
