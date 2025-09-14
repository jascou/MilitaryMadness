package military.engine.ai;

import military.Game;
import military.engine.ImmutableGameState;
import military.engine.Team;
import military.util.Rng;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Executes AIPlayer plans against the Game. Phase 1 stub that supports EndTurnAction only.
 */
public class AIController {
    private static final Logger LOG = military.util.Logs.getLogger(AIController.class);

    private final AIPlayer ai;

    public AIController(AIPlayer ai) {
        this.ai = ai;
    }

    /**
     * Plan and execute the AI turn for the given team. Phase 1: only EndTurnAction is executed.
     * This method is synchronous and expected to be called off the EDT.
     */
    public void takeTurn(Game game, Team aiTeam, Rng rng) {
        // Delegate to adapter-based method to allow headless tests
        takeTurn(wrap(game), aiTeam, rng);
    }

    /**
     * Adapter-based entry point used by tests and production wrapper.
     */
    public void takeTurn(GameAdapter adapter, Team aiTeam, Rng rng) {
        // Build a minimal snapshot view for planning
        ImmutableGameState view = buildSnapshot(adapter);
        ReadOnlyServices svc = new EngineServices();
        List<AiAction> plan;
        try {
            plan = ai.planTurn(view, svc, rng);
        } catch (Throwable t) {
            LOG.warning("AI planning failed: " + t.getMessage());
            plan = java.util.Collections.<AiAction>singletonList(new EndTurnAction());
        }
        execute(adapter, plan);
    }

    private void execute(GameAdapter adapter, List<AiAction> actions) {
        if (actions == null) return;
        for (AiAction a : actions) {
            if (a instanceof EndTurnAction) {
                LOG.fine("Executing EndTurnAction");
                adapter.endTurn();
                return; // End turn ends execution for phase 1
            } else if (a instanceof MoveAction || a instanceof AttackAction || a instanceof SelectAction || a instanceof WaitAction) {
                // Not implemented in phase 1
                LOG.fine("Ignoring action (not yet implemented in phase 1): " + a);
            } else {
                LOG.fine("Unknown action: " + a);
            }
        }
        // Safety: ensure turn ends if plan forgot to include it
        LOG.fine("Plan did not include EndTurnAction; ending turn by default.");
        adapter.endTurn();
    }

    private ImmutableGameState buildSnapshot(GameAdapter adapter) {
        // Reuse data from adapter for cursor/select
        java.util.ArrayList<java.awt.Point> select = adapter.getSelectLocs();
        Point cursor = adapter.getRenderCursor();
        // Query counts via UnitRepository adapter used by GUI/GameController
        military.engine.UnitRepository repo = military.engine.DefaultUnitRepository.getInstance();
        int blue = repo.getUnits(military.engine.Team.BLUE).size();
        int red = repo.getUnits(military.engine.Team.RED).size();
        return new ImmutableGameState(adapter.getTurnFlag(), cursor == null ? new Point(0,0) : cursor,
                select == null ? new ArrayList<Point>() : select,
                blue, red);
    }

    private static GameAdapter wrap(Game game) {
        return new GameAdapter() {
            @Override public boolean getTurnFlag() { return game.getTurn(); }
            @Override public ArrayList<Point> getSelectLocs() { return game.getSelectLocs(); }
            @Override public Point getRenderCursor() { return game.getRenderCursor(); }
            @Override public void endTurn() { game.endTurnForAutomation(); }
        };
    }

    /**
     * Engine-backed read-only services used for AI planning.
     */
    private static final class EngineServices implements ReadOnlyServices {
        @Override public java.util.List<military.engine.Unit> getUnits(Team team) {
            military.engine.UnitRepository repo = military.engine.DefaultUnitRepository.getInstance();
            return new java.util.ArrayList<>(repo.getUnits(team));
        }
        @Override public boolean isOccupied(Point p) {
            try { return !military.engine.LocationManager.getLoc(p).isEmpty(); } catch (Throwable t) { return false; }
        }
        @Override public military.engine.Location getLocation(Point p) { return military.engine.LocationManager.getLoc(p); }
        @Override public java.util.List<Point> getReachable(military.engine.Unit u) {
            Point pos = findUnit(u);
            if (pos == null) return java.util.Collections.emptyList();
            return military.engine.PathfindingService.computeMovesBfs(pos, u, u.getTeam());
        }
        @Override public java.util.List<Point> getAttackableFrom(military.engine.Unit u, Point from) { throw new UnsupportedOperationException("Not implemented yet"); }
        @Override public military.engine.CombatStats previewCombat(military.engine.Unit attacker, military.engine.Unit defender, Point attackerPos, Point defenderPos) { throw new UnsupportedOperationException("Not implemented yet"); }

        private Point findUnit(military.engine.Unit target) {
            java.awt.Point size = military.engine.LocationManager.getSize();
            for (int x = 0; x < size.x; x++) {
                for (int y = 0; y < size.y; y++) {
                    military.engine.Location loc = military.engine.LocationManager.getLoc(new java.awt.Point(x, y));
                    if (!loc.isEmpty() && loc.getUnit() == target) {
                        return new java.awt.Point(x, y);
                    }
                }
            }
            return null;
        }
    }
}
