package military.engine;

import military.Config;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Point;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Verifies SaveLoadService round-trips basic game metadata and reloads the map.
 */
public class SaveLoadRoundTripTest {
    private Path mapsDir;
    private String mapName = "Sample_small";
    private String saveName = "test_save_slot";

    @Before
    public void setUp() throws Exception {
        mapsDir = Config.mapsDir();
        Files.createDirectories(mapsDir);
        // Copy sample map from test resources into Maps directory
        Path target = mapsDir.resolve(mapName + ".txt");
        try (InputStream in = this.getClass().getResourceAsStream("/Maps/" + mapName + ".txt")) {
            Assert.assertNotNull("Test fixture map not found in resources", in);
            Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @After
    public void tearDown() throws Exception {
        // Clean up save file
        Path saveFile = Config.savesDir().resolve(saveName + ".mmsave");
        Files.deleteIfExists(saveFile);
    }

    @Test
    public void testSaveAndLoadGameMetaAndMap() {
        // Load the map
        new DefaultMapService().loadMap(mapName);
        Point sizeBefore = LocationManager.getSize();
        Assert.assertTrue(sizeBefore.x > 0 && sizeBefore.y > 0);

        // Prepare state and save
        GameState state = new GameState();
        state.setTurn(false);
        state.setCursor(new Point(2, 3));
        SaveLoadService.save(saveName, mapName, state);

        // Change in-memory map to ensure load actually reloads
        new DefaultMapService().newLoc(new Point(0,0), -1);

        // Load
        SaveGame data = SaveLoadService.load(saveName);
        Assert.assertEquals(mapName, data.getMapName());
        Assert.assertEquals(false, data.isTurn());
        Assert.assertEquals(new Point(2,3), data.getCursor());
        // Map should be reloaded and size remain the same
        Point sizeAfter = LocationManager.getSize();
        Assert.assertEquals(sizeBefore, sizeAfter);
    }
}
