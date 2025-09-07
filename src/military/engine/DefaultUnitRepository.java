package military.engine;

import java.util.List;

/**
 * Default adapter that delegates to the legacy UnitManager singleton while allowing injection.
 */
public class DefaultUnitRepository implements UnitRepository {
    private static final DefaultUnitRepository INSTANCE = new DefaultUnitRepository();

    public static DefaultUnitRepository getInstance() {
        return INSTANCE;
    }

    private DefaultUnitRepository() {}

    @Override
    public void removeUnit(Unit u) {
        UnitManager.getInstance().removeUnit(u);
    }

    @Override
    public void resetUnits() {
        UnitManager.getInstance().resetUnits();
    }

    @Override
    public List<Unit> getUnits(Team team) {
        // Legacy UnitManager uses boolean team flag: true=blue, false=red
        boolean legacy = (team == Team.BLUE);
        return UnitManager.getInstance().getUnits(legacy);
    }
}
