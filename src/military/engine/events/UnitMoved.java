package military.engine.events;

import java.awt.Point;
import military.engine.Unit;

public class UnitMoved implements GameEvent {
    public final Unit unit;
    public final Point to;
    public UnitMoved(Unit unit, Point to) {
        this.unit = unit;
        this.to = new Point(to);
    }
}
