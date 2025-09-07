package military.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.prefs.Preferences;

public class PreferencesManagerTest {

    private Preferences node;

    @Before
    public void setUp() {
        // Ensure a clean slate for tests by clearing the node values
        node = Preferences.userRoot().node("/military/madness");
        try {
            node.remove("sound.enabled");
            node.remove("last.map");
            node.flush();
        } catch (Exception ignored) {}
    }

    @After
    public void tearDown() {
        try {
            node.remove("sound.enabled");
            node.remove("last.map");
            node.flush();
        } catch (Exception ignored) {}
    }

    @Test
    public void testSoundEnabledRoundTrip() {
        boolean def = true;
        boolean initial = PreferencesManager.isSoundEnabled(def);
        // Initially missing -> returns default
        Assert.assertEquals(def, initial);

        PreferencesManager.setSoundEnabled(false);
        Assert.assertFalse(PreferencesManager.isSoundEnabled(true));
        PreferencesManager.setSoundEnabled(true);
        Assert.assertTrue(PreferencesManager.isSoundEnabled(false));
    }

    @Test
    public void testLastMapRoundTrip() {
        Assert.assertNull(PreferencesManager.getLastMapName());
        PreferencesManager.setLastMapName("Sample_small");
        Assert.assertEquals("Sample_small", PreferencesManager.getLastMapName());
    }
}
