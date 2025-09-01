package military.engine;

import java.awt.Point;

/**
 * Default implementation delegating to LocationManager; provides a seam to separate Designer from runtime engine.
 */
public class DefaultMapService implements MapService {
    @Override
    public void loadMap(String filename) { LocationManager.loadMap(filename); }

    @Override
    public void saveMap(String filename) { LocationManager.saveMap(filename); }

    @Override
    public void newLoc(Point p, int type) { LocationManager.newLoc(p, type); }

    @Override
    public void addUnit(Point p, String name, boolean team) { LocationManager.addUnit(p, name, team); }

    @Override
    public Point getSize() { return LocationManager.getSize(); }

    @Override
    public Location getBase(boolean team) { return LocationManager.getBase(team); }
}
