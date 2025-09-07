package military.engine;

import java.awt.Point;

/**
 * Minimal shared core API for map operations used by both Game and Designer.
 * Implementations should be thin adapters over the underlying map management (e.g., LocationManager).
 */
public interface MapService {
    /**
     * Load a map by name (without extension) from the configured Maps directory.
     * Implementations may validate schema and throw unchecked exceptions on failure.
     */
    void loadMap(String filename);

    /**
     * Save the current map under the given name (without extension). Implementations may honor
     * feature toggles to choose legacy vs. versioned formats; callers should not rely on a specific wire format.
     */
    void saveMap(String filename);

    /**
     * Change the terrain/structure type at a given coordinate.
     * @param p grid coordinate in tile units
     * @param type terrain code (legacy codes: -1 empty, 0..4 terrain bands, 5/6 bases, 7..9 factories)
     */
    void newLoc(Point p, int type);

    /**
     * Add a unit by name and team at the given coordinate.
     */
    void addUnit(Point p, String name, boolean team);

    /**
     * @return current map size (width,height) in tiles.
     */
    Point getSize();

    /**
     * @param team true for blue, false for red
     * @return the base Location for the given team (may be null if not placed)
     */
    Location getBase(boolean team);
}
