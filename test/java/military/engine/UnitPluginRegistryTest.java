package military.engine;

import military.Config;
import org.junit.*;

import java.awt.Point;
import java.nio.file.*;

public class UnitPluginRegistryTest {

    @Before
    public void setUp() throws Exception {
        // Ensure a small map exists
        Path mapsDir = Config.mapsDir();
        Files.createDirectories(mapsDir);
        Path sample = Paths.get("test", "resources", "Maps", "Sample_small.txt");
        Files.copy(sample, mapsDir.resolve("Sample_small.txt"), StandardCopyOption.REPLACE_EXISTING);
        LocationManager.loadMap("Sample_small");
    }

    @After
    public void tearDown() throws Exception {
        UnitPluginRegistry.clear();
        // do not clean maps to avoid interfering with other tests that manage it
    }

    @Test
    public void pluginCreatesCustomUnitByName() {
        // Register a simple creator for a made-up unit name
        UnitPluginRegistry.register("TestBot", (name, team) -> new Unit(name, "Robot", false, false, team, 5, 0, 1, 2, 3));
        Point p = new Point(0,0);
        // Ensure empty tile
        if (!LocationManager.getLoc(p).isEmpty()) {
            p = new Point(1,0);
        }
        LocationManager.addUnit(p, "TestBot", true);
        Unit u = LocationManager.getLoc(p).getUnit();
        Assert.assertNotNull(u);
        Assert.assertEquals("TestBot", u.getName());
        Assert.assertEquals("Robot", u.getType());
        Assert.assertTrue(u.getTeam());
        Assert.assertEquals(5, u.getLandAttack());
    }
}
