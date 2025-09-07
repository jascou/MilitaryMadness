package military.engine;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain-specific model for a map: dimensions, tiles, and units.
 * Not used by rendering directly; supports validation and IO.
 */
public class MapModel {
    public final int width;
    public final int height;
    public final int[][] tiles; // type codes (-1..9 mapped as in files)
    public final List<UnitEntry> units;

    public MapModel(int width, int height, int[][] tiles, List<UnitEntry> units) {
        this.width = width;
        this.height = height;
        this.tiles = tiles;
        this.units = Collections.unmodifiableList(new ArrayList<>(units));
    }

    public boolean isValid() {
        if (width <= 0 || height <= 0) return false;
        if (tiles == null || tiles.length != width) return false;
        for (int x = 0; x < width; x++) {
            if (tiles[x] == null || tiles[x].length != height) return false;
            for (int y = 0; y < height; y++) {
                int t = tiles[x][y];
                if (!(t == -1 || (t >= 0 && t <= 9))) return false;
            }
        }
        for (UnitEntry ue : units) {
            if (ue == null) return false;
            if (ue.x < 0 || ue.y < 0 || ue.x >= width || ue.y >= height) return false;
        }
        return true;
    }

    public static class UnitEntry {
        public final int x;
        public final int y;
        public final String name;
        public final boolean team;
        public UnitEntry(int x, int y, String name, boolean team) {
            this.x = x; this.y = y; this.name = name; this.team = team;
        }
        public Point toPoint() { return new Point(x, y); }
    }
}
