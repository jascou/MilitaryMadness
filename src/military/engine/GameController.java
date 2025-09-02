package military.engine;

import military.gui.GUI;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Thin controller facade owning GameState. Initial step towards decoupling UI from the game loop.
 */
public class GameController {
    private final GameState state;

    public GameController(GameState state) {
        this.state = state;
    }

    public GameState getState() {
        return state;
    }

    /**
     * Render the frame using the current state and data provided by the core logic.
     */
    public void render(GUI gui, boolean turn, ArrayList<Point> selectLocs, Point cursor) {
        state.setTurn(turn);
        state.setCursor(new Point(cursor));
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            gui.render(turn, selectLocs, cursor);
        } else {
            try {
                javax.swing.SwingUtilities.invokeAndWait(() -> gui.render(turn, selectLocs, cursor));
            } catch (Exception ignored) {
                gui.render(turn, selectLocs, cursor);
            }
        }
    }
}
