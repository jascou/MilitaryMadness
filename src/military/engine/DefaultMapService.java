package military.engine;

import java.awt.Point;

/**
 * Default implementation delegating to LocationManager; provides a seam to separate Designer from runtime engine.
 * Feature toggles may influence behavior (e.g., versioned map save), but defaults preserve legacy flows.
 */
public class DefaultMapService implements MapService {
    @Override
    public void loadMap(String filename) { LocationManager.loadMap(filename); }

    @Override
    public void saveMap(String filename) {
        // Toggle allows opting-in to versioned header saves without breaking legacy by default
        if (military.util.FeatureToggles.mapsVersionedSaveEnabled()) {
            LocationManager.saveMapV1(filename);
        } else {
            LocationManager.saveMap(filename);
        }
    }

    @Override
    public void newLoc(Point p, int type) { LocationManager.newLoc(p, type); }

    @Override
    public void addUnit(Point p, String name, boolean team) { LocationManager.addUnit(p, name, team); }

    @Override
    public Point getSize() { return LocationManager.getSize(); }

    @Override
    public Location getBase(boolean team) { return LocationManager.getBase(team); }
}
