package military.engine.ai;

import java.awt.Point;
import java.util.Objects;

/**
 * Move a unit from one tile to another.
 * The engine will validate legality (range, occupancy, turn rules) when applying.
 */
public final class MoveAction implements AiAction {
    private final Point from;
    private final Point to;

    public MoveAction(Point from, Point to) {
        this.from = new Point(Objects.requireNonNull(from, "from"));
        this.to = new Point(Objects.requireNonNull(to, "to"));
    }

    public Point getFrom() { return new Point(from); }
    public Point getTo() { return new Point(to); }

    @Override
    public String toString() {
        return "MoveAction{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}
