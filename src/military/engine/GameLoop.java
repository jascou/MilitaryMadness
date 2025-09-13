package military.engine;

import military.Game;
import military.gui.GUIMiddleMan;
import military.engine.ai.AIController;
import military.engine.ai.AIPlayer;
import military.engine.ai.AiConfig;
import military.engine.ai.SimpleHeuristicAI;
import military.util.DefaultRng;
import military.util.Rng;

import java.awt.event.InputEvent;

/**
 * Dedicated controller/service that owns the main game loop and rendering trigger.
 * This moves loop concerns out of Game to clarify the MVC/MVP boundary.
 */
public class GameLoop implements Runnable {
    private final Game game;
    private final boolean aiEnabled;
    private final Team aiTeam;
    private final AIController aiController;
    private final Rng aiRng;

    public GameLoop(Game game) {
        this.game = game;
        // Initialize AI wiring based on global config
        this.aiEnabled = AiConfig.isEnabled();
        this.aiTeam = AiConfig.getAiTeam();
        if (aiEnabled) {
            AIPlayer player = new SimpleHeuristicAI();
            this.aiController = new AIController(player);
            Long seed = AiConfig.getSeed();
            this.aiRng = (seed == null) ? new DefaultRng() : new DefaultRng(new java.util.Random(seed));
        } else {
            this.aiController = null;
            this.aiRng = null;
        }
    }

    @Override
    public void run() {
        // Loop until the opposite team is captured (legacy rule)
        while (!LocationManager.isCaptured(!game.getTurn())) {
            if (!game.isFactory()) {
                // Trigger a render via the GameController/GUI using current state
                game.getController().render(
                        game.getGui(),
                        game.getTurn(),
                        game.getSelectLocs(),
                        game.getRenderCursor()
                );
            }

            // If it's the AI team's turn, execute AI on a background thread and skip human input
            if (aiEnabled) {
                Team current = game.getTurn() ? Team.BLUE : Team.RED;
                if (current == aiTeam) {
                    Thread aiThread = new Thread(() -> aiController.takeTurn(game, aiTeam, aiRng), "AI-Turn");
                    aiThread.setDaemon(true);
                    aiThread.start();
                    try {
                        aiThread.join(); // wait for AI to complete its (short) turn
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    // Continue loop; the turn likely switched inside AI controller
                    continue;
                }
            }

            // Human turn: block for next input event and let Game handle it
            InputEvent evt = GUIMiddleMan.getInstance().getEvent();
            game.stepOnce(evt);
        }
    }
}
