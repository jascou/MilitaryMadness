package military.engine.ai;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Minimal game-facing adapter to allow AIController to be tested in headless mode
 * without constructing the full Swing-dependent Game. Production code wraps the
 * real Game into this adapter; tests can provide a lightweight fake.
 */
public interface GameAdapter {
    boolean getTurnFlag();
    ArrayList<Point> getSelectLocs();
    Point getRenderCursor();
    void endTurn();
}
