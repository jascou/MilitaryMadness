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
        // Build a minimal snapshot view for planning
        ImmutableGameState view = buildSnapshot(game);
        ReadOnlyServices svc = new NoopServices();
        List<AiAction> plan;
        try {
            plan = ai.planTurn(view, svc, rng);
        } catch (Throwable t) {
            LOG.warning("AI planning failed: " + t.getMessage());
            plan = java.util.Collections.<AiAction>singletonList(new EndTurnAction());
        }
        execute(game, plan);
    }

    private void execute(Game game, List<AiAction> actions) {
        if (actions == null) return;
        for (AiAction a : actions) {
            if (a instanceof EndTurnAction) {
                LOG.fine("Executing EndTurnAction");
                game.endTurnForAutomation();
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
        game.endTurnForAutomation();
    }

    private ImmutableGameState buildSnapshot(Game game) {
        // Reuse data from Game for counts; cursor is whatever Game would render
        java.util.ArrayList<java.awt.Point> select = game.getSelectLocs();
        Point cursor = game.getRenderCursor();
        // Query counts via UnitRepository adapter used by GUI/GameController
        military.engine.UnitRepository repo = military.engine.DefaultUnitRepository.getInstance();
        int blue = repo.getUnits(military.engine.Team.BLUE).size();
        int red = repo.getUnits(military.engine.Team.RED).size();
        return new ImmutableGameState(game.getTurn(), cursor == null ? new Point(0,0) : cursor,
                select == null ? new ArrayList<Point>() : select,
                blue, red);
    }

    /**
     * Phase-1 stub services: methods are intentionally left unimplemented because
     * SimpleHeuristicAI does not use them yet.
     */
    private static final class NoopServices implements ReadOnlyServices {
        @Override public java.util.List<military.engine.Unit> getUnits(Team team) { throw new UnsupportedOperationException("Not implemented in phase 1"); }
        @Override public boolean isOccupied(Point p) { throw new UnsupportedOperationException("Not implemented in phase 1"); }
        @Override public military.engine.Location getLocation(Point p) { throw new UnsupportedOperationException("Not implemented in phase 1"); }
        @Override public java.util.List<Point> getReachable(military.engine.Unit u) { throw new UnsupportedOperationException("Not implemented in phase 1"); }
        @Override public java.util.List<Point> getAttackableFrom(military.engine.Unit u, Point from) { throw new UnsupportedOperationException("Not implemented in phase 1"); }
        @Override public military.engine.CombatStats previewCombat(military.engine.Unit attacker, military.engine.Unit defender, Point attackerPos, Point defenderPos) { throw new UnsupportedOperationException("Not implemented in phase 1"); }
    }
}
