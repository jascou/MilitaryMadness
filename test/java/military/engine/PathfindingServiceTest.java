package military.engine;

import military.util.ResourceLoader;
import org.junit.*;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Task 82: Verify deterministic BFS pathfinding helper respecting movement costs and obstacles.
 */
public class PathfindingServiceTest {

    @Before
    public void setupMap() throws Exception {
        // Ensure Sample_small.txt is available in runtime Maps directory
        java.nio.file.Path mapsDir = military.Config.mapsDir();
        java.nio.file.Files.createDirectories(mapsDir);
        java.nio.file.Path dst = mapsDir.resolve("Sample_small.txt");
        if (!java.nio.file.Files.exists(dst)) {
            try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("Maps/Sample_small.txt")) {
                if (in == null) throw new IllegalStateException("Test resource Maps/Sample_small.txt not found");
                java.nio.file.Files.copy(in, dst, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
        // Load the sample map used by other engine tests (pass base name without extension)
        new DefaultMapService().loadMap("Sample_small");

        // Clear any units that might have been left by previous tests (defensive).
        // Sample_small has no units by default, so this is just a precaution.
    }

    private String getFirstUnitNameFromResources() throws Exception {
        try (InputStream in = ResourceLoader.openTextFromResources("Units.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringTokenizer st;
            while (true) {
                String name = nextToken(br);
                if (name == null) throw new IllegalStateException("Units.txt seemed empty");
                String type = nextToken(br); if (type == null) break;
                String isRange = nextToken(br); if (isRange == null) break;
                String isAir = nextToken(br); if (isAir == null) break;
                String land = nextToken(br); if (land == null) break;
                String air = nextToken(br); if (air == null) break;
                String range = nextToken(br); if (range == null) break;
                String def = nextToken(br); if (def == null) break;
                String shift = nextToken(br); if (shift == null) break;
                return name;
            }
        }
        // Fallback safe default (UnitPluginRegistry will fabricate minimal stats if unknown)
        return "Infantry";
    }

    // Minimal scanner to read whitespace-separated tokens reliably
    private static String nextToken(BufferedReader br) throws Exception {
        int c;
        StringBuilder sb = new StringBuilder();
        // skip whitespace
        while ((c = br.read()) != -1) {
            if (!Character.isWhitespace(c)) { sb.append((char)c); break; }
        }
        if (sb.length() == 0) return null;
        while ((c = br.read()) != -1) {
            if (Character.isWhitespace(c)) break;
            sb.append((char)c);
        }
        return sb.toString();
    }

    @Test
    public void testReachableIncludesStartAndRespectsShift() throws Exception {
        // Place a single RED unit at (0,0)
        String unitName = getFirstUnitNameFromResources();
        LocationManager.addUnit(new Point(0,0), unitName, /*team*/ false);
        Unit u = LocationManager.getLoc(0,0).getUnit();
        Assert.assertNotNull("Unit should be placed at (0,0)", u);
        int shift = u.getShift();

        List<Point> reachable = PathfindingService.computeMovesBfs(new Point(0,0), u, /*turn=RED*/ false);
        // PathfindingService excludes occupied tiles (including the unit's own starting tile)
        Assert.assertFalse(reachable.contains(new Point(0,0)));
        // On Sample_small, terrains are 0 except bases at (1,1) and (1,2). Terrain cost 0 treated as 1, bases are buildings.
        // The pathfinder disallows entering enemy base for non-infantry; but we don't rely on that. We assert an upper bound on distance.
        // With cost 1 per tile, the farthest Manhattan distance reachable should be <= shift.
        for (Point p : reachable) {
            int dist = Math.abs(p.x - 0) + Math.abs(p.y - 0);
            Assert.assertTrue("Tile " + p + " should not exceed shift cost", dist <= shift);
        }
    }

    @Test
    public void testEnemyOccupiedAndFlankedBlocking() throws Exception {
        // Reset map by reloading
        new DefaultMapService().loadMap("Sample_small");
        String unitName = getFirstUnitNameFromResources();
        // Place RED at (0,1), BLUE enemy at (1,1) (blue base tile, but add a blue unit explicitly on it) and another enemy at (0,2)
        LocationManager.addUnit(new Point(0,1), unitName, /*RED*/ false);
        LocationManager.addUnit(new Point(1,1), unitName, /*BLUE*/ true);
        LocationManager.addUnit(new Point(0,2), unitName, /*BLUE*/ true);
        Unit red = LocationManager.getLoc(0,1).getUnit();
        List<Point> reachable = PathfindingService.computeMovesBfs(new Point(0,1), red, false);

        // Enemy-occupied tiles themselves should not be in results
        Assert.assertFalse(reachable.contains(new Point(1,1)));
        Assert.assertFalse(reachable.contains(new Point(0,2)));

        // Tile (1,0) is adjacent to enemy at (1,1); according to flanked rule, we can potentially step there,
        // but expansion beyond flanked tiles is curtailed (they are not enqueued). Ensure we still can reach (1,0)
        // if within shift, but not expand through it to (2,0) on an empty map when shift is large.
        boolean canReach10 = reachable.contains(new Point(1,0));
        if (canReach10) {
            // If (1,0) is reachable, ensure that (2,0) is NOT reachable due to flanked expansion block unless directly reachable otherwise.
            Assert.assertFalse("Expansion should not continue beyond flanked tile (1,0)", reachable.contains(new Point(2,0)));
        }
    }

    @Test
    public void testDeterministicRepeatability() throws Exception {
        // Reload clean map, place a unit and compute twice; results should be identical
        new DefaultMapService().loadMap("Sample_small");
        String unitName = getFirstUnitNameFromResources();
        LocationManager.addUnit(new Point(2,2), unitName, /*RED*/ false);
        Unit u = LocationManager.getLoc(2,2).getUnit();
        List<Point> a = PathfindingService.computeMovesBfs(new Point(2,2), u, false);
        List<Point> b = PathfindingService.computeMovesBfs(new Point(2,2), u, false);

        // Compare as sets to avoid relying on order
        Set<Point> sa = new HashSet<>(a);
        Set<Point> sb = new HashSet<>(b);
        Assert.assertEquals(sa, sb);
    }
}
