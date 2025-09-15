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
 * Simple heuristic: first try to perform one strong attack (prefer kills, else max expected damage)
 * from current positions. If no attack is available, move at most one unit greedily toward the
 * nearest enemy unit or enemy base. Always end the turn after at most one action sequence.
 */
public class SimpleHeuristicAI implements AIPlayer {
    @Override
    public List<AiAction> planTurn(ImmutableGameState view, ReadOnlyServices svc, Rng rng) {
        List<AiAction> plan = new ArrayList<>();
        try {
            Team myTeam = view.getTurn() ? Team.BLUE : Team.RED;
            Team enemyTeam = (myTeam == Team.BLUE) ? Team.RED : Team.BLUE;
            double agg = AiConfig.getAggressiveness();
            double caution = AiConfig.getCaution();
            double cap = AiConfig.getCapturePriority();

            // 1) Try to find a good immediate attack from current positions
            List<military.engine.Unit> myUnits = svc.getUnits(myTeam);
            Point bestAtkFrom = null;
            Point bestAtkTarget = null;
            double bestAtkScore = Double.NEGATIVE_INFINITY;
            for (military.engine.Unit u : myUnits) {
                if (u == null) continue;
                if (u.isAttackDone()) continue; // cannot attack again this turn
                Point from = findUnitPosition(svc, u);
                if (from == null) continue;
                List<Point> targets = svc.getAttackableFrom(u, from);
                if (targets == null || targets.isEmpty()) continue;
                for (Point tgt : targets) {
                    military.engine.Location tLoc = svc.getLocation(tgt);
                    if (tLoc == null || tLoc.isEmpty()) continue;
                    military.engine.Unit defender = tLoc.getUnit();
                    if (defender.getTeam() == u.getTeam()) continue;
                    military.engine.CombatStats stats = svc.previewCombat(u, defender, from, tgt);
                    if (stats == null) continue;
                    int defBefore = defender.getHealth();
                    int defAfter = stats.getDefender().getHealth();
                    int damage = Math.max(0, defBefore - defAfter);
                    int kill = (defAfter <= 0) ? 1 : 0;
                    // Score: prioritize kills heavily, scaled by aggressiveness; modest random tie-breaker
                    double score = agg * (kill * 1000.0 + damage);
                    // Prefer attacking weaker defenders slightly if equal other factors
                    score += (rng.nextDouble() * 0.01);
                    if (score > bestAtkScore) {
                        bestAtkScore = score;
                        bestAtkFrom = from;
                        bestAtkTarget = tgt;
                    }
                }
            }
            if (bestAtkFrom != null && bestAtkTarget != null) {
                plan.add(new AttackAction(bestAtkFrom, bestAtkTarget));
                plan.add(new EndTurnAction());
                return plan; // perform only one primary action per simple heuristic
            }

            // 2) Otherwise, move one unit greedily toward the nearest enemy unit or base (weighted)
            List<Point> enemyUnits = findTeamUnitPositions(svc, enemyTeam);
            List<Point> enemyBases = findTeamBasePositions(enemyTeam);

            Point bestFrom = null;
            Point bestTo = null;
            double bestScore = Double.NEGATIVE_INFINITY;
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
                    // Base desire: get closer to enemies (scaled by aggressiveness)
                    int dEnemy = distanceToNearest(r, enemyUnits);
                    int dBase = enemyBases.isEmpty() ? dEnemy : distanceToNearest(r, enemyBases);
                    double score = 0.0;
                    score += agg * (100 - Math.min(100, dEnemy));
                    score += cap * (100 - Math.min(100, dBase));
                    // Caution penalty: avoid ending adjacent to enemies when caution > 0
                    if (caution > 0 && r != null) {
                        int adjacentEnemies = countAdjacentEnemies(svc, r, myTeam);
                        score -= caution * (adjacentEnemies * 5.0);
                    }
                    // Mild preference to move at all
                    if (!r.equals(from)) score += 0.1;
                    // Random tie-breaker
                    score += rng.nextDouble() * 0.001;
                    if (score > bestScore) {
                        bestScore = score;
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

    private static int countAdjacentEnemies(ReadOnlyServices svc, Point p, Team myTeam) {
        Location loc = svc.getLocation(p);
        if (loc == null) return 0;
        int cnt = 0;
        for (Location adj : loc.getAdjacent()) {
            if (!adj.isEmpty() && adj.getUnit().getTeam() != (myTeam == Team.BLUE)) {
                cnt++;
            }
        }
        return cnt;
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
