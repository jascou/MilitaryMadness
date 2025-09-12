package military.engine.ai;

import java.awt.Point;
import java.util.Objects;

/**
 * Optional: select a tile to focus/prepare for subsequent actions.
 * Depending on engine wiring, this may be a no-op for non-GUI paths.
 */
public final class SelectAction implements AiAction {
    private final Point at;

    public SelectAction(Point at) {
        this.at = new Point(Objects.requireNonNull(at, "at"));
    }

    public Point getAt() { return new Point(at); }

    @Override
    public String toString() {
        return "SelectAction{" +
                "at=" + at +
                '}';
    }
}
