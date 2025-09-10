package military.engine;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Movement/pathfinding utilities (BFS-based) extracted from Game.
 */
public final class PathfindingService {
    private PathfindingService() {}

    /**
     * Computes reachable points using remaining move points from the unit's shift stat and terrain costs.
     * Returns a list of Points including the starting position.
     */
    public static List<Point> computeMovesBfs(Point start, Unit unit, boolean turn) {
        int width = LocationManager.getSize().x;
        int height = LocationManager.getSize().y;
        int[][] movesLeft = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                movesLeft[i][j] = -1;
            }
        }
        int remaining = unit.getShift();
        try {
            // Subtract movement points already spent this turn (e.g., first move before attack)
            remaining -= unit.getMovePointsSpentThisTurn();
        } catch (Throwable t) {
            // Older units may not track points; ignore
        }
        if (remaining < 0) remaining = 0;
        movesLeft[start.x][start.y] = remaining;
        ArrayDeque<Point> queue = new ArrayDeque<>();
        queue.add(new Point(start));
        while (!queue.isEmpty()) {
            Point p = queue.pollFirst();
            Location[] adj = LocationManager.getLoc(p).getAdjacent();
            for (Location loc : adj) {
                if (loc.getTerrain() == -1 || (!loc.isEmpty() && loc.getUnit().getTeam() != turn)) {
                    continue;
                }
                int terrain = loc.getTerrain() / 10;
                if (terrain == 0 || unit.isAir()) {
                    terrain = 1;
                }
                int newMovesLeft = movesLeft[p.x][p.y] - terrain;
                boolean flanked = false;
                for (Location flank : loc.getAdjacent()) {
                    if (!flank.isEmpty() && flank.getUnit().getTeam() != turn) {
                        flanked = true;
                    }
                }
                int lx = loc.getLoc().x;
                int ly = loc.getLoc().y;
                if (newMovesLeft > movesLeft[lx][ly]
                        && (unit.getType().equals("Infantry") || (!(loc instanceof Base) && terrain != 4))) {
                    if (!((loc instanceof Factory) && ((Factory) loc).getTeam() != unit.getTeam() && !unit.getType().equals("Infantry"))) {
                        movesLeft[lx][ly] = newMovesLeft;
                        if (!flanked) {
                            queue.add(new Point(lx, ly));
                        }
                    }
                }
            }
            if (!LocationManager.getLoc(p).isEmpty()) {
                movesLeft[p.x][p.y] = -1;
            }
        }
        ArrayList<Point> result = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (movesLeft[i][j] > -1) {
                    result.add(new Point(i, j));
                }
            }
        }
        return result;
    }
}
