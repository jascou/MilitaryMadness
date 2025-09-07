package military.engine;

import military.Game;
import military.gui.GUIMiddleMan;

import java.awt.Point;
import java.awt.event.InputEvent;

/**
 * Dedicated controller/service that owns the main game loop and rendering trigger.
 * This moves loop concerns out of Game to clarify the MVC/MVP boundary.
 */
public class GameLoop implements Runnable {
    private final Game game;

    public GameLoop(Game game) {
        this.game = game;
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
            // Block for next input event and let Game handle it
            InputEvent evt = GUIMiddleMan.getInstance().getEvent();
            game.stepOnce(evt);
        }
    }
}
