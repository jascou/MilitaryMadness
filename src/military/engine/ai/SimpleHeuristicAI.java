package military.engine.ai;

import military.engine.ImmutableGameState;
import military.util.Rng;

import java.util.Collections;
import java.util.List;

/**
 * Phase-1 stub implementation that simply ends the turn immediately.
 */
public class SimpleHeuristicAI implements AIPlayer {
    @Override
    public List<AiAction> planTurn(ImmutableGameState view, ReadOnlyServices svc, Rng rng) {
        return Collections.<AiAction>singletonList(new EndTurnAction());
    }
}
