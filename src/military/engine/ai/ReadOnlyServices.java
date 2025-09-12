package military.engine.ai;

import military.engine.Location;
import military.engine.Unit;
import military.engine.Team;
import military.engine.CombatStats;

import java.awt.Point;
import java.util.List;

/**
 * Read-only façade over engine services for AI planning. Implementations must not
 * mutate engine state and should be safe to call from non-EDT threads.
 */
public interface ReadOnlyServices {
    /** @return list of units belonging to the given team (snapshot view). */
    List<Unit> getUnits(Team team);

    /** @return true if the given map coordinate is occupied by any unit. */
    boolean isOccupied(Point p);

    /** @return immutable copy or read-only view of the Location at p; may return null if out of bounds. */
    Location getLocation(Point p);

    /**
     * Compute reachable tiles for the given unit from its current position considering movement rules.
     * Returned points are map coordinates (axial/offset per engine convention).
     */
    List<Point> getReachable(Unit u);

    /**
     * Compute attackable target tiles for a unit if it were standing at 'from'. This lets the AI evaluate
     * attacks both pre- and post-move.
     */
    List<Point> getAttackableFrom(Unit u, Point from);

    /**
     * Preview the outcome of attacking 'defender' from attackerPos to defenderPos without mutating state.
     */
    CombatStats previewCombat(Unit attacker, Unit defender, Point attackerPos, Point defenderPos);
}
