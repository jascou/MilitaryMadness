package military.engine;

import military.Config;
import military.util.ResourceLoader;
import org.junit.*;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Scanner;

public class GameStateIoTest {

    private Path mapsDir;

    @Before
    public void setUp() throws Exception {
        mapsDir = Config.mapsDir();
        Files.createDirectories(mapsDir);
        // Copy sample map into Maps directory
        Path sample = Paths.get("test", "resources", "Maps", "Sample_small.txt");
        Files.copy(sample, mapsDir.resolve("Sample_small.txt"), StandardCopyOption.REPLACE_EXISTING);
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(mapsDir.resolve("Sample_small.txt"));
        Files.deleteIfExists(mapsDir.resolve("state_test.gstate"));
    }

    @Test
    public void saveThenLoadRestoresUnitsAndStats() throws Exception {
        // Load base map
        LocationManager.loadMap("Sample_small");
        // Find a unit name from Units.txt for portability
        // We'll pick a unit name if available; otherwise use a fallback that DebugStateIO can load with defaults
        String unitName = "Infantry";
        try (InputStream in = ResourceLoader.openTextFromResources("Units.txt");
             Scanner sc = new Scanner(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            if (sc.hasNext()) unitName = sc.next();
        }
        // Place a unit and tweak stats
        Point p = new Point(0, 0);
        // Ensure we're placing on a valid tile (not -1)
        for (int x = 0; x < LocationManager.getSize().x; x++) {
            for (int y = 0; y < LocationManager.getSize().y; y++) {
                if (LocationManager.getLoc(x, y).getType() >= 0 && LocationManager.getLoc(x, y).isEmpty()) {
                    p = new Point(x, y);
                    x = LocationManager.getSize().x; // break outer
                    break;
                }
            }
        }
        Unit u = TestBuilders.unit(unitName, false, false, true, 10, 0, 1, 4, 3);
        LocationManager.getLoc(p).addUnit(u);
        UnitManager.getInstance().addUnit(u);
        u.setHealth(5);
        u.addExp(3);

        // Save state
        Path out = mapsDir.resolve("state_test.gstate");
        DebugStateIO.saveState(out);

        // Perturb current world (generate a tiny new map and clear UnitManager) to be sure we actually reload
        LocationManager.generateMap(2, 2);
        UnitManager.getInstance().getUnits(true).clear();
        UnitManager.getInstance().getUnits(false).clear();

        // Load state
        DebugStateIO.loadState(out);

        // Verify restored
        Unit restored = LocationManager.getLoc(p).getUnit();
        Assert.assertNotNull("Unit should be restored at saved position", restored);
        Assert.assertEquals("Name preserved", u.getName(), restored.getName());
        Assert.assertTrue("Team preserved (blue)", restored.getTeam());
        Assert.assertEquals("Health restored", 5, restored.getHealth());
        Assert.assertTrue("Exp at least saved amount (addExp accumulates)", restored.getExp() >= 3);
    }
}
