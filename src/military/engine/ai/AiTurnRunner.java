package military.engine.ai;

import military.Game;
import military.engine.Team;
import military.engine.events.EventBus;
import military.engine.events.GameEvent;
import military.engine.events.TurnStarted;
import military.util.DefaultRng;
import military.util.Rng;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Listens for TurnStarted events and triggers AI turns for configured teams.
 * Runs AI planning/execution on a single background thread to avoid blocking the EDT.
 */
public final class AiTurnRunner implements Consumer<GameEvent>, AutoCloseable {
    private static final Logger LOG = military.util.Logs.getLogger(AiTurnRunner.class);

    private final Game game;
    private final AIController controller;
    private final ExecutorService executor;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public AiTurnRunner(Game game) {
        this.game = game;
        this.controller = new AIController(new SimpleHeuristicAI());
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "AiTurnRunner");
                t.setDaemon(true);
                return t;
            }
        });
        EventBus.getInstance().register(this);
    }

    /**
     * Trigger AI for the initial turn if the current player is AI-controlled.
     */
    public void triggerIfStartingTurnIsAI() {
        if (!AiConfig.isEnabled()) return;
        Team current = game.getTurn() ? Team.BLUE : Team.RED;
        if (isAiControlled(current)) {
            submitTurn(current);
        }
    }

    private boolean isAiControlled(Team team) {
        if (!AiConfig.isEnabled()) return false;
        if (AiConfig.isControlBoth()) return true;
        return AiConfig.getAiTeam() == team;
    }

    @Override
    public void accept(GameEvent event) {
        if (!(event instanceof TurnStarted)) return;
        TurnStarted ts = (TurnStarted) event;
        if (isAiControlled(ts.team)) {
            submitTurn(ts.team);
        }
    }

    private void submitTurn(final Team team) {
        // Ensure we don't queue multiple turns simultaneously
        if (!started.compareAndSet(false, true)) return;
        if (AiConfig.isDebugSync()) {
            // Run synchronously on the current thread for debugger-friendly breakpoints
            try {
                Rng rng = (AiConfig.getSeed() == null) ? new DefaultRng() : new DefaultRng(new java.util.Random(AiConfig.getSeed()));
                controller.takeTurn(game, team, rng);
            } catch (Throwable t) {
                LOG.fine("AI turn failed: " + t.toString());
            } finally {
                started.set(false);
            }
        } else {
            executor.submit(new Runnable() {
                @Override public void run() {
                    try {
                        Rng rng = (AiConfig.getSeed() == null) ? new DefaultRng() : new DefaultRng(new java.util.Random(AiConfig.getSeed()));
                        controller.takeTurn(game, team, rng);
                    } catch (Throwable t) {
                        LOG.fine("AI turn failed: " + t.toString());
                    } finally {
                        // Allow next turn to be queued
                        started.set(false);
                    }
                }
            });
        }
    }

    @Override
    public void close() {
        EventBus.getInstance().unregister(this);
        executor.shutdownNow();
    }
}
