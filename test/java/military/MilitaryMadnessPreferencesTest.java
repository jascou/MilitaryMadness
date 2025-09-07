package military;

import military.Config;
import military.util.PreferencesManager;
import org.junit.*;

import javax.swing.*;
import java.nio.file.*;

public class MilitaryMadnessPreferencesTest {

    private Path mapsDir;

    @Before
    public void setUp() throws Exception {
        mapsDir = Config.mapsDir();
        Files.createDirectories(mapsDir);
        Path sample = Paths.get("test", "resources", "Maps", "Sample_small.txt");
        Files.copy(sample, mapsDir.resolve("Sample_small.txt"), StandardCopyOption.REPLACE_EXISTING);
        // Seed preference
        PreferencesManager.setLastMapName("Sample_small");
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(mapsDir.resolve("Sample_small.txt"));
    }

    @Test
    public void loadMapListSelectsLastMapFromPreferences() {
        boolean has = MilitaryMadness.loadMapList();
        Assert.assertTrue("Maps should be discovered", has);
        JComboBox<String> combo = MilitaryMadness.scenarioComboBox;
        Assert.assertNotNull(combo);
        Assert.assertEquals("Sample_small", combo.getSelectedItem());
    }
}
