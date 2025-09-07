package military.engine;

import military.engine.Unit;
import military.engine.Team;
import java.util.List;

/**
 * Abstraction over unit storage to decouple from the UnitManager singleton.
 */
public interface UnitRepository {
    void removeUnit(Unit u);
    void resetUnits();
    List<Unit> getUnits(Team team);
}
