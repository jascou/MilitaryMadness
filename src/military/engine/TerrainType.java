package military.engine;

/**
 * Terrain types mapped roughly to existing integer codes.
 */
public enum TerrainType {
    VOID(-1),
    PLAIN(0),
    FOREST(1),
    MOUNTAIN(2),
    WATER(3),
    ROAD(4),
    BLUE_BASE(5),
    RED_BASE(6),
    FACTORY(7),
    BLUE_FACTORY(8),
    RED_FACTORY(9);

    public final int code;

    TerrainType(int code) { this.code = code; }
}
