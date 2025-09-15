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
                // Provide a small delay to allow GUI to show indicator if configured
                int delay = military.engine.ai.AiConfig.getDelayMs();
                if (delay > 0) {
                    try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
                adapter.endTurn();
                return; // End turn ends execution for phase 1
            } else if (a instanceof MoveAction || a instanceof AttackAction || a instanceof SelectAction || a instanceof WaitAction) {
                // Not implemented in phase 1
                LOG.fine("Ignoring action (not yet implemented in phase 1): " + a);
                // Allow UI to render between actions
                int delay = military.engine.ai.AiConfig.getDelayMs();
                if (delay > 0) {
                    try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
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
        @Override public java.util.List<Point> getAttackableFrom(military.engine.Unit u, Point from) {
            java.util.ArrayList<Point> out = new java.util.ArrayList<>();
            if (u == null || from == null) return out;
            java.awt.Point size = military.engine.LocationManager.getSize();
            // Bounds check
            if (from.x < 0 || from.y < 0 || from.x >= size.x || from.y >= size.y) return out;
            boolean turn = u.getTeam();
            military.engine.Location origin = military.engine.LocationManager.getLoc(from);
            if (origin == null) return out;
            if (u.isRanged()) {
                // BFS up to range across adjacency, collecting enemy-occupied tiles
                class Node { military.engine.Location loc; int depth; Node(military.engine.Location l, int d){loc=l;depth=d;} }
                java.util.ArrayDeque<Node> stack = new java.util.ArrayDeque<>();
                // Start from adjacent tiles with depth range-1 (to match Game.rangedIterative semantics)
                for (military.engine.Location adj : origin.getAdjacent()) {
                    stack.push(new Node(adj, Math.max(0, u.getRange() - 1)));
                }
                while (!stack.isEmpty()) {
                    Node node = stack.pop();
                    for (military.engine.Location loc : node.loc.getAdjacent()) {
                        if (!loc.isEmpty()) {
                            military.engine.Unit defender = loc.getUnit();
                            if (defender.getTeam() != turn) {
                                java.awt.Point p = loc.getLoc();
                                // Avoid duplicates
                                boolean exists = false;
                                for (java.awt.Point q : out) { if (q.equals(p)) { exists = true; break; } }
                                if (!exists) out.add(new java.awt.Point(p));
                            }
                        }
                        if (node.depth > 1) {
                            stack.push(new Node(loc, node.depth - 1));
                        }
                    }
                }
            } else {
                // Melee: adjacent enemy units only
                for (military.engine.Location loc : origin.getAdjacent()) {
                    if (!loc.isEmpty()) {
                        military.engine.Unit defender = loc.getUnit();
                        if (defender.getTeam() != turn) {
                            out.add(new java.awt.Point(loc.getLoc()));
                        }
                    }
                }
            }
            return out;
        }
        @Override public military.engine.CombatStats previewCombat(military.engine.Unit attacker, military.engine.Unit defender, Point attackerPos, Point defenderPos) {
            if (attacker == null || defender == null) return null;
            // Build lightweight clones so that CombatStats mutation does not affect real units
            military.engine.Unit atk = new military.engine.Unit(attacker.getName(), attacker.getType(), attacker.isRanged(), attacker.isAir(), attacker.getTeam(), attacker.getLandAttack(), attacker.getAirAttack(), attacker.getRange(), attacker.getDefense(), attacker.getShift());
            military.engine.Unit def = new military.engine.Unit(defender.getName(), defender.getType(), defender.isRanged(), defender.isAir(), defender.getTeam(), defender.getLandAttack(), defender.getAirAttack(), defender.getRange(), defender.getDefense(), defender.getShift());
            // Copy dynamic state
            atk.setHealth(attacker.getHealth());
            def.setHealth(defender.getHealth());
            atk.addExp(attacker.getExp());
            def.addExp(defender.getExp());
            // Create ephemeral Locations with terrain from map to approximate terrain effects
            military.engine.Location atkLoc = military.engine.LocationManager.getLoc(attackerPos != null ? attackerPos : attacker.getLoc().getLoc());
            military.engine.Location defLoc = military.engine.LocationManager.getLoc(defenderPos != null ? defenderPos : defender.getLoc().getLoc());
            if (atkLoc == null || defLoc == null) return null;
            // Attach clones to ephemeral locations: create wrappers duplicating terrain/adjacency so support/surround roughly apply
            // We will reuse real Location objects but place cloned units there temporarily; since addUnit checks null, use setLoc directly
            atk.setLoc(atkLoc);
            def.setLoc(defLoc);
            try {
                return new military.engine.CombatStats(atk, def);
            } catch (Throwable t) {
                return null;
            }
        }

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
