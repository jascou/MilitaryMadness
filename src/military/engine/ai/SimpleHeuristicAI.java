package military.engine.ai;

import military.engine.ImmutableGameState;
import military.engine.Team;
import military.engine.Location;
import military.engine.Base;
import military.util.Rng;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Simple greedy movement heuristic: for the current team, move at most one unit toward the nearest
 * enemy unit or enemy base, then end the turn. Attacking is not handled yet.
 */
public class SimpleHeuristicAI implements AIPlayer {
    @Override
    public List<AiAction> planTurn(ImmutableGameState view, ReadOnlyServices svc, Rng rng) {
        List<AiAction> plan = new ArrayList<>();
        try {
            Team myTeam = view.getTurn() ? Team.BLUE : Team.RED;
            Team enemyTeam = (myTeam == Team.BLUE) ? Team.RED : Team.BLUE;

            // Collect enemy targets (units and bases)
            List<Point> enemyTargets = new ArrayList<>();
            // Find enemy units by scanning locations
            enemyTargets.addAll(findTeamUnitPositions(svc, enemyTeam));
            // Find enemy bases
            enemyTargets.addAll(findTeamBasePositions(enemyTeam));

            // Pick one of my units to move
            List<military.engine.Unit> myUnits = svc.getUnits(myTeam);
            Point bestFrom = null;
            Point bestTo = null;
            int bestDistance = Integer.MAX_VALUE;
            for (military.engine.Unit u : myUnits) {
                Point from = findUnitPosition(svc, u);
                if (from == null) continue;
                List<Point> reachable = svc.getReachable(u);
                if (reachable == null || reachable.isEmpty()) continue;
                // Ensure start is included
                if (!reachable.contains(from)) {
                    reachable = new ArrayList<>(reachable);
                    reachable.add(new Point(from));
                }
                for (Point r : reachable) {
                    int d = distanceToNearest(r, enemyTargets);
                    if (d < bestDistance || (d == bestDistance && rng.nextDouble() < 0.5)) {
                        bestDistance = d;
                        bestFrom = from;
                        bestTo = r;
                    }
                }
            }
            if (bestFrom != null && bestTo != null && !bestFrom.equals(bestTo)) {
                plan.add(new MoveAction(bestFrom, bestTo));
            }
        } catch (Throwable t) {
            // Fallback: ignore and just end turn
        }
        plan.add(new EndTurnAction());
        return plan;
    }

    private static int distance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private static int distanceToNearest(Point p, List<Point> targets) {
        int best = Integer.MAX_VALUE;
        for (Point t : targets) {
            int d = distance(p, t);
            if (d < best) best = d;
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    private static Point findUnitPosition(ReadOnlyServices svc, military.engine.Unit u) {
        // Walk the map via locations
        java.awt.Point size = military.engine.LocationManager.getSize();
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                Location loc = svc.getLocation(new Point(x, y));
                if (loc != null && !loc.isEmpty() && loc.getUnit() == u) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    private static List<Point> findTeamUnitPositions(ReadOnlyServices svc, Team team) {
        List<Point> out = new ArrayList<>();
        java.awt.Point size = military.engine.LocationManager.getSize();
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                Location loc = svc.getLocation(new Point(x, y));
                if (loc != null && !loc.isEmpty() && loc.getUnit().getTeam() == (team == Team.BLUE)) {
                    out.add(new Point(x, y));
                }
            }
        }
        return out;
    }

    private static List<Point> findTeamBasePositions(Team team) {
        List<Point> out = new ArrayList<>();
        java.awt.Point size = military.engine.LocationManager.getSize();
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                Location loc = military.engine.LocationManager.getLoc(new Point(x, y));
                if (loc instanceof Base) {
                    Base b = (Base) loc;
                    if (b.getTeam() == (team == Team.BLUE)) {
                        out.add(new Point(x, y));
                    }
                }
            }
        }
        return out;
    }
}
