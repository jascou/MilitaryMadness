package military.engine;

import java.awt.Point;

/**
 * Minimal game state model to decouple state from rendering and input handling.
 */
public class GameState {
    private boolean turn; // true: Player1, false: Player2
    private Point cursor = new Point(1, 0);

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public Point getCursor() {
        return cursor;
    }

    public void setCursor(Point cursor) {
        this.cursor = cursor;
    }
}
