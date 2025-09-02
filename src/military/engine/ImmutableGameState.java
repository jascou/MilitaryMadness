package military.engine;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of game state for rendering.
 */
public final class ImmutableGameState {
    private final boolean turn;
    private final Point cursor;
    private final List<Point> select;
    private final int blueCount;
    private final int redCount;

    public ImmutableGameState(boolean turn, Point cursor, List<Point> select, int blueCount, int redCount) {
        this.turn = turn;
        this.cursor = new Point(cursor);
        this.select = (select == null) ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(select));
        this.blueCount = blueCount;
        this.redCount = redCount;
    }

    public boolean getTurn() { return turn; }
    public Point getCursor() { return new Point(cursor); }
    public List<Point> getSelect() { return select; }
    public int getBlueCount() { return blueCount; }
    public int getRedCount() { return redCount; }
}
