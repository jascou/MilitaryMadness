package military.engine.ai;

import military.engine.ImmutableGameState;
import military.util.Rng;

import java.util.List;

/**
 * Strategy interface for AI players to plan a full turn.
 * Implementations should be deterministic for a given input state and RNG.
 */
public interface AIPlayer {
    /**
     * Plan a sequence of actions for the current turn.
     *
     * @param view immutable snapshot for rendering/state observation
     * @param svc read-only services facade for engine queries
     * @param rng random number generator to use for any tie-breaking/random decisions
     * @return ordered list of actions to execute; must end with an {@link EndTurnAction} eventually
     */
    List<AiAction> planTurn(ImmutableGameState view, ReadOnlyServices svc, Rng rng);
}
