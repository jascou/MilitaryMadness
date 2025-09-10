package military.engine;

import java.awt.Point;
import java.io.Serializable;

/**
 * Serializable save-game metadata capturing minimal game progress required to resume play.
 * The actual map (tiles and units) is persisted as a standard map file via MapService.saveMap().
 */
public class SaveGame implements Serializable {
    private static final long serialVersionUID = 1L;

    private String mapName;       // map filename (without extension) where current map state was saved
    private boolean turn;         // true = Player1 (Blue), false = Player2 (Red)
    private int cursorX;          // cursor location X
    private int cursorY;          // cursor location Y

    public SaveGame() {}

    public SaveGame(String mapName, boolean turn, Point cursor) {
        this.mapName = mapName;
        this.turn = turn;
        if (cursor == null) cursor = new Point(0,0);
        this.cursorX = cursor.x;
        this.cursorY = cursor.y;
    }

    public String getMapName() { return mapName; }
    public boolean isTurn() { return turn; }
    public Point getCursor() { return new Point(cursorX, cursorY); }
}
