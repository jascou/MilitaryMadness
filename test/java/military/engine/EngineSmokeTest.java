package military.engine;

import military.Config;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.Point;

public class EngineSmokeTest {

    @Before
    public void ensureMaps() throws Exception {
        Files.createDirectories(Config.mapsDir());
        // Ensure a simple map exists
        Path sample = Path.of("test", "resources", "Maps", "Sample_small.txt");
        Files.copy(sample, Config.mapsDir().resolve("Sample_small.txt"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testLoadMapAndBasics() {
        LocationManager.loadMap("Sample_small");
        // size should be at least 1x1 and bases should be set
        Point size = LocationManager.getSize();
        Assert.assertTrue(size.x >= 1 && size.y >= 1);
        Assert.assertNotNull("Blue base should not be null", LocationManager.getBase(true));
        Assert.assertNotNull("Red base should not be null", LocationManager.getBase(false));
        // At start, neither side should be captured
        Assert.assertFalse(LocationManager.isCaptured(true));
        Assert.assertFalse(LocationManager.isCaptured(false));
    }
}
