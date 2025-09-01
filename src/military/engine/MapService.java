package military.engine;

import java.awt.Point;

/**
 * Minimal shared core API for map operations used by both Game and Designer.
 */
public interface MapService {
    void loadMap(String filename);
    void saveMap(String filename);
    void newLoc(Point p, int type);
    void addUnit(Point p, String name, boolean team);
    Point getSize();
    Location getBase(boolean team);
}
