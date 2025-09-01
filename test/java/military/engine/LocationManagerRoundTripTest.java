package military.engine;

import military.Config;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LocationManagerRoundTripTest {
    private Path mapsDir;

    @Before
    public void setUp() throws Exception {
        mapsDir = Config.mapsDir();
        Files.createDirectories(mapsDir);
        // Copy sample map into Maps directory
        Path sample = Path.of("test", "resources", "Maps", "Sample_small.txt");
        Files.copy(sample, mapsDir.resolve("Sample_small.txt"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    @After
    public void tearDown() throws Exception {
        // Cleanup any output map created by the test
        Files.deleteIfExists(mapsDir.resolve("Sample_small_out.txt"));
        Files.deleteIfExists(mapsDir.resolve("Sample_small.txt"));
    }

    @Test
    public void testLoadThenSaveRoundTrip() throws IOException {
        LocationManager.loadMap("Sample_small");
        // Save to a new name
        LocationManager.saveMap("Sample_small_out");
        Path original = mapsDir.resolve("Sample_small.txt");
        Path saved = mapsDir.resolve("Sample_small_out.txt");
        Assert.assertTrue("Saved file should exist", Files.exists(saved));

        List<String> originalLines = Files.readAllLines(original);
        List<String> savedLines = Files.readAllLines(saved);
        // Compare the first two lines (dimensions) and tile grid
        Assert.assertEquals("Dimensions should match", originalLines.get(0), savedLines.get(0));
        Assert.assertEquals("Dimensions should match", originalLines.get(1), savedLines.get(1));
        // Number of rows in map grid equals second dimension
        int rows = Integer.parseInt(originalLines.get(1).trim());
        for (int i = 0; i < rows; i++) {
            Assert.assertEquals("Tile row " + i + " should match", originalLines.get(2 + i).trim(), savedLines.get(2 + i).trim());
        }
    }
}
