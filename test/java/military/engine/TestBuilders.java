package military.engine;

import java.awt.Point;

/** Test helpers/builders for engine tests. */
public final class TestBuilders {
    private TestBuilders() {}

    public static Unit unit(String name, boolean ranged, boolean air, boolean team,
                            int landAttack, int airAttack, int range, int defense, int shift) {
        return new Unit(name, name, ranged, air, team, landAttack, airAttack, range, defense, shift);
    }

    /**
     * Create a 2x1 grid with two adjacent locations at (0,0) and (1,0). Terrain=10 by default.
     */
    public static Location[] twoAdjacent() {
        Location a = new Location(new Point(0,0), 10);
        Location b = new Location(new Point(1,0), 10);
        a.setAdjacent(new Location[]{b});
        b.setAdjacent(new Location[]{a});
        return new Location[]{a,b};
    }
}
