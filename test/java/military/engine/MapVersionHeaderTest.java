package military.engine;

import military.Config;
import org.junit.*;

import java.nio.file.*;
import java.util.List;

public class MapVersionHeaderTest {

    private Path mapsDir;

    @Before
    public void setUp() throws Exception {
        mapsDir = Config.mapsDir();
        Files.createDirectories(mapsDir);
        // Copy sample map
        Path sample = Paths.get("test", "resources", "Maps", "Sample_small.txt");
        Files.copy(sample, mapsDir.resolve("Sample_small.txt"), StandardCopyOption.REPLACE_EXISTING);
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(mapsDir.resolve("Sample_small.txt"));
        Files.deleteIfExists(mapsDir.resolve("Sample_small_v1.txt"));
    }

    @Test
    public void loadVersionedHeaderFile() throws Exception {
        // Load legacy map
        LocationManager.loadMap("Sample_small");
        // Save with header to a new file
        LocationManager.saveMapV1("Sample_small_v1");
        Path v1 = mapsDir.resolve("Sample_small_v1.txt");
        Assert.assertTrue(Files.exists(v1));
        // Ensure header is present
        List<String> lines = Files.readAllLines(v1);
        Assert.assertTrue("First line should be header", lines.get(0).startsWith("MMAPv"));
        // Now load the versioned file to ensure loader accepts it
        LocationManager.loadMap("Sample_small_v1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHeaderThrows() throws Exception {
        Path bad = mapsDir.resolve("BadHeader.txt");
        Files.write(bad, java.util.Arrays.asList("BADHDR", "2", "2", "0 0", "0 0"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            LocationManager.loadMap("BadHeader");
        } finally {
            Files.deleteIfExists(bad);
        }
    }
}
